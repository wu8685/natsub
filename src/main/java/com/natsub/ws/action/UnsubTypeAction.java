package com.natsub.ws.action;

import java.util.Map;

import javax.websocket.Session;

import com.natsub.ws.Topic;
import com.natsub.ws.TopicManager;

public class UnsubTypeAction extends TopicAction {

	public UnsubTypeAction(TopicManager manager) {
		super(manager);
	}

	@Override
	public void doAction(Session session, Map<String, String> params) {
		if (! params.containsKey("type")) {
			return;
		}
		Topic topic = new Topic("", params.get("type"));
		manager.unsubscribeTypeTopic(topic, session);
	}

	@Override
	public String getName() {
		return "unsubscribeType";
	}

}
