package chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


/**
 * a threaded version of the gui chatclient to 
 * be able to receive input from the server
 *
 */

public class ChatClientApp extends JFrame implements ActionListener, MouseListener, KeyListener, WindowListener, Runnable
{
	
			private static final long serialVersionUID = 1L;
			private Socket socket              = null;
			private DataInputStream  streamIn   = null;
			private DataOutputStream streamOut = null;
			private ChatClientThread client    = null;
			private JTextArea  display = new JTextArea();
			private JTextArea ids=new JTextArea();
			private JTextArea input   = new JTextArea();
			private JRadioButton    sendToAll    = new JRadioButton("send to all");
			private JRadioButton    sendPrivate    = new JRadioButton("send private");
			private JRadioButton    sendPrivateEncrypted   = new JRadioButton("send encrypted and private");
			private JButton   connect = new JButton("connect");
			private JButton    quit    = new JButton("bye");
			
			private final int port=9293;
		
			private JPanel processPanel = new JPanel(new GridLayout(1,10));
			private boolean done = true;
			private String line = "";
			private ChatServer server;
			private JLabel idLabel=new JLabel("ID to send private:",SwingConstants.CENTER);
			private JTextField id=new JTextField();
			private JScrollPane inputScrollPane, idScrollPane, outputScrollPane;
	   
	   public ChatClientApp()
	   {
		   setTitle("Chat");
		   setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.jpg"))); 
		   Color color = new Color(199,135, 113);
		   Color color2=new Color(227,188,175);
		   Color color3=new Color(247,227,220);
		   Dimension size=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		   setSize(size.width/2,size.height/2);
		   setName("Chat");
		   setTitle("Chat");
		   JPanel panel=new JPanel(new BorderLayout());
		   setLayout(new BorderLayout());
		   setFont(new Font("Serif", Font.BOLD, 12));
		   
		   server=new ChatServer(port);		
		   server.start();
		   
		   display.setEditable(false);
		   display.setBackground(color2);
		   display.setBorder(BorderFactory.createLineBorder(Color.black));
		   display.addKeyListener(this);
		   display.setSize(200, 200);
		   DefaultCaret caret = (DefaultCaret) display.getCaret(); // 
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE); 
		   display.setLineWrap(true);
		   display.setBackground(Color.white);
		   outputScrollPane = new JScrollPane(display); 
		   outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		   outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   panel.add(outputScrollPane,BorderLayout.CENTER);
		   
		   ids.setEditable(false);
		   ids.setBorder(BorderFactory.createLineBorder(Color.black));
		   ids.addKeyListener(this);
		   caret=(DefaultCaret)ids.getCaret();
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		   idScrollPane = new JScrollPane(ids); 
		   idScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		   idScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   panel.add(idScrollPane,BorderLayout.LINE_START);
		   
		   panel.add(new JPanel(),BorderLayout.LINE_END);
		   
		   panel.add(new JPanel(),BorderLayout.PAGE_START);
		   
		   sendToAll.setEnabled(false);
		   sendToAll.addActionListener(this);
		   sendToAll.addKeyListener(this);
		   processPanel.add(sendToAll);
		   
		   sendPrivateEncrypted.setBackground(color);
		   sendPrivateEncrypted.setEnabled(false);
		   sendPrivateEncrypted.addActionListener(this);
		   sendPrivateEncrypted.addKeyListener(this);
		   processPanel.add(sendPrivateEncrypted);
		   
		   sendPrivate.setEnabled(false);
		   sendPrivate.addActionListener(this);
		   sendPrivate.addKeyListener(this);
		   sendPrivate.setBackground(color);
		   processPanel.add(sendPrivate);
		   
		   idLabel.setAlignmentY(RIGHT_ALIGNMENT);
		   id.setBorder(BorderFactory.createLineBorder(Color.black));
		   id.setOpaque(true);
		   id.setBackground(color3);
		   id.setEnabled(false);
		   idLabel.setOpaque(true);
		   idLabel.addKeyListener(this);
		   processPanel.add(idLabel);
		   
		   id.addKeyListener(this);
		   processPanel.add(id);
		   
		   //processPanel.setLayout(new GridLayout(0,2));
		   connect.setBackground(color);
		   connect.setBorder(BorderFactory.createLineBorder(Color.black));
		   connect.addActionListener(this);
		   connect.addKeyListener(this);
		   processPanel.add(connect);
		   
		   quit.setBackground(color);
		   quit.setBorder(BorderFactory.createLineBorder(Color.black));
		   quit.addActionListener(this);
		   quit.setEnabled(false);
		   quit.addKeyListener(this);
		   processPanel.add(quit);	
		   
		   input.setBackground(color3);
		   input.setBorder(BorderFactory.createLineBorder(Color.black));
		   input.setLineWrap(true);
		   input.setEnabled(false);
		   input.addKeyListener(this);
		   caret=(DefaultCaret)input.getCaret();
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		   inputScrollPane=new JScrollPane(input);
		   inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		   inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		   processPanel.add(inputScrollPane);
		   
		   panel.add(processPanel,BorderLayout.PAGE_END);
		   
		   add(panel,BorderLayout.CENTER);
		   
