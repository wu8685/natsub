# natsub
A NATS subscribe api used on server

By creating a MessageProcessor with a NATS server ips, natsub is simply integrated with the Web socket server in Java. The front end like js can easily subscribe or dissubscrbe a topic or a type of topic with any number of id by sending message. There will be just one handler to communicate with NATS server shared by any number of sessions which subscribe the same topic.

## How to setup
### Setup server
The following code shows how to initialize the web socket server with natsub handlering the messages.
```java
	package com.natsub.ws;

	import javax.websocket.CloseReason;
	import javax.websocket.OnClose;
	import javax.websocket.OnError;
	import javax.websocket.OnMessage;
	import javax.websocket.OnOpen;
	import javax.websocket.Session;
	import javax.websocket.server.ServerEndpoint;

	import org.apache.log4j.Logger;

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
```
### Configure topic
Configure topics in Enum TopicType under com.natsub.ws.Topic.java directly. Recently it just support hardcodely.
```java
    EXAMPLE_TOPIC("1", "topic.example.prefix.", NatsHandler.class);
```
The first parameter is the type of the topic which is also the topic unique identification.
The second parameter is the prefix of the topic name. It will be joined with id sent from client, then as a name to subscribe the topic from NATS. 
The last parameter is the handler to process the response from NATS. Now only NatsHandler is provided.

### Web client
Firstly, the ws.js (which is under example/client) should be added in html.
Then connect to the web socket server by code:
```javascript
	WSManager().get_instance().connect('http://example.ws');
```
#### register message handler
```javascript
	WSManager().get_instance().register_handler({
		type: "1",
		
		// process the message
		handle_message: function(message) {
		
		}
	})
```
#### Subscribe topic
Subscribe by specific ids 1, 2, 3:
```javascript
	WSManager().get_instance().send_params({
		action: 'subscribe',
		type: '1',
		id: '1,2,3'
	})
```
Then it subscribe topic "topic.example.prefix.1", "topic.example.prefix.2" and "topic.example.prefix.3" . If the id is not provided, the name of subscribed topic will be "topic.example.prefix."
#### Unsubscribe topic
by id:
```javascript
	WSManager().get_instance().send_params({
		action: 'unsubscribe',
		type: '1',
		id: '1,2,3'
	})
```
by type:
```javascript
	WSManager().get_instance().send_params({
		action: 'unsubscribeType',
		type: '1'
	})
```
unsubscribe all topic related current session:
```javascript
	WSManager().get_instance().send_params({
		action: 'unsubscribeAll'
	})
```
