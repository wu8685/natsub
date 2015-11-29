package com.natsub.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.websocket.Session;

import org.junit.Test;

import com.natsub.ws.NatsHandler;
import com.natsub.ws.Topic;
import com.natsub.ws.TopicManager;

public class NatsHandlerTest {

	protected NatsHandler handler = new MockHandler(new Topic("test", "1"));
	protected MockTopicManager manager = new MockTopicManager();

	@Test
	public void registerSession() {
		try {
			Session s = new MockSession();
			handler.register(manager, s);
			assertEquals(1, handler.sessions.size());
			assertEquals(1, manager.handlerSize());

			handler.register(manager, s);
			assertEquals(1, handler.sessions.size());
			assertEquals(1, manager.handlerSize());

			handler.disregister(manager, s);
			assertEquals(0, handler.sessions.size());
			assertEquals(0, manager.handlerSize());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void registerSameTopic() {
		try {
			Session s1 = new MockSession();
			Session s2 = new MockSession();
			handler.register(manager, s1);
			handler.register(manager, s2);
			assertEquals(2, handler.sessions.size());
			assertEquals(1, manager.handlerSize());

			handler.disregister(manager, s1);
			assertEquals(1, handler.sessions.size());
			assertEquals(1, manager.handlerSize());

			handler.disregister(manager, s1);
			assertEquals(1, handler.sessions.size());
			assertEquals(1, manager.handlerSize());

			handler.disregister(manager, s2);
			assertEquals(0, handler.sessions.size());
			assertEquals(0, manager.handlerSize());
		} catch (Exception e) {
			fail();
		}
	}
}

class MockTopicManager extends TopicManager {

	public MockTopicManager() {
		super("");
	}

	public int handlerSize() {
		return handlers.size();
	}
}
