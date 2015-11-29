package com.natsub.ws;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.nats.MsgHandler;

import com.google.gson.JsonObject;
import com.natsub.ws.NatsManager;

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
	
	public void register(TopicManager manager, Session session) throws IOException {
		log.info("NATS info: Register topic " + topic.topic + ", web socket session: " + session.getId());
		sessions.add(session);
		if (! manager.handlers.containsKey(topic)) {
			manager.handlers.put(topic, this);
			subscribe();
		}
	}
	
	public void disregister(TopicManager manager, Session session) throws IOException {
		log.info("NATS info: Disregister topic " + topic.topic + ", web socket session: " + session.getId());
		sessions.remove(session);
		if (sessions.size() == 0) {
			manager.handlers.remove(topic);
			unsubscribe();
		}
	}
	
	protected void subscribe() throws IOException {
		subId = nats.getConnection().subscribe(topic.topic, this);
	}

	protected void unsubscribe() throws IOException {
		if (subId == null || subId == -1) {
			return;
		}
		nats.getConnection().unsubscribe(subId);
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
				session.getBasicRemote().sendText(result.toString());
			} catch (Exception e) {
				log.error(e.getLocalizedMessage());
			}
		}
	}
}
