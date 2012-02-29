package com.jamierf.mediamanager.web;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.EmbeddedResourceHandler;

import com.google.gson.Gson;
import com.jamierf.epdirscanner.EpDirScanner;
import com.jamierf.mediamanager.DLManager;
import com.jamierf.mediamanager.web.messages.LogMessage;

public class WebUI {

	private static final Logger logger = LoggerFactory.getLogger(WebUI.class);

	private final WebServer server;
	private final Gson gson;
	private final DataWebSocketHandler websocketHandler;

	public WebUI(int port, final EpDirScanner epScanner, final DLManager dlManager) throws IOException {
		server = WebServers.createWebServer(port);
		gson = new Gson();

		if (logger.isDebugEnabled())
			logger.debug("WebUI started on port {}", port);

		// Static files, in htdocs resource directory
		server.add(new EmbeddedResourceHandler(WebUI.class.getPackage().getName().replaceAll("\\.", "/") + "/htdocs"));

		server.add("/library.json", new HttpHandler() {
			@Override
			public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
				final String json = gson.toJson(epScanner.getSeries());
				response.content(json).end();
			}
		});

		server.add("/extramedia.json", new HttpHandler() {
			@Override
			public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
				final String json = gson.toJson(epScanner.getExtras());
				response.content(json).end();
			}
		});

		server.add("/extradls.json", new HttpHandler() {
			@Override
			public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
				final String json = gson.toJson(dlManager.getExtras());
				response.content(json).end();
			}
		});

		websocketHandler = new DataWebSocketHandler();
		server.add("/socket", websocketHandler);

		server.start();
	}

	public void sendLog(String message, LogMessage.Type type) {
		websocketHandler.send(new LogMessage(message, type));
	}
}
