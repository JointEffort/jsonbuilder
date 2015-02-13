package nl.jointeffort.jsonbuilder;

public interface Filter<T> {

	boolean include(T item);
	
}
