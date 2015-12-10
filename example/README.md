# natsub

## How to demo
### Run Nats in Docker
First of all, build a docker image under directory docker. Then run a corresponding container.
```
docker build -t natsForNatsub .
docker run -it -d -p 4222:4222 -e TOPIC=topic.example.prefix.1 -e MESSAGE=hello natsForNatsub
```
This container will publish a topic topic.example.prefix.1 every four seconds.

### Instruct Nats
Configure Nats address in class com.natsub.ws.server.WebSocketService
```java
......

	@ServerEndpoint(value = "/ws/wsservice")
	public class CIWebSocket {

		private static Logger log = Logger.getLogger(CIWebSocket.class);
		
		// parameter should be a string consist of nats server ips which is joined by ';'
		protected static MessageProcessor processor = new MessageProcessor("http://1.1.1.1:4222;http://2.2.2.2:4222");
		
......
```

### Run Natsub project as a Web app
Run this project in a web server.

### Demo on web
Access it on web page. After subscribing the topic and registering a handler, the topic presently can be received and shown in the text area.
![natsub demo](/natsub/example/doc/example.jpg)
