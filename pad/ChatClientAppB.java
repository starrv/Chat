package pad;

import java.applet.Applet;
import java.net.*;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketPermission;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;


/**
 * a threaded version of the gui chatclient to 
 * be able to receive input from the server
 *
 */

public class ChatClientAppB extends Applet implements ActionListener, Runnable{
	
	   private Socket socket              = null;
	   private DataInputStream  streamIn   = null;
	   private DataOutputStream streamOut = null;
	   private ChatClientThread client    = null;
	   private TextArea  display = new TextArea();
	   private TextArea ids=new TextArea();
	   private TextField input   = new TextField();
	   private Button    send    = new Button("Send");
	   private TextField privateInput   = new TextField();
	   private TextField privateInputEncrypted   = new TextField();
	   private Button    sendPrivate    = new Button("Send Private");
	   private Button    sendPrivateEncrypted   = new Button("Send Encrypted Private");
	   private Panel privatePanel=new Panel();
	   private Button   connect = new Button("Connect");
	   private Button    quit    = new Button("Bye");
	   private String    serverName = "localhost";//will get from browser
	   private int       serverPort = 8080;//will get from browser
	   private Panel mainPanel = new Panel();
	   private Panel keys = new Panel();
	   private Panel south = new Panel();
	   private Panel displayPanel = new Panel();
	   private boolean done = true;
	   private String line = "";
	   private ChatServer server;
	   private Label idLabel=new Label("ID to send private:");
	   private TextField id=new TextField();
	
	public void init()
	{
		setName("Let's Chat");
		setLayout(null);
		setFont(new Font("Helvetica", Font.PLAIN, 14));
		setSize(500,350);
		mainPanel.setSize(500,350);
		setBackground(Color.white);
		mainPanel.setLayout(new BorderLayout());
		quit.setEnabled(false);
		connect.setEnabled(true);
		quit.addActionListener(this);
		connect.addActionListener(this);
		send.addActionListener(this);
		sendPrivate.addActionListener(this);
		sendPrivateEncrypted.addActionListener(this);
		privatePanel.setLayout(new GridLayout(5,2));
		privatePanel.setSize(mainPanel.getWidth(), mainPanel.getHeight()/3);
		privatePanel.add(quit);
		privatePanel.add(connect);
		privatePanel.add(input);
		privatePanel.add(send);
		send.setEnabled(false);		
		privatePanel.add(privateInput);
		privatePanel.add(sendPrivate);
		sendPrivate.setEnabled(false);		
		privatePanel.add(privateInputEncrypted);
		privatePanel.add(sendPrivateEncrypted);
		sendPrivateEncrypted.setEnabled(false);		
		privatePanel.add(idLabel);
		idLabel.setAlignment(Label.RIGHT);
		idLabel.setFont(new Font("Arial", Font.BOLD, 16));
		privatePanel.add(id);
		
		
		server=new ChatServer(serverPort);		
		//System.out.println("socket can "+p2.getActions());
		server.start();
		
		Panel titlePanel=new Panel();
		displayPanel.setLayout(new GridLayout(2,1));
		Label title = new Label("Our Chat", Label.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 14));
		ids.setFont(new Font("Helvetica", Font.PLAIN, 14));
		ids.setBackground(Color.white);
		ids.setEditable(false);
		displayPanel.add(ids);
		display.setEditable(false);//set editable to false so users can't type there
		display.setBackground(Color.orange);//so it isnt gray
		display.setFont(new Font("Helvetica", Font.PLAIN, 14));
		displayPanel.add(display);
		mainPanel.add(title, BorderLayout.NORTH);
		mainPanel.add(displayPanel, BorderLayout.CENTER);
		mainPanel.add(privatePanel, BorderLayout.SOUTH);
		
