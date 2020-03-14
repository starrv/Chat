package pad;

import java.net.*;
import java.security.AccessControlException;
import java.io.*;

/**
 * 
 *Version 4:
 *Group Chat
 *This time it sends all text received from any of the connected clients to all clients. 
 *This means that the server has to receive and send, and the client has to send as well as receive
 *
 */
public class ChatServer implements Runnable{

	private ChatServerThread clients[] = new ChatServerThread[50];
	private ServerSocket server = null;
	private Thread thread = null;
	private int clientCount = 0;
	private SocketPermission p2;
	private static int serversConnected=0;
	
	public ChatServer(int port){
		try{
			System.out.println("Obtaining socket permission for localhost:"+port+" please wait...");
			p2 = new SocketPermission("localhost:"+port,  "listen, accept, connect, resolve");
			System.out.println("localhost:"+port+" can do these actions: "+p2.getActions());
			System.out.println("Binding to port " + port + " please wait...");
			server = new ServerSocket(port);
			serversConnected=1;
			start();
		}
		catch(IOException ioe){
			System.out.println("I/O Exception Cannot bind to port " + port + ": " +ioe.getMessage());
			if(ioe.getMessage().equalsIgnoreCase("Address already in use: JVM_Bind"))
			{
				serversConnected=1;
			}
			else
			{
				System.out.println("Message again repeat: "+ioe.getMessage());
			}
		}
		catch(AccessControlException ace)
		{
			System.out.println("Access Control Exception Cannot bind to port " + port + " due to : " +ace.getMessage());
			if(ace.getMessage().equalsIgnoreCase("Address already in use:  JVM_Bind"))
			{
				serversConnected=1;
			}
		}
	}
	

	//same as previous versions (i.e. same as version 3 and 2)	
	public void start(){
		if(thread==null){
			thread = new Thread(this);
			thread.start();
		}
	}	
	//same as previous versions (i.e. same as version 3 and 2)	
	public void stop(){
		if(thread !=null){
			thread=null;
		}
		serversConnected-=1;
	}
	//same as previous version (i.e. same as version 3)
	public void run() {
		// TODO Auto-generated method stub
		while(thread != null)
		{
			try
			{
				System.out.println("Waiting for a client...");
				addThread(server.accept());
			}
			catch(IOException ioe)
			{
				System.out.println("error accepting the client "+ioe.getMessage());
			}catch(NullPointerException e)
			{
				System.out.println("error accepting the client "+e.getMessage());
				break;
			}
			
		}
	}
	
	public synchronized void handlePrivateEncrypted(String ID, String fromID, String input, String key)
	{
		if(findClient(ID)!=-1)
		{
			System.out.println("Private Encrypted message '"+input+"' sent from " + fromID+" to "+ID);
			String message=OneTimePad.decryptMessage(input, key);
			if(input.equals("bye"))
			{
				clients[findClient(ID)].send("bye");
				clients[findClient(fromID)].send("You said: " +input);
				remove(ID);
			}
			else
			{
				clients[findClient(ID)].send("Private encrypted message from User " + fromID + ": " +message);
				clients[findClient(fromID)].send("You said: " +message+" encrypted as "+input);
			}
		}
		else
		{
			System.out.println("Error!! No such client "+ID);
			clients[findClient(fromID)].send("Error!! No such user " + ID);
		}
	}
	
	
	public synchronized void handlePrivate(String ID, String fromID, String input)
	{
		if(findClient(ID)!=-1)
		{
			System.out.println("Private message '"+input+"' sent from " + fromID+" to "+ID);
			if(input.equals("bye"))
			{
				clients[findClient(ID)].send("bye");
				clients[findClient(fromID)].send("You said: " +input);
				remove(ID);
			}
			else
			{
				clients[findClient(ID)].send("Private message from User " + fromID + ": " +input);
				clients[findClient(fromID)].send("You said: " +input);
			}
		}
		else
		{
			System.out.println("Error!! No such client "+ID);
			clients[findClient(fromID)].send("Message couldn't be sent!! No such user " + ID);
		}
	}
	private void sendToAllButOne(String message, String ID)
	{
		for(int i=0; i<clientCount; i++)
		{
			if(clients[i].getID()!=ID)
			{
				clients[i].send(message);
			}
		}
	}
	
	private void sendToAll(String message)
	{
		for(int i=0; i<clientCount; i++)
		{
			clients[i].send(message);
		}
	}
	
	public synchronized void handle(String ID, String input)
	{
		
			System.out.println("Message from " + ID+ ": " + input);
			for(int i=0; i<clientCount; i++)
			{
				if(clients[i].getID()!=ID)
				{
					clients[i].send("User " + ID + " said: " +input);
				}
				else
				{
					clients[i].send("You said: " +input);
				}
			}
			if(input.equals("bye"))
			{
			//clients[findClient(ID)].send("bye");
			remove(ID);
			}	
	}
	
	public synchronized void remove(String ID)
	{
		int pos = findClient(ID);
		if(pos >= 0)
		{
			ChatServerThread toTerminate = clients[pos];
			System.out.println("Removing client  " +pos+" with ID "+ ID);
			if ( pos < clientCount-1)
			{
				for (int i = pos+1; i < clientCount; i++)
				{
					clients[i-1] = clients[i];
				}
			}
			clientCount--;
			sendToAll("Client "+pos+" with ID "+ ID + " removed");
			printAllClients();
			try
			{
				toTerminate.close();
			}
			catch(IOException ioe)
			{
				System.out.println("Error closing thread: " + ioe);
			}
		}
	}

	
	private int findClient(String ID){
		for(int i=0; i<clientCount; i++){
			if(clients[i].getID().equals(ID)){
				System.out.println("Found ID named "+ID);
				return i;
			}
			else
			{
				System.out.println("Not found yet ID named "+ID);
			}
		}
		System.out.println("Never found ID named "+ID);
		return -1; //if ID not found in array	
	}
	
	private void printAllClients()
	{
		for(int i=0; i<clientCount; i++)
		{
			String ID=clients[i].getID();
			String IDs="ids~The IDs of the other clients in the group chat are: \n";
			for(int j=0; j<clientCount; j++)
			{
				if(clients[j].getID()!=ID)
				{
					IDs+=clients[j].getID()+"\n";
				}
			}
			IDs+="Your ID is: "+ID;
			clients[i].send(IDs);
		}
	}
	private synchronized void addThread(Socket socket)
	{
		if(clientCount < clients.length)
		{
			System.out.println("Client "+ clientCount + " accepted on : " + socket);
			clients[clientCount] = new ChatServerThread(this, socket);
			if(!(clients[clientCount].getID().equalsIgnoreCase("")))
			{																																	
				try
				{
					clients[clientCount].open();
					clients[clientCount].start();
					clientCount++;
					sendToAllButOne("Client "+ clientCount + " accepted with ID "+clients[clientCount-1].getID(), clients[clientCount-1].getID());
					printAllClients();
				}
				catch(IOException ioe)
				{                                                                                                                                                                                                                                            
					System.out.println("Error opening thread: " + ioe.getMessage());
				}			
			}
		}
		else
		{
			System.out.println("Client was refused: maximum " + clients.length + " reached.");
		}
	}
	
	public static int numServersConnected()
	{
		return serversConnected;
	}

	//same as previous version
		public static void main(String[] args){
			   ChatServer server = null;
			   if(args.length !=1){
				   System.out.println("To run the server you need to specify a port");
			   }
			   else{
				   server = new ChatServer(Integer.parseInt(args[0]));
				   
			   }
		}
	
	
	
	
	
	

}
