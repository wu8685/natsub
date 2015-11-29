package com.natsub.ws;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.nats.Connection;

public class NatsManager {
	
	private static Logger logger = Logger.getLogger(NatsManager.class);
	private Connection conn;
	private String natsServer;
	
	public NatsManager(String natsServer) {
		this.natsServer = natsServer;
	}
	
	public Connection getConnection() {
		if(conn == null || !conn.isConnected()) {
			synchronized(this) {
				if(conn == null || !conn.isConnected()) {
					buildConnection(natsServer);
				}
			}
		}
		return conn;
	}

	private void buildConnection(String natsServers) {
		Properties prop = new Properties();
		prop.put("servers", natsServers);
		try {
			conn = Connection.connect(prop);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void destroy() throws Exception {
		if(conn != null) {
			conn.close();
		}
	}
}
