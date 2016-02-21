package com.natsub.ws;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nats.MsgHandler;

import com.google.gson.JsonObject;

public class NatsHandler extends MsgHandler {
	
	private static Logger log = Logger.getLogger(NatsHandler.class);
	
	protected Topic topic = null;
	protected Integer subId = -1;
	protected Set<Session> sessions = new HashSet<Session>();
	protected NatsManager nats = null;
	
	public NatsHandler(Topic topic, NatsManager natsManager) {
		this.topic = topic;
		this.nats = natsManager;
	}
	
	public synchronized void register(Session session) {
		log.info("NATS: Register topic " + topic.topic + ", web socket session: " + session.getId());
		sessions.add(session);
		if (! hasSubscribed()) {
			subscribe();
		}
	}
	
	public synchronized void disregister(Session session) {
		log.info("NATS: Disregister topic " + topic.topic + ", web socket session: " + session.getId());
		if (!sessions.remove(session)) {
			return;
		}
		if (sessions.size() == 0 && hasSubscribed()) {
			unsubscribe();
		}
	}
	
	protected void subscribe() {
		try {
			subId = nats.getConnection().subscribe(topic.topic, this);
			log.info("NATS: subscribe topic " + topic.topic);
		} catch (IOException e) {
			log.error("NATS: fail to subscribe Nats topic: " + topic.topic);
		}
	}

	protected void unsubscribe() {
		if (subId == null || subId == -1) {
			return;
		}
		try {
			nats.getConnection().unsubscribe(subId);
			subId = -1;
			log.info("NATS: unsubscribe topic " + topic.topic);
		} catch (IOException e) {
			log.error("NATS: fail to unsubscribe Nats topic: " + topic.topic);
		}
	}
	
	protected boolean hasSubscribed() {
		return subId != -1;
	}

	@Override
	public void execute(String message) {
		for (Session session : sessions) {
			try {
				String decodeMessage = new String(message.getBytes(), "UTF-8");
				log.info("NATS info: Server Response to session: " + session.getId() + "----"
						+ decodeMessage);
				
				JsonObject result = new JsonObject();
				result.addProperty("message", decodeMessage);
				result.addProperty("type", this.topic.type);
				
				synchronized(session) {
					session.getBasicRemote().sendText(result.toString());
				}
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}
}
