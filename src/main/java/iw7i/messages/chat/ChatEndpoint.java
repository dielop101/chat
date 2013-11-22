package iw7i.messages.chat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import Eliza.ElizaMain;

@ServerEndpoint(value = "/chat/{room}", encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class)
public class ChatEndpoint {
	private final Logger log = Logger.getLogger(getClass().getName());
	private static ArrayList<Session> sessions = new ArrayList<Session>();
	private static ElizaMain eliza;
	private static File logFile = new File("logChat");
	private static BufferedWriter output;

	@OnOpen
	public void open(final Session session, @PathParam("room") String room) {
		session.getUserProperties().put("room", room);
		sessions.add(session);
		log.info(room);
		// si eliza no ha sido inicializado, lo inicializamos (así como el
		// fichero de Logs
		if (eliza == null) {
			eliza = new ElizaMain();
			eliza.readScript(false, "/script");
			if (!logFile.exists()) {
				try {
					// En caso de no hacerlo
					logFile.createNewFile();

				} catch (Exception e) {
					System.out.println("No se ha podido crear el archivo");
				}
			}
			
			// Eliza da la bienvenida al nuevo usuario y metemos el saludo en
			// logs
			ChatMessage elizaChatMessage = new ChatMessage();
			elizaChatMessage.setSender("Eliza");
			elizaChatMessage.setMessage("Hello, I'm Eliza!");
			elizaChatMessage.setReceived(new Date());
			try {
				output = new BufferedWriter(new FileWriter(logFile, true));
				output.write("\nsala = "
						+ session.getUserProperties().get("room"));
				output.write("\nEliza escribe -> Hello, I'm Eliza!");
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				session.getBasicRemote().sendObject(elizaChatMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@OnClose
	public void onClose(final Session session) {
		sessions.remove(session);
	}

	@OnMessage
	public void onMessage(final Session session, final ChatMessage chatMessage) {
		try {
			String reply = eliza.runProgram(chatMessage.getMessage(), null);
			ChatMessage elizaChatMessage = new ChatMessage();
			elizaChatMessage.setSender("Eliza");
			elizaChatMessage.setMessage(reply);
			elizaChatMessage.setReceived(new Date());
			log.info("" + session.getUserProperties().get("room"));
			try {
				output = new BufferedWriter(new FileWriter(logFile, true));
				output.write("\nsala = "
						+ session.getUserProperties().get("room"));
				output.write("\n" + chatMessage.getSender() + " escribe -> "
						+ chatMessage.getMessage());
				output.write("\nsala = "
						+ session.getUserProperties().get("room"));
				output.write("\nEliza escribe -> " + reply);
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (int i = 0; i < sessions.size(); i++) {
				Session s = sessions.get(i);
				if (session.getUserProperties().get("room")
						.equals(s.getUserProperties().get("room"))) {
					s.getBasicRemote().sendObject(chatMessage);
					s.getBasicRemote().sendObject(elizaChatMessage);
				}
			}
			// session.getBasicRemote().sendObject(elizaChatMessage);
		} catch (Exception e) {
			log.log(Level.WARNING, "onMessage failed", e);
		}
	}
}
