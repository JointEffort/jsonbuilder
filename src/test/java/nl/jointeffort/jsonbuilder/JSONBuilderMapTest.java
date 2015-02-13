package nl.jointeffort.jsonbuilder;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import nl.jointeffort.jsonbuilder.JSONBuilder;

import org.junit.Before;
import org.junit.Test;

public class JSONBuilderMapTest extends AbstractJsonBuilderTest {

	private Map<Object, Object> data;
	
	@Before
	public void setup() {
		data = new HashMap<Object, Object>();
		data.put("long", 1L);
		data.put("boolean", true);
		data.put("Boolean", Boolean.TRUE);
		data.put("string", "String");
		data.put("null", null);
	}
	
	@Test
	public void testSingleMapSerialization() {
		String s = new JSONBuilder().serialize(data);
		assertValidJson(s);
		assertEquals("{\"string\":\"String\",\"boolean\":true,\"Boolean\":true,\"long\":1,\"null\":null}", s);
	}
	
	@Test
	public void testNestedMapSerialization() {
		Map<Object, Object> child = new HashMap<Object, Object>();
		child.put("long", 1L);
		child.put("boolean", true);
		data.put("child", child);
		String s = new JSONBuilder().serialize(data);
		assertValidJson(s);
		assertEquals("{\"child\":{\"boolean\":true,\"long\":1},\"string\":\"String\",\"boolean\":true,\"Boolean\":true,\"long\":1,\"null\":null}", s);
	}
}
