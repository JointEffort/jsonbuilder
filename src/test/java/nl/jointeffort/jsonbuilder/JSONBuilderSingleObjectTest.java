package nl.jointeffort.jsonbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.jointeffort.jsonbuilder.CalendarTransformer;
import nl.jointeffort.jsonbuilder.JSONBuilder;
import nl.jointeffort.jsonbuilder.model.test.Professional;
import nl.jointeffort.jsonbuilder.model.test.UserState;
import nl.jointeffort.jsonbuilder.model.test.UserType;

import org.junit.Test;

import com.google.common.base.CaseFormat;

public class JSONBuilderSingleObjectTest extends AbstractJsonBuilderTest {

	@Test
	public void testSingleObjectSerializationWithoutIncludes() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		JSONBuilder json = new JSONBuilder();
		String s = json.serialize(prof);
		assertValidJson(s);
		assertEquals("{}", s);
	}

	@Test
	public void testSingleObjectSerializationWithNullValues() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, null);
		JSONBuilder json = new JSONBuilder().include("id","emailAddress");

		String s = json.serialize(prof);
		assertValidJson(s);
		assertEquals("{\"id\":1,\"emailAddress\":null}", s);
	}

	@Test
	public void testSingleObjectSerializationWithBoolenValues() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, null);
		prof.trueFlag = Boolean.TRUE;
		JSONBuilder json = new JSONBuilder().include("id","trueFlag","falseFlag");

		String s = json.serialize(prof);
		assertValidJson(s);
		assertEquals("{\"id\":1,\"trueFlag\":true,\"falseFlag\":false}", s);
	}

	@Test
	public void testSingleObjectSerializationWithSimpleIncludes() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		JSONBuilder json = new JSONBuilder().include("state", "id", "type", "emailAddress");
		String s = json.serialize(prof);
		assertValidJson(s);
		assertContainsKVP(s, "state", "Silver");
		assertContainsKVP(s, "id", 1);
		assertContainsKVP(s, "type", "Freelancer");
		assertContainsKVP(s, "emailAddress", "anonymous@hfs.nl");
	}

	@Test
	public void testSingleObjectSerializationToWriter() throws IOException {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		JSONBuilder json = new JSONBuilder().include("state", "id", "type", "emailAddress");

		StringWriter writer = new StringWriter();
		json.serialize(prof, writer);
		String writerString = writer.toString();
		assertValidJson(writerString);
		String directString = json.serialize(prof);
		assertEquals(writerString, directString);
	}


	@Test
	public void testSingleObjectSerializationWithCaseConversion() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		JSONBuilder json = new JSONBuilder(CaseFormat.LOWER_CAMEL, CaseFormat.LOWER_UNDERSCORE).include("state", "id", "type", "emailAddress");
		String s = json.serialize(prof);
		assertValidJson(s);
		assertContainsKVP(s, "state", "Silver");
		assertContainsKVP(s, "id", 1);
		assertContainsKVP(s, "type", "Freelancer");
		assertContainsKVP(s, "email_address", "anonymous@hfs.nl");
	}

	@Test
	public void testSingleObjectSerializationWithDefaultTypeTransformer() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		prof.accountCreationDate = Calendar.getInstance();
		JSONBuilder json = new JSONBuilder().include("state", "accountCreationDate");
		String s = json.serialize(prof);
		assertValidJson(s);
		assertContainsKVP(s, "state", "Silver");
		assertContainsKVP(s, "accountCreationDate", prof.accountCreationDate.getTimeInMillis());
	}

	@Test
	public void testSingleObjectSerializationWithCustomTypeTransformer() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		prof.accountCreationDate = Calendar.getInstance();
		JSONBuilder json = new JSONBuilder().include("state", "accountCreationDate");
		json.withTransformer(Calendar.class, new CalendarTransformer(
				"dd-MM-yyyy"));
		String s = json.serialize(prof);
		assertValidJson(s);
		assertContainsKVP(s, "state", "Silver");
		assertContainsKVP(s, "accountCreationDate", new SimpleDateFormat("dd-MM-yyyy").format(prof.accountCreationDate.getTime()));
	}

	@Test
	public void testWithRootObject() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, "anonymous@hfs.nl");
		JSONBuilder json = new JSONBuilder().include("id","state");
		String s = json.withRootScope("user").serialize(prof);
		assertValidJson(s);
		String expected = "{\"user\":{\"id\":1,\"state\":\"Silver\"}}";
		assertEquals(expected, s);
	}

	@Test
	public void testIllegalInclude() {
		try {
			new JSONBuilder().include("id, state");
			fail("IllegalArgumentException expected on include path");
		} catch (IllegalArgumentException e) {
			// OK
		}

	}
}
