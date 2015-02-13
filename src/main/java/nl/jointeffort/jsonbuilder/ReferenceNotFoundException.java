package nl.jointeffort.jsonbuilder;

public class ReferenceNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReferenceNotFoundException(Class<?> clazz, String property) {
		super(String.format("Member '%s' not found on class %s", property, clazz));
	}

}
