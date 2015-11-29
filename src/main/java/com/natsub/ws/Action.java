package com.natsub.ws;

import java.util.Map;

import javax.websocket.Session;

public interface Action {

	public void doAction(Session session, Map<String, String> params);
	
	public String getName();
}
