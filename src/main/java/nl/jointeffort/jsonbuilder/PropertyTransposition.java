package nl.jointeffort.jsonbuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class PropertyTransposition extends PropertyOp {

	private static DateFormat FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String sourcePath;

	private boolean sourceIsStringDate;
	
	public PropertyTransposition(String sourcePath) {
		this(sourcePath, false);
	}

	public PropertyTransposition(String sourcePath, boolean sourceIsStringDate) {
		this.sourcePath = sourcePath;
		this.sourceIsStringDate = sourceIsStringDate;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	@Override
	public Object getSourceValue(ReflectionCache reflectionCache, Object object, int depth) {
		String startVal = sourcePath;
		if (sourcePath.contains(".") && depth > 0) {
			startVal = startVal.substring(sourcePath.indexOf('.')+1);
		}
		Object sourceValue = internalGet(reflectionCache, startVal, object);
		return sourceIsStringDate ? convertFromStringDate(sourceValue) : sourceValue;
	}

	private Object convertFromStringDate(Object sourceValue) {
		if (sourceValue != null) {
			try {
				return FMT.parse(sourceValue.toString()).getTime();
			} catch (Exception e) {
				// Ignore and return original source value
			}
		}
		return sourceValue;
	}
	
}
