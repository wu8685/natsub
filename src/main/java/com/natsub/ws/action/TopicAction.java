package com.natsub.ws.action;

import java.util.Map;

import com.natsub.ws.Action;
import com.natsub.ws.Topic;
import com.natsub.ws.TopicManager;

public abstract class TopicAction implements Action {
	
	protected TopicManager manager = null;
	
	public TopicAction(TopicManager manager) {
		this.manager = manager;
	}

	protected Topic[] getTopics(Map<String, String> params) {
		return Topic.buildTopic(params);
	}
	
}
