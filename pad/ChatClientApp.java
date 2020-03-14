package pad;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
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
import java.net.UnknownHostException;


/**
 * a non-threaded version of the gui chatclient to 
 * it does not receive input from the server
 * The next version,ChatClientAppB, creates a new thread and is able to display input from the server 
 *
 */


public class ChatClientApp extends Applet implements ActionListener, Runnable{
	
	   private Socket socket              = null;
	   private DataInputStream  streamIn   = null;
	   private DataOutputStream streamOut = null;
	   private ChatClientThread client    = null;
	   private TextArea  display = new TextArea();
	   private TextField input   = new TextField();
	   private TextField inputPrivate   = new TextField();
	   private Button    sendPrivate    = new Button("Send Private");
	   private Button    send    = new Button("Send"), connect = new Button("Connect"),
	                     quit    = new Button("Bye");
	   private String    serverName = "localhost";
	   private int       serverPort = 8080;
	   Panel mainPanel = new Panel();
	   private Label idLabel=new Label("ID to send private:");
	   private TextField id=new TextField();
	   Panel south = new Panel();
	   private boolean done = true;
	   private String line = "";
	
	
	public void init(){
		
		mainPanel.setLayout(new BorderLayout());
		setSize(500, 500);
		
		quit.setEnabled(false);
		connect.setEnabled(true);
		quit.addActionListener(this);
		connect.addActionListener(this);
	
		
		
		south.setLayout(new GridLayout(5, 2));
		south.add(connect);
		south.add(quit);
		south.add(input);
		south.add(send);
		send.setEnabled(false);
		south.add(inputPrivate);
		south.add(sendPrivate);
		south.add(idLabel);
		idLabel.setAlignment(Label.RIGHT);
		idLabel.setFont(new Font("Arial", Font.BOLD, 16));
		south.add(id);
		sendPrivate.setEnabled(false);
		send.addActionListener(this);
		sendPrivate.addActionListener(this);
		
		
		Label title = new Label("Our Chat", Label.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 14));
		
		mainPanel.add(title, BorderLayout.NORTH);
		display.setEditable(false);//set editable to false so users can't type there
		display.setBackground(new Color(120, 100, 75, 10));//so it isnt gray
		display.setForeground(new Color(240, 240, 75, 100));//so it isnt gray
		mainPanel.add(display, BorderLayout.CENTER);
		mainPanel.add(south, BorderLayout.SOUTH);
		
		add(mainPanel);
		
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==quit){
			disconnect();
		}
		else if(e.getSource()==connect){
			connect(serverName, serverPort);
		}
		else if(e.getSource()==send){
			send();
			//input.requestFocus();
		}
		else if(e.getSource()==sendPrivate){
			sendPrivate();
			//input.requestFocus();
		}
	}
	
	public void connect(String serverName, int serverPort){	
		 done=false;
		displayOutput("call to connect was made");
		//create new socket, open stream, disable connect button, enable send and quit button
		try{
			socket=new Socket(serverName, serverPort);
			displayOutput("Connected: "+ socket);
			open();
			send.setEnabled(true);
			sendPrivate.setEnabled(true);
			quit.setEnabled(true);
			connect.setEnabled(false);
		}
		catch(UnknownHostException uhe){
			displayOutput(uhe.getMessage());
		}
		catch(IOException ioe){
			displayOutput(ioe.getMessage());
		}
		
	}

	public void disconnect(){
		input.setText("bye");
		send();
		quit.setEnabled(false);
		sendPrivate.setEnabled(false);
		connect.setEnabled(true);
		send.setEnabled(false);
		sendPrivate.setEnabled(false);
	}
	
	private void send(){
		//send message
	//	displayOutput(input.getText());//testing buttons before testing connection and streams
	//	input.setText("");//testing buttons before testing connection and streams
		String msg = input.getText();
		try{
			displayOutput(msg);
			streamOut.writeUTF(msg);
			streamOut.flush();
			//server.handle(socket.getPort(), msg);
			if(msg.equalsIgnoreCase("bye")){
				quit.setEnabled(false);
				connect.setEnabled(true);
				send.setEnabled(false);
				sendPrivate.setEnabled(false);
				close();
			}
			input.setText("");
		}
		catch(IOException ioe){
			displayOutput("Sending error "+ioe.getMessage());
			close();
		}
	}
	
	private void sendPrivate(){
		//send message
	//	displayOutput(input.getText());//testing buttons before testing connection and streams
	//	input.setText("");//testing buttons before testing connection and streams
		String msg = inputPrivate.getText();
		try{
			displayOutput(msg);
			streamOut.writeUTF(msg);
			streamOut.flush();
			//server.handle(socket.getPort(), msg);
			if(msg.equalsIgnoreCase("bye")){
				quit.setEnabled(false);
				connect.setEnabled(true);
				send.setEnabled(false);
				sendPrivate.setEnabled(false);
				close();
			}
			input.setText("");
		}
		catch(IOException ioe){
			displayOutput("Sending error "+ioe.getMessage());
			close();
		}
	}
	
	public void open(){
		try{
			streamOut = new DataOutputStream(socket.getOutputStream());
			streamIn =  new DataInputStream(socket.getInputStream());
			new Thread(this).start();
		}
		catch(IOException ioe){
			
		}
	}
	
	public void close(){
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
			displayOutput("Error closing");
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
	public void getParameters(){
		serverName = getParameter("host");
		serverPort = Integer.parseInt(getParameter("port"));
	}
	
	public void run() {
		// TODO Auto-generated method stub
		try{
			while(!done){
				line = streamIn.readUTF();
				displayOutput(line);
			}
		}
		catch(IOException ioe){
			done=true;
			displayOutput(ioe.getMessage());
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
