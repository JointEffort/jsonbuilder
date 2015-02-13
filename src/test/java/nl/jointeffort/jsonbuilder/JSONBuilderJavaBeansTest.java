package nl.jointeffort.jsonbuilder;

import static org.junit.Assert.assertEquals;
import nl.jointeffort.jsonbuilder.JSONBuilder;
import nl.jointeffort.jsonbuilder.model.test.Professional;
import nl.jointeffort.jsonbuilder.model.test.UserState;
import nl.jointeffort.jsonbuilder.model.test.UserType;

import org.junit.Test;

public class JSONBuilderJavaBeansTest extends AbstractJsonBuilderTest {

	@Test
	public void testSingleObjectSerializationJavaBeanProps() {
		Professional prof = new Professional(UserState.Silver, 1L, UserType.Freelancer, null);
		prof.name = "Achteren";
		prof.middleName = "naar";
		prof.firstName = "Vanvoor";
		assertEquals("Vanvoor naar Achteren", prof.getFullName());
		JSONBuilder json = new JSONBuilder().include("id","fullName","nerd","looser","success");
		
		String s = json.serialize(prof);
		assertValidJson(s);
		assertContainsKVP(s, "id", 1);
		assertContainsKVP(s, "fullName", "Vanvoor naar Achteren");
		assertContainsKVP(s, "nerd", true);
		assertContainsKVP(s, "looser", false);
		assertContainsKVP(s, "success", true);
	}


}
