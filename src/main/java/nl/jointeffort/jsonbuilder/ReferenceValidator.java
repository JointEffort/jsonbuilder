package nl.jointeffort.jsonbuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class ReferenceValidator {

	private ReflectionCache reflectionCache;
	private Map<String, Object> paths;
	private Class<?> rootClass;

	public ReferenceValidator(Map<String, Object> paths) {
		this.paths = paths;
	}

	public void assertValidForClass(Class<?> rootClass) throws ReferenceNotFoundException {
		this.reflectionCache = new ReflectionCache();
		this.rootClass = rootClass;
		assertValidMap(paths, rootClass);
	}

	@SuppressWarnings("unchecked")
	private void assertValidMap(Map<String, Object> context, Class<?> clazz) throws ReferenceNotFoundException {
		reflectionCache.ensureClassIsReflected(clazz);
		for (String path : context.keySet()) {
			Object pathValue = context.get(path);
			if (pathValue == null) {
				assertPropertyExist(path, clazz);
			} else if (pathValue instanceof Map) {
				Class<?> targetClass = getClassOfProperty(clazz, path);
				if (targetClass != null) {
					assertValidMap((Map<String, Object>) pathValue, targetClass);
				}
			} else if (pathValue instanceof PropertyTransposition) {
				String subPath = ((PropertyTransposition) pathValue).getSourcePath();
				assertValidSourcePath(subPath, rootClass);
			}
		}
	}

	private Class<?> getClassOfProperty(Class<?> clazz, String property) throws ReferenceNotFoundException {
		Member member = reflectionCache.getMember(clazz, property);
		Class<?> memberType = null;
		if (member instanceof Field) {
			memberType = ((Field) member).getType();
			if (Collection.class.isAssignableFrom(memberType)) {
				memberType = getElementType(((Field) member).getGenericType());
			}
		} else if (member instanceof Method) {
			memberType = ((Method) member).getReturnType();
			if (Collection.class.isAssignableFrom(memberType)) {
				memberType = getElementType(((Method) member).getGenericReturnType());
			}
		} else {
			throw new ReferenceNotFoundException(clazz, property);
		}

		return memberType;
	}

	private Class<?> getElementType(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) type;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			Type retType = fieldArgTypes[0];
			return retType instanceof Class ? (Class<?>) retType : null;
		} else {
			throw new IllegalStateException("Something went wrong....");
		}
	}

	private void assertValidSourcePath(String sourcePath, Class<?> clazz) throws ReferenceNotFoundException {
		if (sourcePath.contains(".")) {
			String first = sourcePath.substring(0, sourcePath.indexOf('.'));
			String rest = sourcePath.substring(sourcePath.indexOf('.') + 1);
			Class<?> propertyType = getClassOfProperty(clazz, first);
			assertValidSourcePath(rest, propertyType);
		} else {
			assertPropertyExist(sourcePath, clazz);
		}
	}

	private void assertPropertyExist(String property, Class<?> clazz) throws ReferenceNotFoundException {
		if (reflectionCache.getMember(clazz, property) == null) {
			throw new ReferenceNotFoundException(clazz, property);
		}
	}


}
