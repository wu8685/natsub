package com.natsub.ws.action;

import java.util.Map;

import javax.websocket.Session;

import com.natsub.ws.Topic;
import com.natsub.ws.TopicManager;

public class UnsubAction extends TopicAction {

	public UnsubAction(TopicManager manager) {
		super(manager);
	}

	@Override
	public void doAction(Session session, Map<String, String> params) {
		for (Topic topic : getTopics(params)) {
			manager.unsubscribeTopic(topic, session);
		}
	}

	@Override
	public String getName() {
		return "unsubscribe";
	}

}
