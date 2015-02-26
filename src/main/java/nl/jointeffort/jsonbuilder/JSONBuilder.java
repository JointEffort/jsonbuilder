package nl.jointeffort.jsonbuilder;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JSONBuilder {

	private ReflectionCache reflectionCache = new ReflectionCache();
	private Map<Class<?>, Transformer> transformers = new HashMap<Class<?>, Transformer>();
	private CaseFormat sourceCaseFormat;
	private CaseFormat targetCaseFormat;
	private Stack<Object> inputStack;
	private Stack<StringBuilder> outputStack;
	private Stack<Map<String, Object>> pathStack;
	private Map<String, Object> includePaths = new HashMap<String, Object>();
	private Map<String, Filter<?>> filters = new HashMap<String, Filter<?>>();
	private String rootScope;

	/**
	 * Create JSONBuilder without case conversion.
	 */
	public JSONBuilder() {
		this(null, null);
	}

	/**
	 * Create a JSONBuilder with the specified case conversion.
	 */
	public JSONBuilder(CaseFormat sourceCaseFormat, CaseFormat targetCaseFormat) {
		this.sourceCaseFormat = sourceCaseFormat;
		this.targetCaseFormat = targetCaseFormat;
		addStandardTransformers();
	}

	private void addStandardTransformers() {
		withTransformer(Calendar.class, new CalendarTransformer());
	}

	/**
	 * Add a transformer for the specified datatype.
	 * 
	 * @param fieldClass
	 * @param transformer
	 * @return this for fluent interface (builder pattern)
	 */
	public JSONBuilder withTransformer(Class<?> fieldClass, Transformer transformer) {
		transformers.put(fieldClass, transformer);
		return this;
	}

	/**
	 * Serialize the specified object, writing it to the specifed writer.
	 * 
	 * @param object
	 * @return a JSON string.
	 */
	public void serialize(Object object, Writer writer) throws IOException {
		writer.write(serialize(object));
		writer.flush();
	}

	/**
	 * Serialize the specified object.
	 * 
	 * @param object
	 * @return a JSON string.
	 */
	public String serialize(Object object) {
		boolean collectionRoot = object instanceof Collection;
		outputStack = new Stack<StringBuilder>();
		outputStack.push(new StringBuilder());
		inputStack = new Stack<Object>();
		inputStack.push(object);
		pathStack = new Stack<Map<String, Object>>();
		pathStack.push(includePaths);

		if (collectionRoot) {
			Collection col = (Collection) object;

			if (isSimpleCollection(col)) {
				addKVPCollection(null, col);
			} else {
				Iterator iterator = col.iterator();
				StringBuilder currentOutput = outputStack.peek();
				boolean first = true;
				while (iterator.hasNext()) {
					Object obj = iterator.next();
					recursiveSerialize(obj, pathStack.peek());
					currentOutput.append(first ? "{" : ",{").append(outputStack.pop()).append('}');
					first = false;

				}
			}
			return finish(true);
		} else {
			for (String includePath : includePaths.keySet()) {
				serializeProperty(includePath);
			}
			return finish(false);
		}
	}

	private boolean isSimpleCollection(Collection col) {
		if (col.size() > 1) {
			Object obj = col.iterator().next();
			return reflectionCache.isSimpleType(obj);
		} else {
			return false;
		}
	}

	public String serialize(Map map) {
		StringBuilder builder = new StringBuilder();
		for (Object key : map.keySet()) {
			Object keyString = key.toString();
			Object value = map.get(key);
			String jsonValue = null;
			if (value instanceof Map) {
				jsonValue = serialize((Map) value);
			} else if (value == null) {
				jsonValue = "null";
			} else if (value instanceof Boolean || value instanceof Number) {
				jsonValue = String.valueOf(value);
			} else {
				jsonValue = "\"" + jsonEscape(String.valueOf(value)) + "\"";
			}
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append(String.format("\"%s\":%s", keyString, jsonValue));
		}
		return builder.insert(0, "{").append("}").toString();
	}

	private String finish(boolean rootIsCollection) {
		StringBuilder output = outputStack.pop();
		if (rootIsCollection) {
			output.insert(0, '[').append(']');
		} else {
			output.insert(0, '{').append('}');
		}
		if (!Strings.isNullOrEmpty(rootScope)) {
			String prefix = "{\"" + rootScope + "\":";
			output.insert(0, prefix).append("}");
		}
		return output.toString();
	}

	private void serializeProperty(String path) {
		Object currentObject = inputStack.peek();
		reflectionCache.ensureClassIsReflected(currentObject.getClass());
		Map<String, Object> currentObjectPaths = pathStack.peek();
		Object propertyDescription = currentObjectPaths.get(path);
		StringBuilder currentOutput = outputStack.peek();
		if (propertyDescription == null) {
			Member member = reflectionCache.getMember(currentObject, path);
			Class memberType = member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
			Object memberValue = reflectionCache.getMemberValue(member, currentObject);
			if (memberValue == null) {
				addKVP(member, memberValue);
			} else if (isCollectionClass(memberType)) {
				// Collection of 'simple' elements
				String key = member instanceof Field ? ((Field) member).getName() : reflectionCache
						.getPropertyName((Method) member);
				addKVPCollection(key, (Collection) memberValue);
			} else {
				// Simple property
				addKVP(member, memberValue);
			}
		} else if (propertyDescription instanceof PropertyTransposition) {
			PropertyTransposition transposition = (PropertyTransposition) propertyDescription;
			String key = path;
			Object value = transposition.getSourceValue(reflectionCache, currentObject, inputStack.size() - 1);
			if (currentOutput.length() > 0) {
				currentOutput.append(',');
			}
			addKVP(key, value);

		} else {
			Member member = reflectionCache.getMember(currentObject, path);
			Class memberType = member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
			Object memberValue = reflectionCache.getMemberValue(member, currentObject);
			// Nested object, can be a collection or single element association
			if (memberValue == null) {
				addKVP(member, memberValue);
			} else if (isCollectionClass(memberType)) {
				if (currentOutput.length() > 0) {
					currentOutput.append(',');
				}
				String key = path;
				if (sourceCaseFormat != null) {
					key = sourceCaseFormat.to(targetCaseFormat, path);
				}
				if (sourceCaseFormat != null) {
					key = sourceCaseFormat.to(targetCaseFormat, path);
				}
				currentOutput.append('"').append(key).append("\":[");
				Collection col = (Collection) memberValue;
				Iterator iterator = col.iterator();
				boolean first = true;
				while (iterator.hasNext()) {
					Object object = iterator.next();
					Filter filter = filters.get(path);
					if (filter != null && !filter.include(object)) {
						continue;
					}
					recursiveSerialize(object, (Map<String, Object>) propertyDescription);
					currentOutput.append(first ? "{" : ",{").append(outputStack.pop()).append('}');
					first = false;
				}
				currentOutput.append("]");
			} else {
				recursiveSerialize(memberValue, (Map<String, Object>) propertyDescription);
				if (currentOutput.length() > 0) {
					currentOutput.append(',');
				}
				if (sourceCaseFormat != null) {
					path = sourceCaseFormat.to(targetCaseFormat, path);
				}
				currentOutput.append('"').append(path).append("\":{").append(outputStack.pop()).append("}");
			}
		}
	}

	private void addKVP(String key, Object value) {
		if (sourceCaseFormat != null) {
			key = sourceCaseFormat.to(targetCaseFormat, key);
		}
		StringBuilder output = outputStack.peek();
		if (value == null) {
			output.append(String.format("\"%s\":null", key));
		} else if (value instanceof Map) {
			output.append(String.format("\"%s\":%s", key, serialize((Map)value)));
		} else if (value instanceof Boolean || value instanceof Number) {
			output.append(String.format("\"%s\":%s", key, String.valueOf(value)));
		} else {
			output.append(String.format("\"%s\":\"%s\"", key, jsonEscape(String.valueOf(value))));
		}
	}

	private void recursiveSerialize(Object object, Map<String, Object> nestedPaths) {
		inputStack.push(object);
		pathStack.push(nestedPaths);
		outputStack.push(new StringBuilder());
		for (String p2 : nestedPaths.keySet()) {
			serializeProperty(p2);
		}
		inputStack.pop();
		pathStack.pop();
	}

	private void addKVPCollection(String key, Collection values) {
		StringBuilder output = outputStack.peek();
		if (output.length() > 0) {
			output.append(',');
		}
		StringBuilder tmpVal = new StringBuilder();
		Iterator iter = values.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof Boolean || obj instanceof Number) {
				tmpVal.append(obj);
			} else {
				tmpVal.append('"').append(jsonEscape(String.valueOf(obj))).append('"');
			}
			if (iter.hasNext()) {
				tmpVal.append(',');
			}
		}
		if (key != null) {
			if (sourceCaseFormat != null) {
				key = sourceCaseFormat.to(targetCaseFormat, key);
			}
			output.append(String.format("\"%s\":[%s]", key, tmpVal.toString()));
		} else {
			output.append(tmpVal.toString());
		}
	}

	private String jsonEscape(String val) {
		return val.trim().replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private void addKVP(Member member, Object rawValue) {
		Class memberType = member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
		String key = member instanceof Field ? ((Field) member).getName() : reflectionCache
				.getPropertyName((Method) member);
		StringBuilder output = outputStack.peek();
		if (output.length() > 0) {
			output.append(',');
		}
		if (rawValue != null && transformers.containsKey(memberType)) {
			Object value = transformers.get(memberType).transform(rawValue);
			addKVP(key, value);
		} else {
			addKVP(key, rawValue);
		}
	}

	private boolean isCollectionClass(Class memberClass) {
		return Collection.class.isAssignableFrom(memberClass);
	}

	/**
	 * @param propertyPath
	 *            a (list of comma separated) property path(s).
	 * @return this for fluent interface (builder pattern)
	 */
	public JSONBuilder include(String... paths) {
		for (String rawPath : paths) {
			includeSingle(rawPath, null);
		}
		return this;
	}

	private JSONBuilder includeSingle(String rawPath, PropertyTransposition transposition) {
		if (rawPath.contains(",")) {
			throw new IllegalArgumentException("Property path '" + rawPath
					+ "' contains invalid comma character. Please specify paths as a 'vararg' parameters");
		}
		String path = rawPath;
		Map<String, Object> pathObject = includePaths;
		while (path.contains(".")) {
			String parent = path.substring(0, path.indexOf('.'));
			String rest = path.substring(path.indexOf('.') + 1);
			Map<String, Object> nested = (Map<String, Object>) pathObject.get(parent);
			if (nested == null) {
				nested = new HashMap<String, Object>();
				pathObject.put(parent, nested);
			}
			path = rest;
			pathObject = nested;
		}
		pathObject.put(path, transposition);
		return this;
	}

	public JSONBuilder withFilter(String path, Filter filter) {
		filters.put(path, filter);
		return this;
	}

	/**
	 * Wraps the resulting JSON object in another object, having one attributed with the name specified by the rootScope
	 * argument.
	 * 
	 * @param name
	 *            name of the JSON attribute in the wrapping JSON object.
	 * @return this for fluent interface (builder pattern)
	 */
	public JSONBuilder withRootScope(String name) {
		this.rootScope = name;
		return this;
	}

	/**
	 * Include a property from the source object tree on another location, with respect to collections (you can't put a
	 * collection child attribute in a parent attribute because there are multiple childs).
	 * 
	 * @param sourcePath
	 *            e.g. parent.children.location.name => { "children": [ { "location": { "name" : "value" } } ] }
	 * @param destinationPath
	 *            e.g. parent.children.location_name => { "children": [ { "location_name": "value" } ] }
	 * @return
	 */
	public JSONBuilder includeTransposed(String sourcePath, String destinationPath) {
		includeSingle(destinationPath, new PropertyTransposition(sourcePath));
		return this;
	}

	public JSONBuilder includeStringDate(String... paths) {
		for (String rawPath : paths) {
			includeSingle(rawPath, new PropertyTransposition(rawPath, true));
		}
		return this;
	}

	public ReferenceValidator getValidator() {
		return new ReferenceValidator(includePaths);
	}
}