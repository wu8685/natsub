package com.natsub.ws;

import static org.junit.Assert.assertEquals;

import java.lang.ref.WeakReference;
import java.util.Map;

import javax.websocket.Session;

import org.junit.Test;

public class TopicManagerTest {

	protected TopicManager manager = new TopicManager("nat servers");
	
	public TopicManagerTest() {
		manager.handlerFactory = new MockFactory(manager.handlers);
	}
	
	public void gc() throws InterruptedException {
		int before = manager.handlers.size();
		for (int i = 0; i < 10; i ++) {
			System.gc();
			if (before != manager.handlers.size()) {
				return;
			}
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void registerTopic() throws InterruptedException {
		Session s = new MockSession();
		Topic t = new Topic("topic", "1");

		manager.subscribeTopic(t, s);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));

		manager.unsubscribeTopic(t, s);
		t = null;
		gc();
		manager.unsubscribeTopic(t, s);
		assertEquals(0, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t));
	}
	
	@Test
	public void registerTopicByDiffSession() throws InterruptedException {
		Session s1 = new MockSession();
		Session s2 = new MockSession();
		Topic t = new Topic("topic", "1");

		manager.subscribeTopic(t, s1);
		manager.subscribeTopic(t, s2);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(2, manager.handlers.get(t).get().sessions.size());
		
		manager.unsubscribeTopic(t, s1);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(1, manager.handlers.get(t).get().sessions.size());
		
		manager.unsubscribeTopic(t, s1);
		assertEquals(1, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t));
		assertEquals(1, manager.handlers.get(t).get().sessions.size());
		
		manager.unsubscribeTopic(t, s2);
		t = null;
		gc();
		assertEquals(0, manager.handlers.size());
	}
	
	@Test
	public void registerDiffTopic() throws InterruptedException {
		Session s = new MockSession();
		Topic t1 = new Topic("topicA", "1");
		Topic t2 = new Topic("topicB", "1");

		manager.subscribeTopic(t1, s);
		manager.subscribeTopic(t2, s);
		assertEquals(2, manager.handlers.size());
		assertEquals(true, manager.handlers.containsKey(t1));
		assertEquals(true, manager.handlers.containsKey(t2));
		
		manager.unsubscribeTopic(t1, s);
		t1 = null;
		gc();
		assertEquals(1, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t1));
		assertEquals(true, manager.handlers.containsKey(t2));
		
		manager.unsubscribeTopic(t2, s);
		t2 = null;
		gc();
		assertEquals(0, manager.handlers.size());
		assertEquals(false, manager.handlers.containsKey(t1));
		assertEquals(false, manager.handlers.containsKey(t2));
	}
	
	@Test
	public void unregisterAllTopic() throws InterruptedException {
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
		t1 = t11 = t2 = null;
		gc();
		assertEquals(1, manager.handlers.size());
	}
	
	@Test
	public void unregisterTypeTopic() throws InterruptedException {
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
		t1 = t11 = t2 = null;
		gc();
		assertEquals(1, manager.handlers.size());
	}
}

class MockHandler extends NatsHandler {
	
	public MockHandler(Topic topic) {
		super(topic, new NatsManager("nat servers"));
	}
	
	public MockHandler(Topic topic, NatsManager natsManager) {
		super(topic, natsManager);
	}

	@Override
	public void subscribe() {
		subId = 1;
		MockNatsServer.NATS.sub(topic.topic, this);
	}

	@Override
	public void unsubscribe() {
		subId = -1;
		MockNatsServer.NATS.unsub(topic.topic);
	}
}

class MockFactory extends HandlerFactory {

	public MockFactory(Map<Topic, WeakReference<NatsHandler>> handlers) {
		super("", handlers);
	}

	@Override
	protected void initHandlerClasses() {
		handlerClasses.put("1", MockHandler.class);
		handlerClasses.put("2", MockHandler.class);
	}
	
}
