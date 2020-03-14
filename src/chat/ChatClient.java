package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
/**
 * 
 * Chat Client Version 4
 * sends and receives messages unlike the first chat client version
 * It is a creates a separate thread to receive the messages from the server
 *
 */
public class ChatClient implements Runnable
{

	private Socket socket = null;
	private Thread thread = null;
	private DataInputStream console = null;
	//BufferedReader console = null; //d = new BufferedReader(new InputStreamReader(in))
	private DataOutputStream streamOut = null;
	private ChatClientThread client = null;
	private String line="";
	
	public ChatClient(String serverName, int serverPort){
		Functions.printMessage("Establishing connection to server "+serverName+ " on port "+ serverPort + " please wait...");
		try{
			socket = new Socket(serverName, serverPort);
			Functions.printMessage("Connected to socket: " + socket);
			start();
		}
		catch(UnknownHostException uhe){
			Functions.printMessage("Error Unknown Host: "+ uhe.getMessage());
		}
		catch(IOException ioe){
			Functions.printMessage("Unexpected exception: "+ ioe.getMessage());
		}
	}
	
	
	public void run() {
		// TODO Auto-generated method stub
		while((thread!=null) && (!line.equalsIgnoreCase("bye"))){
			try{
				line=console.readUTF();
				streamOut.writeUTF(line);
				streamOut.flush();
			}
			catch(IOException ioe)
			{
				Functions.printMessage("Sending error: " + ioe.getMessage());
			}
		}
	}

	public void start()throws IOException{
		console = new DataInputStream(socket.getInputStream());
		streamOut = new DataOutputStream(socket.getOutputStream());
		if(thread==null){
			client = new ChatClientThread(this, socket);
			thread = new Thread(this);
			thread.start();
		}
		
	}
	
	public void handle(String msg){
		if(msg.equalsIgnoreCase("bye")){
			line="bye";
			stop();//not the deprecated stop().. but the ChatClient class stop method
		}
		else{
			Functions.printMessage(msg);
		}
	}
	
	public void stop(){
		if(thread!=null){
			thread=null;
		}
		try{
			if(console !=null) console.close();
			if(streamOut !=null) streamOut.close();
			if(socket !=null) socket.close();
			if(client!=null){
				client = null;// = null; //instead of deprecated stop()
			}
		}catch(IOException ioe)
		{
			Functions.printMessage("Error closing....");
		}
	}	
}