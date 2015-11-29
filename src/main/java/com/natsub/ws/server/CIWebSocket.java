package com.natsub.ws.server;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.natsub.ws.MessageProcessor;

@ServerEndpoint(value = "/ws/wsservice")
public class CIWebSocket {

	private static Logger log = Logger.getLogger(CIWebSocket.class);
	
	// parameter should be a string consist of nats server ips which is joined by ';'
	protected static MessageProcessor processor = new MessageProcessor("1.1.1.1;2.2.2.2");
	
	@OnOpen
	public void onOpen(Session session) {
		log.info("Client connected, session ID: " + session.getId());
	}
	
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("Client send: " + message);
		processor.onMessage(message, session);
	}
	
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info("Client disconnected, session ID: " + session.getId());
		processor.onClose(session, reason);
	}
	
	@OnError
	public void onError(Session session, Throwable e) {
		log.error("Error occurred! session ID: " + session.getId());
		processor.onError(session, e);
	}
}
