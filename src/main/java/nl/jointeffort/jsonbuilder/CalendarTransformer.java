package nl.jointeffort.jsonbuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarTransformer implements Transformer {

	private DateFormat fmt;

	public CalendarTransformer() {
	}

	public CalendarTransformer(String dateFormat) {
		fmt = new SimpleDateFormat(dateFormat);
	}

	@Override
	public Object transform(Object value) {
		if (value instanceof Calendar) {
			if (fmt == null) {
				return value == null ? null : ((Calendar) value).getTimeInMillis();
			} else {
				return value == null ? "" : fmt.format(((Calendar) value).getTime());
			}
		} else {
			throw new IllegalArgumentException("Type conversion not possible for " + value);
		}
	}

}
