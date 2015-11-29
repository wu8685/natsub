package com.natsub.ws;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Test;
import com.natsub.ws.MessageProcessor;

public class MessageProcessorTest {

	protected MessageProcessor processor = new MessageProcessor("nat servers");
	
	@Test
	public void parseParams() {
		String message = "a=b";
		Map<String, String> params = processor.parse(message);
		assertEquals("b", params.get("a"));
		assertEquals(null, params.get("other"));

		message = "&a=b&";
		params = processor.parse(message);
		assertEquals("b", params.get("a"));

		message = "a=b&c=d";
		params = processor.parse(message);
		assertEquals("b", params.get("a"));
		assertEquals("d", params.get("c"));
		
		message = " a = b & c = d ";
		params = processor.parse(message);
		assertEquals("b", params.get("a"));
		assertEquals("d", params.get("c"));
		
		message = "a=b&c=";
		params = processor.parse(message);
		assertEquals("b", params.get("a"));
		assertEquals("", params.get("c"));
		
		message = "a=bc=&x=y";
		params = processor.parse(message);
		assertEquals(false, params.containsKey("a"));
		assertEquals("y", params.get("x"));
		
		message = "=b";
		params = processor.parse(message);
		assertEquals(0, params.size());
	}
}