		add(mainPanel);
		
	}


	@Override
	public void actionPerformed(ActionEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource()==quit){
			disconnect();
		}
		else if(e.getSource()==connect){
			connect(serverName, serverPort);
		}
		else if(e.getSource()==send)
		{
			send();
		}
		else if(e.getSource()==sendPrivate)
		{
			sendPrivate();
		}
		else if(e.getSource()==sendPrivateEncrypted)
		{
			sendPrivateEncrypted();
		}
	}
	
	public void connect(String serverName, int serverPort){		
		done=false;
		displayOutput("call to connect was made, waiting to connect...");
		//create new socket, open stream, disable connect button, enable send and quit button
		try{
			ids.setText("Error: problem with server...");
			socket=new Socket(serverName, serverPort);
			//displayOutput("Socket closed: "+socket.isClosed());
			displayOutput("Connected: "+ socket);
			open();
			send.setEnabled(true);
			sendPrivate.setEnabled(true);	
			sendPrivateEncrypted.setEnabled(true);	
			quit.setEnabled(true);
			connect.setEnabled(false);
		}
		catch(UnknownHostException uhe)
		{
			displayOutput(uhe.getMessage());
			done=true;
		}
		catch(IOException ioe)
		{
			displayOutput(ioe.getMessage());
			done=true;
		}
		catch(Exception e)
		{
			displayOutput(e.getMessage());
			done=true;
		}
	}

	public void disconnect()
	{
		done=true;
		input.setText("bye");
		send();
		input.setText("");
		display.setText("");
		ids.setText("");
		quit.setEnabled(false);
		connect.setEnabled(true);
		send.setEnabled(false);
		sendPrivate.setEnabled(false);
		sendPrivateEncrypted.setEnabled(false);
	}
	
	private void send()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error: problem with server.");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = input.getText();
			//System.out.print("hi");
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else
			{
				try
				{
					//displayOutput("You said: "+msg);
					streamOut.writeUTF(msg);
					streamOut.flush();
					if(msg.equalsIgnoreCase("bye"))
					{
						quit.setEnabled(false);
						connect.setEnabled(true);
						send.setEnabled(false);
						sendPrivate.setEnabled(false);
						sendPrivateEncrypted.setEnabled(false);
						close();
					}
					input.setText("");
				}
				catch(IOException ioe)
				{
					displayOutput("Sending error "+ioe.getMessage());
					close();
				}
			}
		}
	}
	
	private void sendPrivate()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error: problem with server.");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = privateInput.getText();
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else if(id.getText().equalsIgnoreCase(""))
			{
				displayOutput("Message failed to be sent.  Please enter ID of recipient");
			}
			else
			{
				try
				{
					try
					{
						streamOut.writeUTF("private~"+id.getText()+"~"+msg);
						streamOut.flush();				
						if(msg.equalsIgnoreCase("bye"))
						{
							quit.setEnabled(false);
							connect.setEnabled(true);
							send.setEnabled(false);
							sendPrivate.setEnabled(false);
							sendPrivateEncrypted.setEnabled(false);
							close();
						}
						//displayOutput("You said: "+msg);
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
					privateInput.setText("");
				}
				catch(IOException ioe)
				{
					displayOutput("Sending error "+ioe.getMessage());
					close();
				}
			}
		}
	}
	
	private void sendPrivateEncrypted()
	{
		//send message
		//	displayOutput(input.getText());//testing buttons before testing connection and streams
		//	input.setText("");//testing buttons before testing connection and streams
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error: problem with server.");
		}
		else
		{
			String msg = privateInputEncrypted.getText();
			if(msg.equalsIgnoreCase(""))
			{
				displayOutput("Please enter text");
			}
			else if(id.getText().equalsIgnoreCase(""))
			{
				displayOutput("Message failed to be sent.  Please enter ID of recipient");
			}
			else
			{				
				OneTimePad encryptor=new OneTimePad(msg);
				String msgEncrypted=encryptor.getEncryptedMessage();
				//System.out.print("hi");
				try
				{
					try
					{
						streamOut.writeUTF("privateEncrypted~"+id.getText()+"~"+msgEncrypted+"~"+encryptor.getCurrentKey());
						streamOut.flush();				
						if(msg.equalsIgnoreCase("bye"))
						{
							quit.setEnabled(false);
							connect.setEnabled(true);
							send.setEnabled(false);
							sendPrivate.setEnabled(false);
							sendPrivateEncrypted.setEnabled(false);
							close();
						}
						//displayOutput("You said: "+msg+" encrypted as "+msgEncrypted);
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
					privateInputEncrypted.setText("");
				}
				catch(IOException ioe)
				{
					displayOutput("Sending error "+ioe.getMessage());
					close();
				}
			}
		}
	}
	
	public void open(){
		try
		{
			streamOut = new DataOutputStream(socket.getOutputStream());
			streamIn =  new DataInputStream(socket.getInputStream());
		    new Thread(this).start();//background thread to handle the input from the server...need to uncomment
		}
		catch(IOException ioe)
		{
			displayOutput("Read/Write error: "+ioe.getMessage());
		}
	}
	
	public void close(){
		done=true;
		try{
			if(streamOut !=null){
			streamOut.close();
			}
			if(streamIn !=null){
				streamIn.close();
			}
			if(socket !=null){
				socket.close();
			}		
		}
		catch(IOException ioe){
			displayOutput("Error closing: "+ioe.getMessage());
			client.close();
			client = null;
		}
	}
	
	public void displayOutput(String msg){
		display.append(msg +"\n");
	}
	
	public void handle(String msg){
		if(msg.equals("bye")){
			displayOutput("GOODBYE");
			close();
		}
		else{
			displayOutput(msg);
		}
	}
	public void getParameters()
	{
		serverName = getParameter("host");
		serverPort = Integer.parseInt(getParameter("port"));
	}



	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			while(!done){
				if(streamIn!=null)
				{
					if(!(socket.isClosed()))
					{
						line = streamIn.readUTF();
						StringTokenizer tokenizer=new StringTokenizer(line,"~");
						if(tokenizer.countTokens()>1)
						{
							if(tokenizer.nextToken().equalsIgnoreCase("ids"))
							{
								line=tokenizer.nextToken();
								displayIDs(line);
								displayOutput(line);
							}
						}
						else
						{
							displayOutput(line);
						}
					}
				}
			}
		}
		catch(IOException ioe){
			done=true;
			displayOutput("Read error occurred: "+ioe.getMessage());
		}		
	}
	
	private void displayIDs(String line)
	{
		ids.setText(line);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
