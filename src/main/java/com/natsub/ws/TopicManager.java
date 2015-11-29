package com.natsub.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import org.apache.log4j.Logger;

public class TopicManager {
	
	private static Logger log = Logger.getLogger(TopicManager.class);
	
	protected Map<Topic, NatsHandler> handlers = new HashMap<Topic, NatsHandler>();
	protected HandlerFactory handlerFactory;
	
	public TopicManager(String natsServer) {
		this.handlerFactory = new HandlerFactory(natsServer);
	}
	
	public void subscribeTopic(Topic topic, Session session) {
		log.info("NATS info: Subscribe topic:" + topic + " for session id:" + session.getId());

		NatsHandler handler = null;
		try {
			if (handlers.containsKey(topic)) {
				handler = handlers.get(topic);
			} else {
				handler = handlerFactory.create(topic);
			}
			handler.register(this, session);
		} catch (Exception e) {
			log.warn("NATS info: Fail to subscribe topic " + topic + " for session id" + session.getId(), e);
			return;
		}
	}
	
	public void unsubscribeTopic(Topic topic, Session session) {
		log.info("NATS info: Unsubscribe topic:" + topic + " for session id:" + session.getId());
		
		if (handlers.containsKey(topic)) {
			NatsHandler handler = handlers.get(topic);
			try {
				handler.disregister(this, session);
			} catch (IOException e) {
				log.warn("NATS info: Fail to unsubscribe topic " + topic + " for session id" + session.getId(), e);
				return;
			}
		}
	}
	
	public void unsubscribeAllTopic(Session session) {
		log.info("NATS info: Unsubscribe all topics for session id:" + session.getId());
		
		Set<Topic> topics = new HashSet<Topic>(handlers.keySet());
		for (Topic t : topics) {
			try {
				handlers.get(t).disregister(this, session);
			} catch(Exception e) {
				log.warn("NATS info: Fail to unsubscribe topic " + t.topic + " for session id" + session.getId(), e);
			}
		}
	}
	
	public void unsubscribeTypeTopic(Topic topic, Session session) {
		log.info("NATS info: Unsubscribe all " + topic.type + " type topics for session id:" + session.getId());
		
		Set<Topic> topics = new HashSet<Topic>(handlers.keySet());
		for (Topic t : topics) {
			if (! t.type.equals(topic.type)) {
				continue;
			}
			try {
				handlers.get(t).disregister(this, session);
			} catch(Exception e) {
				log.warn("NATS info: Fail to unsubscribe topic " + t.topic + " for session id" + session.getId(), e);
			}
		}
	}
}

class HandlerFactory {
	
	protected Map<String, Class<?>> handlerClasses = new HashMap<String, Class<?>>();
	protected String natsServers;
	
	public HandlerFactory(String natsServer) {
		this.natsServers = natsServer;
		initHandlerClasses();
	}
	
	protected void initHandlerClasses() {
		for (TopicType t : TopicType.values()) {
			handlerClasses.put(t.type, t.clazz);
		}
	}

	public NatsHandler create(Topic topic) throws Exception {
		Class<?> clazz = searchHandlerClasses(topic.type);
		if (clazz != null) {
			return (NatsHandler) clazz.getDeclaredConstructor(Topic.class).newInstance(topic);
		}
		throw new Exception("NATS info: Fail to find Nats handler for topic:" + topic);
	}
	
	protected Class<?> searchHandlerClasses(String type) {
		if (handlerClasses.containsKey(type)) {
			return handlerClasses.get(type);
		}
		return null;
	}
}