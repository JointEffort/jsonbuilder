package nl.jointeffort.jsonbuilder;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionCache {

	private static final Logger LOG = LoggerFactory.getLogger(ReflectionCache.class);

	private Map<Class<?>, Map<String, Member>> membersByClass = new HashMap<Class<?>, Map<String, Member>>();

	public void ensureClassIsReflected(Class<?> clazz) {
		if (!membersByClass.containsKey(clazz)) {
			reflectClass(clazz, null);
		}
	}

	public void reflectClass(Class<?> aClass, Map<String, Member> members) {
		if (members == null) {
			members = new HashMap<String, Member>();
			membersByClass.put(aClass, members);
		}
		// First go to superclass
		if (aClass.getSuperclass() != null) {
			reflectClass(aClass.getSuperclass(), members);
		}

		for (Method method : aClass.getDeclaredMethods()) {
			if (isJavaBeanGetter(method) && !method.isSynthetic()) {
				String propertyName = getPropertyName(method);
				method.setAccessible(true);
				members.put(propertyName, method);
			}
		}

		for (Field field : aClass.getDeclaredFields()) {
			if (!isStatic(field) && !field.isSynthetic()) {
				field.setAccessible(true);
				members.put(field.getName(), field);
			}
		}

	}

	private Map<String, Member> get(Class<?> clazz) {
		return membersByClass.get(clazz);
	}

	public Object getMemberValue(Member member, Object currentObject) {
		try {
			if (member instanceof Method) {
				return ((Method) member).invoke(currentObject, new Object[] {});
			} else {
				return ((Field) member).get(currentObject);
			}
		} catch (Exception e) {
			LOG.debug("Encountered NPE while retrieving value from " + member.getName() + " on " + currentObject, e);
			return null;
		}
	}

	public Member getMember(Class<?> clazz, String name) {
		ensureClassIsReflected(clazz);
		return get(clazz).get(name);
	}
	
	public Member getMember(Object object, String name) {
		Member field = getMember(object.getClass(), name);
		if (field == null) {
			throw new NullPointerException("Member " + name + " not found as field or method on object of class "
					+ object.getClass());
		}
		return field;
	}

	private boolean isJavaBeanGetter(Method method) {
		if (isStatic(method)) {
			return false;
		}
		if (method.getReturnType().equals(Void.class)) {
			return false;
		}
		if (method.getParameterTypes().length > 0) {
			return false;
		}
		if (method.getName().startsWith("get")) {
			return true;
		}
		if ((method.getName().startsWith("is") || method.getName().startsWith("has"))
				&& (method.getReturnType().equals(Boolean.class) || Boolean.TYPE.equals(method.getReturnType()))) {
			return true;
		}
		return false;
	}

	private boolean isStatic(Member member) {
		return Modifier.isStatic(member.getModifiers());
	}

	public String getPropertyName(Method method) {
		if (method.getName().startsWith("is")) {
			return Introspector.decapitalize(method.getName().substring(2));
		} else if (method.getName().startsWith("get") || method.getName().startsWith("has")) {
			return Introspector.decapitalize(method.getName().substring(3));
		} else {
			throw new IllegalArgumentException("Method " + method + " is not a valid JavaBean getter!");
		}
	}

	public boolean isSimpleType(Object object) {
		Class<?> clazz = object.getClass();
		return clazz.isPrimitive() || clazz == String.class || object instanceof Number || object instanceof Boolean;
	}

}
