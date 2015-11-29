package com.natsub.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Topic {
	
	private static Logger log = Logger.getLogger(Topic.class);
	
	public static Topic[] buildTopic(Map<String, String> params) {
		if (! params.containsKey("type")) {
			log.info("NATS info: No topic type being specified");
			return new Topic[]{};
		}
		
		String type = params.get("type");
		TopicType t = TopicType.getType(type);
		if (t == null) {
			log.info("NATS info: No matched topic for type: " + params.get("type"));
			return new Topic[]{};
		}
		
		if (params.containsKey("id")) {
			String[] ids = splitString(params.get("id"));
			List<Topic> result = new ArrayList<Topic>();
			for (String id : ids) {
				result.add(new Topic(t.prefix + id, type));
			}
			return result.toArray(new Topic[0]);
		}
		return new Topic[] { new Topic(t.prefix, t.type) };
	}

	protected static String[] splitString(String str) {
		String[] splited = str.split(",");
		List<String> result = new ArrayList<String>();
		for (String s : splited) {
			s = s.trim();
			if (s.length() > 0) {
				result.add(s);
			}
		}
		return result.toArray(new String[0]);
	}

	public String topic;
	public String type;
	
	public Topic(String topic, String type) {
		this.topic = topic;
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return topic.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Topic)) {
			return false;
		}
		Topic other = (Topic) obj;
		return this.topic.equals(other.topic);
	}

	@Override
	public String toString() {
		return topic;
	}
}

enum TopicType {
	
	EXAMPLE_TOPIC("1", "topic.example.prefix.", NatsHandler.class);
	
	public static TopicType getType(String type) {
		for (TopicType t : TopicType.values()) {
			if (t.type.equals(type)) {
				return t;
			}
		}
		return null;
	}
	
	protected String type;
	protected String prefix;
	protected Class<?> clazz;
	
	private TopicType(String type, String prefix, Class<?> clazz) {
		this.type = type;
		this.prefix = prefix;
		this.clazz = clazz;
	}
}