package nl.jointeffort.jsonbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class AbstractJsonBuilderTest {

	protected void assertContainsNotKVP(String json, String key, String value) {
		String sequence = String.format("\"%s\":\"%s\"", key, value);
		assertNotNull("JSON string is null", json);
		assertFalse(String.format("KVP <%s> not present in JSON <%s>", sequence, json), json.contains(sequence));
		
	}
	
	protected void assertContainsKVP(String json, String key, Object value) {
		String sequence = String.format("\"%s\":\"%s\"", key, value);
		if (value instanceof Boolean || value == null || value instanceof Number) {
			sequence = String.format("\"%s\":%s", key, value);
		}
		assertNotNull("JSON string is null", json);
		assertTrue(String.format("KVP <%s> not present in JSON <%s>", sequence, json), json.contains(sequence));
	}
	
	protected void assertValidJson(String test) {
	    try {
	        new JSONObject(test);
	    } catch (JSONException ex) {
	        // edited, to include @Arthur's comment
	        // e.g. in case JSONArray is valid as well...
	        try {
	            new JSONArray(test);
	        } catch (JSONException ex1) {
	            fail("Invalid JSON: " + test);
	        }
	    }
	}

}
