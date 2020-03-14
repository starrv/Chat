package chat;

/**
 * 
 *Version 4:
 *Thread for Group Chat Server
 *This time it sends all text received from any of the connected clients to all clients. 
 *This means that the server has to receive and send, and the client has to send as well as receive
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class ChatServerThread extends Thread {

	private ChatServer server = null;
	private Socket socket = null;
	private String ID="";
	private DataInputStream streamIn = null;
	private DataOutputStream streamOut = null;
	private static final int ID_SIZE=5;

	public ChatServerThread(ChatServer _server, Socket _socket) {
		super();
		server = _server;
		socket = _socket;
		// ID = socket.getPort();
		IDGenerator idGenerator=new IDGenerator(50);
		ID = idGenerator.generateID(ID_SIZE);
		Functions.printMessage("Chat Server Thread Info: server: " + server
				+ " socket: " + socket + " ID: " + ID);
	}

	public void send(String msg) {
		try 
		{
			streamOut.writeUTF(msg);
			streamOut.flush();
		} 
		catch (IOException ioe) 
		{
			Functions.printMessage(ID + " ERROR sending: " + ioe.getMessage());
			server.removeClient(ID);
			ID = "";// set ID -1 for the thread...
			System.exit(0);
		}
	}

	public String getID() {
		return ID;
	}

	public void run() {
		Functions.printMessage("Server Thread " + ID + " running.");
		while (ID != "") {
			try {
				String line = streamIn.readUTF();
				StringTokenizer tokenizer = new StringTokenizer(line, "~");
				if (tokenizer.countTokens() <= 1) {
					server.handle(ID, line);
				} else {
					Functions.printMessage(line + " has " + tokenizer.countTokens()
							+ " tokens.");
					String prefix = tokenizer.nextToken();
					String toID = tokenizer.nextToken();
					String message = tokenizer.nextToken();
					if (prefix.equalsIgnoreCase("private")) {
						server.handlePrivate(toID, ID, message);
					} else if (prefix.equalsIgnoreCase("privateEncrypted")) {
						String key = tokenizer.nextToken();
						server.handlePrivateEncrypted(toID, ID, message, key);
					}
				}
			} catch (IOException ioe) {
				// Functions.printMessage(ID + "ERROR reading: " +
				// ioe.getMessage());
				server.removeClient(ID);
				ID = "";// set ID to -1 so it will not enter the loop again
						// instead of deprecated stop()
			}
		}
	}

	public void open() throws IOException {
		streamIn = new DataInputStream(new BufferedInputStream(
				socket.getInputStream()));
		streamOut = new DataOutputStream(new BufferedOutputStream(
				socket.getOutputStream()));
	}

	public void close() throws IOException {
		if (socket != null) {
			socket.close();
		}
		if (streamIn != null) {
			streamIn.close();
		}
		if (streamOut != null) {
			streamOut.close();
		}
	}

}
