package nl.jointeffort.jsonbuilder;

import java.lang.reflect.Member;

public abstract class PropertyOp {

	public abstract Object getSourceValue(ReflectionCache reflectionCache, Object object, int depth);

	protected Object internalGet(ReflectionCache reflectionCache, String path, Object currentObject) {
		if (currentObject == null) {
			return null;
		} else if (path.contains(".")) {
			String propName = path.substring(0, path.indexOf('.'));
			String rest = path.substring(path.indexOf('.')+1);
			Object propValue = getProp(reflectionCache, currentObject, propName);
			return internalGet(reflectionCache, rest, propValue);
		} else {
			return getProp(reflectionCache, currentObject, path);
		}
	}

	protected Object getProp(ReflectionCache reflectionCache, Object object, String property) {
		Member member = reflectionCache.getMember(object, property);
		return reflectionCache.getMemberValue(member, object);
	}

}