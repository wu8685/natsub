package com.natsub.log;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void init() {
		String prefix = getServletContext().getRealPath("/");
		String file = getInitParameter("log4j-init-file");

		if (file != null) {
			PropertyConfigurator.configure(prefix + file);
			System.out.println("Log4J Logging started: " + prefix + file);
		} else {
			System.out.println("Log4J Is not configured for your Application: " + prefix + file);
		}
	}
}
