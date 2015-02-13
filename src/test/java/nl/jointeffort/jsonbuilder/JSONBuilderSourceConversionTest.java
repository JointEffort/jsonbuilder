package nl.jointeffort.jsonbuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.jointeffort.jsonbuilder.JSONBuilder;
import nl.jointeffort.jsonbuilder.model.test.Professional;
import nl.jointeffort.jsonbuilder.model.test.UserState;
import nl.jointeffort.jsonbuilder.model.test.UserType;

import org.junit.Test;

import com.google.common.base.CaseFormat;

public class JSONBuilderSourceConversionTest extends AbstractJsonBuilderTest {

	@Test
	public void testSingleObjectSerializationWithSimpleIncludes() throws Exception {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = "2014-01-21 14:34:12";
		Date date = format.parse(dateString);
		
		prof.lastModifiedDate = dateString;
		JSONBuilder json = new JSONBuilder(CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_UNDERSCORE).include("state", "id", "type", "emailAddress");
		json.includeStringDate("lastModifiedDate");
		String s = json.serialize(prof);
		assertValidJson(s);
		
		assertContainsKVP(s, "last_modified_date", date.getTime());
	}

}
