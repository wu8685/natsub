package com.natsub.ws;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.websocket.Session;

import org.apache.log4j.Logger;

public class TopicManager {
	
	private static Logger log = Logger.getLogger(TopicManager.class);
	
	protected Map<Topic, WeakReference<NatsHandler>> handlers = new WeakHashMap<Topic, WeakReference<NatsHandler>>();
	protected HandlerFactory handlerFactory;
	
	public TopicManager(String natsServer) {
		this.handlerFactory = new HandlerFactory(natsServer, handlers);
	}
	
	public void subscribeTopic(Topic topic, Session session) {
		log.info("Topic: Subscribe topic:" + topic + " for session id:" + session.getId());

		NatsHandler handler = null;
		try {
			handler = handlerFactory.getHandler(topic);
			handler.register(session);
		} catch (Exception e) {
			log.error("Topic: Fail to subscribe topic " + topic + " for session id" + session.getId(), e);
			return;
		}
	}
	
	public void unsubscribeTopic(Topic topic, Session session) {
		log.debug("Topic: Unsubscribe all topics for session id:" + session.getId());
		
		Set<Topic> topics = new HashSet<Topic>(handlers.keySet());
		for (Topic t : topics) {
			try {
				NatsHandler handler = handlerFactory.getHandler(t);
				handler.disregister(session);
			} catch(Exception e) {
				log.error("Topic: Fail to unsubscribe topic " + t.topic + " for session id" + session.getId(), e);
			}
		}
	}
	
	public void unsubscribeAllTopic(Session session) {
		log.info("NATS info: Unsubscribe all topics for session id:" + session.getId());
		
		Set<Topic> topics = new HashSet<Topic>(handlers.keySet());
		for (Topic t : topics) {
			try {
				NatsHandler handler = handlerFactory.getHandler(t);
				handler.disregister(session);
			} catch(Exception e) {
				log.error("Topic: Fail to unsubscribe topic " + t.topic + " for session id" + session.getId(), e);
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
				NatsHandler handler = handlerFactory.getHandler(t);
				handler.disregister(session);
			} catch(Exception e) {
				log.error("Topic: Fail to unsubscribe topic " + t.topic + " for session id" + session.getId(), e);
			}
		}
	}
}

class HandlerFactory {
	
	protected Map<String, Class<?>> handlerClasses = new HashMap<String, Class<?>>();
	protected Map<Topic, WeakReference<NatsHandler>> handlers = null;
	protected NatsManager natsManager;
	
	public HandlerFactory(String natsServer, Map<Topic, WeakReference<NatsHandler>> handlers) {
		this.handlers = handlers;
		this.natsManager = new NatsManager(natsServer);
		
		initHandlerClasses();
	}
	
	protected void initHandlerClasses() {
		for (TopicType t : TopicType.values()) {
			handlerClasses.put(t.type, t.clazz);
		}
	}

	public NatsHandler getHandler(Topic topic) throws Exception {
		NatsHandler handler = fetchHandler(topic);
		if (handler == null) {
			return generateHandler(topic);
		}
		return handler;
	}
	
	protected synchronized NatsHandler generateHandler(Topic topic) throws Exception {
		NatsHandler handler = fetchHandler(topic);
		if (handler != null) {
			return handler;
		}
		Class<?> clazz = searchHandlerClasses(topic.type);
		if (clazz != null) {
			handler = (NatsHandler) clazz.getDeclaredConstructor(Topic.class, NatsManager.class).newInstance(topic, natsManager);
			handlers.put(topic, new WeakReference<NatsHandler>(handler));
			return handler;
		}
		throw new Exception("Topic info: Fail to find Nats handler for topic:" + topic);
	}
	
	protected NatsHandler fetchHandler(Topic topic) {
		WeakReference<NatsHandler> ref = handlers.get(topic);
		if (ref == null) {
			return null;
		}
		return ref.get();
	}
	
	protected Class<?> searchHandlerClasses(String type) {
		if (handlerClasses.containsKey(type)) {
			return handlerClasses.get(type);
		}
		return null;
	}
}