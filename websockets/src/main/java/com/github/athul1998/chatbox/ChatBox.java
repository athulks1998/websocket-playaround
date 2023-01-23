package com.github.athul1998.chatbox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;

/**
 * @author athul1998 Web socket class for user management
 * This is an implementation for users for a public chat 
 * To do: users with token only needs to be allowed a session
 */
@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatBox {

	Map<String, Session> sessions = new ConcurrentHashMap<>();

	/**
	 * @param session
	 * @param username INFO : Happens when a new session is opened
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("username") String username) {
		sessions.put(username, session);
		broadcast("User " + username + " joined the chat");
	}

	/**
	 * @param session
	 * @param username INFO : Happenes when a session is closed
	 */
	@OnClose
	public void onClose(Session session, @PathParam("username") String username) {
		sessions.remove(username);
		broadcast("User " + username + " left from the chat ");
	}

	/**
	 * @param session
	 * @param username
	 * @param throwable INFO: If an error occurs with the session
	 */
	@OnError
	public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
		sessions.remove(username);
		broadcast("User " + username + " left on error: " + throwable);
	}

	/**
	 * @param message
	 * @param username
	 * INFO : Function excecutes when a message event is triggered 
	 */
	@OnMessage
	public void onMessage(String message, @PathParam("username") String username) {
		if (message.equalsIgnoreCase("_ready_")) {
			broadcast("User " + username + " joined");
		} else {
			broadcast(username + ": " + message);
		}
	}

	/**
	 * @param message INFO : broad cast the message to the chatbox
	 */
	private void broadcast(String message) {
		sessions.values().forEach(sess -> sess.getAsyncRemote().sendObject(message, result -> {
			if (result.getException() != null) {
				System.out.println("Unable to send message: " + result.getException());
			}
		}));
	}

}