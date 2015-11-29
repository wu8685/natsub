package com.natsub.ws;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.websocket.Session;

import org.junit.Test;

import com.natsub.ws.NatsHandler;
import com.natsub.ws.Topic;
import com.natsub.ws.TopicManager;

public class TopicManagerTest {

	protected TopicManager manager = new TopicManager("nat servers");
	
	public TopicManagerTest() {
		manager.handlerFactory = new MockFactory();
	}
	
	@Test
	public void registerTopic() {
		Session s = new MockSession();
		Topic t = new Topic("topic", "1");

		manager.subscribeTopic(t, s);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		
		manager.unsubscribeTopic(t, s);
		assertEquals(0, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t));
	}
	
	@Test
	public void registerTopicByDiffSession() {
		Session s1 = new MockSession();
		Session s2 = new MockSession();
		Topic t = new Topic("topic", "1");

		manager.subscribeTopic(t, s1);
		manager.subscribeTopic(t, s2);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(2, manager.handlers.get(t).sessions.size());
		
		manager.unsubscribeTopic(t, s1);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(1, manager.handlers.get(t).sessions.size());
		
		manager.unsubscribeTopic(t, s1);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(1, manager.handlers.get(t).sessions.size());
		
		manager.unsubscribeTopic(t, s2);
		assertEquals(0, manager.handlers.size());
	}
	
	@Test
	public void registerDiffTopic() {
		Session s = new MockSession();
		Topic t1 = new Topic("topicA", "1");
		Topic t2 = new Topic("topicB", "1");

		manager.subscribeTopic(t1, s);
		manager.subscribeTopic(t2, s);
		assertEquals(2, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t1));
		assertEquals(true, manager.handlers.containsKey(t2));
		
		manager.unsubscribeTopic(t1, s);
		assertEquals(1, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t1));
		assertEquals(true, manager.handlers.containsKey(t2));
		
		manager.unsubscribeTopic(t2, s);
		assertEquals(0, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t1));
		assertEquals(false, manager.handlers.containsKey(t2));
	}
	
	@Test
	public void unregisterAllTopic() {
		Session s = new MockSession();
		Topic t1 = new Topic("topicA", "1");
		Topic t11 = new Topic("topicA.1", "1");
		Topic t2 = new Topic("topicB", "2");
		
		manager.subscribeTopic(t1, s);
		manager.subscribeTopic(t11, s);
		manager.subscribeTopic(t2, s);
		
		Session s2 = new MockSession();
		manager.subscribeTopic(t1, s2);
		
		assertEquals(3, manager.handlers.size());
		manager.unsubscribeAllTopic(s);
		assertEquals(1, manager.handlers.size());
	}
	
	@Test
	public void unregisterTypeTopic() {
		Session s = new MockSession();
		Topic t1 = new Topic("topicA", "1");
		Topic t11 = new Topic("topicA.1", "1");
		Topic t2 = new Topic("topicB", "2");
		
		manager.subscribeTopic(t1, s);
		manager.subscribeTopic(t11, s);
		manager.subscribeTopic(t2, s);
		
		assertEquals(3, manager.handlers.size());
		t1 = new Topic("", new String("1"));
		manager.unsubscribeTypeTopic(t1, s);
		assertEquals(1, manager.handlers.size());
	}
}

class MockHandler extends NatsHandler {
	
	public MockHandler(Topic topic) {
		super(topic, new NatsManager("nat servers"));
	}

	@Override
	public void subscribe() throws IOException {
	}

	@Override
	public void unsubscribe() throws IOException {
	}
}

class MockFactory extends HandlerFactory {

	public MockFactory() {
		super("nat servers");
	}

	@Override
	protected void initHandlerClasses() {
		handlerClasses.put("1", MockHandler.class);
		handlerClasses.put("2", MockHandler.class);
	}
	
}
