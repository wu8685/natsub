package com.natsub.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.natsub.ws.action.SubAction;
import com.natsub.ws.action.UnsubAction;
import com.natsub.ws.action.UnsubAllAction;
import com.natsub.ws.action.UnsubTypeAction;

public class MessageProcessor {

	private static Logger log = Logger.getLogger(MessageProcessor.class);
	
	protected List<Action> actions = new ArrayList<Action>();
	
	protected TopicManager manager;
	
	public MessageProcessor(String natsServer) {
		this.manager = new TopicManager(natsServer);
		initActions(manager);
	}
	
	protected void initActions(TopicManager manager) {
		actions.add(new SubAction(manager));
		actions.add(new UnsubAction(manager));
		actions.add(new UnsubAllAction(manager));
		actions.add(new UnsubTypeAction(manager));
	}
	
	public void onMessage(String message, Session session) {
		log.info("NATS info: msg processor process message: " + message);
		
		Map<String, String> params = parse(message);
		
		if (params.containsKey("health_check")) {
			try {
				session.getBasicRemote().sendText("health_check");
			} catch (IOException e) {
				log.error(e.getLocalizedMessage());
			}
			return;
		}
		
		String action = params.get("action");
		if (action == null || action.length() == 0) {
			return;
		}
		for (Action a : actions) {
			if (action.equals(a.getName())) {
				a.doAction(session, params);
				break;
			}
		}
	}
	
	public void onClose(Session session, CloseReason reason) {
		log.info("NATS info: NATS msg processor on close: " + reason.getReasonPhrase());
		log.info("NATS info: Unsubscribe all topic.");
		manager.unsubscribeAllTopic(session);
	}
	
	public void onError(Session session, Throwable e) {
		log.error("NATS info: msg processor on error: " + e.getMessage());
	}
	
	protected Map<String, String> parse(String message) {
		String[] pairs = message.split("&");
		Map<String, String> params = new HashMap<String, String>();
		for (String pair : pairs) {
			if (pair.indexOf("=") == -1 || pair.lastIndexOf("=") != pair.indexOf("=")) {
				log.error("NATS info: The message parameter format is not correct.");
				continue;
			}
			String[] keyValue = pair.split("=");
			if (keyValue[0].trim().length() == 0) {
				log.error("NATS info: The message parameter format is not correct.");
				continue;
			}
			params.put(keyValue[0].trim(), keyValue.length > 1 ? keyValue[1].trim() : "");
		}
		return params;
	}
	
}
