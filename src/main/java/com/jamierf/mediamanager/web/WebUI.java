package com.jamierf.mediamanager.web;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.EmbeddedResourceHandler;

import com.jamierf.epdirscanner.EpDirScanner;
import com.jamierf.mediamanager.web.messages.LogMessage;

public class WebUI {

	private static final Logger logger = LoggerFactory.getLogger(WebUI.class);

	private final WebServer server;
	private final DataWebSocketHandler websocketHandler;

	public WebUI(int port, EpDirScanner epScanner) throws IOException {
		server = WebServers.createWebServer(port);

		if (logger.isDebugEnabled())
			logger.debug("WebUI started on port {}", port);

		// Static files, in htdocs resource directory
		server.add(new EmbeddedResourceHandler(WebUI.class.getPackage().getName().replaceAll("\\.", "/") + "/htdocs"));

		server.add("/filetree.html", new MediaTreeHandler(epScanner));

		websocketHandler = new DataWebSocketHandler();
		server.add("/socket", websocketHandler);

		server.start();
	}

	public void sendLog(String message, LogMessage.Type type) {
		websocketHandler.send(new LogMessage(message, type));
	}
}
