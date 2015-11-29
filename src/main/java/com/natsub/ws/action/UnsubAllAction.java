package com.natsub.ws.action;

import java.util.Map;

import javax.websocket.Session;

import com.natsub.ws.TopicManager;

public class UnsubAllAction extends TopicAction {

	public UnsubAllAction(TopicManager manager) {
		super(manager);
	}

	@Override
	public void doAction(Session session, Map<String, String> params) {
		manager.unsubscribeAllTopic(session);
	}

	@Override
	public String getName() {
		return "unsubscribeAll";
	}

}
