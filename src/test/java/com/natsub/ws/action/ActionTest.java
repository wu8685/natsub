package com.natsub.ws.action;

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.natsub.ws.MessageProcessor;
import com.natsub.ws.Topic;
import com.natsub.ws.action.SubAction;

public class ActionTest {

	protected MessageProcessor processor = new MessageProcessor("nats servers");
	
	@Test
	public void createTopic() {
		SubAction sub = new SubAction(null);
		Map<String, String> params = new HashMap<String, String>();
		
		assertArrayEquals(new String[]{}, sub.getTopics(params));
		
		params.put("id", "1");
		assertArrayEquals(new String[]{}, sub.getTopics(params));

		params.put("type", "1");
		assertArrayEquals(new Topic[]{new Topic("topic.example.prefix.1", "1")}, sub.getTopics(params));
		params.put("id", " 1 , 2 ");
		assertArrayEquals(new Topic[]{new Topic("topic.example.prefix.1", "1"), new Topic("topic.example.prefix.2", "1")}, sub.getTopics(params));
	}
}