		   addKeyListener(this);
		   addMouseListener(this);
		   addWindowListener(this);
		   addKeyListener(this);
	   }
	   
	   
	
	public void actionPerformed(ActionEvent e) 
	{
		// TODO Auto-generated method stub
		if(e.getSource()==quit){
			disconnect();
		}
		else if(e.getSource()==connect){
			connect(server.getServerHost(), server.getServerPort());
		}
		else if(e.getSource()==sendToAll)
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
		displayOutput("call to connect was made, waiting to connect to "+serverName+":"+serverPort+"...");
		Functions.printMessage("call to connect was made, waiting to connect to "+serverName+":"+serverPort+"...");
		//create new socket, open stream, disable connect button, enable send and quit button
		try
		{
			socket=new Socket(serverName, serverPort);
			//displayOutput("Socket closed: "+socket.isClosed());
			displayOutput("Connected: "+ socket);
			Functions.printMessage("Connected: "+ socket);
			open();
			id.setEnabled(true);
			input.setEnabled(true);
			sendToAll.setEnabled(true);
			sendPrivate.setEnabled(true);	
			sendPrivateEncrypted.setEnabled(true);	
			quit.setEnabled(true);
			connect.setEnabled(false);
			streamOut.writeUTF("hello");
			streamOut.flush();
		}
		catch(UnknownHostException uhe)
		{
			displayOutput(uhe.getMessage());
			Functions.printMessage(uhe.getMessage());
			done=true;
		}
		catch(IOException ioe)
		{
			displayOutput(ioe.getMessage());
			Functions.printMessage(ioe.getMessage());
			done=true;
		}
		catch(Exception e)
		{
			displayOutput(e.getMessage());
			Functions.printMessage(e.getMessage());
			done=true;
		}
	}

	public void disconnect()
	{
		try
		{
			streamOut.writeUTF("bye");
			streamOut.flush();
		}	
		catch(IOException ioe)
		{
			displayOutput("Sending error "+ioe.getMessage());
			Functions.printMessage(ioe.getMessage());
		}
		catch(NullPointerException e)
		{
			setVisible(false);
		}
		done=true;
		close();
		id.setText("");
		id.setEnabled(false);
		input.setText("");
		input.setEnabled(true);
		display.setText("");
		ids.setText("");
		setTitle("Chat");
		quit.setEnabled(false);
		connect.setEnabled(true);
		sendToAll.setEnabled(false);
		sendPrivate.setEnabled(false);
		sendPrivateEncrypted.setEnabled(false);
	}
	
	private void send()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error sending message");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = input.getText();
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
						disconnect();
					}
					//input.setText("");
				}
				catch(IOException ioe)
				{
					Functions.printMessage("Sending error "+ioe.getMessage());
					ioe.printStackTrace(System.out);
					disconnect();
				}
			}
		}
	}
	
	private void sendPrivate()
	{
		if(line.equalsIgnoreCase(""))
		{
			displayOutput("Error sending message");
		}
		else
		{
			//send message
			//	displayOutput(input.getText());//testing buttons before testing connection and streams
			//	input.setText("");//testing buttons before testing connection and streams
			String msg = input.getText();
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
							disconnect();
						}
						//displayOutput("You said: "+msg);
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
				//	privateInput.setText("");
				}
				catch(IOException ioe)
				{
					//displayOutput("Sending error "+ioe.getMessage());
					disconnect();
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
			displayOutput("Error sending message");
		}
		else
		{
			String msg = input.getText();
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
				try
				{
					try
					{
						streamOut.writeUTF("privateEncrypted~"+id.getText()+"~"+msgEncrypted+"~"+encryptor.getCurrentKey());
						streamOut.flush();				
						if(msg.equalsIgnoreCase("bye"))
						{
							disconnect();
						}
						//displayOutput("You said: "+msg+" encrypted as "+msgEncrypted);
					}
					catch(NumberFormatException e)
					{
						displayOutput("Message failed to be sent.  Please enter ID of recipient");
					}
					//privateInputEncrypted.setText("");
				}
				catch(IOException ioe)
				{
					//displayOutput("Sending error "+ioe.getMessage());
					disconnect();
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
	
	public void displayOutput(String msg)
	{
		display.append(msg +"\n");
	}
	
	public void handle(String msg){
		if(msg.equals("bye")){
			disconnect();
		}
		else{
			displayOutput(msg);
		}
	}
	
	
	public void run() 
	{
		// TODO Auto-generated method stub
		try
		{
			while(!done)
			{
				if(streamIn!=null)
				{
					if(!(socket.isClosed()))
					{
						line = streamIn.readUTF();
						Functions.printMessage("line:"+line);
						StringTokenizer tokenizer=new StringTokenizer(line,"~");
						if(tokenizer.countTokens()>1)
						{
							if(tokenizer.nextToken().equalsIgnoreCase("ids"))
							{
								line=tokenizer.nextToken();
								displayIDs(line);
								displayOutput(line);
							}
							else
							{
								tokenizer=new StringTokenizer(line,"~");
								if(tokenizer.nextToken().equalsIgnoreCase("id"))
								{
									line=tokenizer.nextToken();
									setTitle(line);
								}
							}
						}
						else
						{
							displayOutput(line);
							continue;
						}
					}
					else
					{
						Functions.printMessage("Socket closed");
					}
				}
				else
				{
					Functions.printMessage("Stream is null");
				}
			}
		}
		catch(IOException ioe)
		{
			done=true;
			Functions.printMessage("Read error occurred: "+ioe.getMessage());
			ioe.printStackTrace(System.out);
			disconnect();
		}		
	}
	
	private void displayIDs(String line)
	{
		ids.setText(line);
	}


	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		/*if(e.getKeyCode()==KeyEvent.VK_ESCAPE);
		{
			disconnect();
		}
		if(e.getKeyCode()==KeyEvent.VK_S)
		{
			if(done==true)
				connect(serverName,serverPort);
		}*/
	}


	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	public void windowActivated(WindowEvent arg0) 
	{
		// TODO Auto-generated method stub
	}

	
	public void windowClosed(WindowEvent event) 
	{
		// TODO Auto-generated method stub
		disconnect();
		System.exit(0);
	}


	public void windowClosing(WindowEvent event) {
		// TODO Auto-generated method stub
		disconnect();
		System.exit(0);
	}

	
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
