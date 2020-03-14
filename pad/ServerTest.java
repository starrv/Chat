package pad;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerTest {

	public static void main(String[] args) {

		ServerSocket serversocket = null;
		try {
			serversocket = new ServerSocket(8080);
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		while (true) {
			System.out.println("Waiting for someone to connect to us on port "+serversocket.getLocalPort());
			try {
				
				//This call waits/blocks until someone connects to the port we are listening to
				Socket connectionsocket = serversocket.accept();
				
				//Get information about what computer connected to the server, just to show it
				InetAddress client = connectionsocket.getInetAddress();
				System.out.println(client.getHostAddress() + " connected to us.\n");
				
				//The data from the client (web browser) is sent via an InputStream. We read it
				// with a BufferedReader.
				BufferedReader input = new BufferedReader(
						new InputStreamReader(connectionsocket.getInputStream())
						);
				
				//The way we send data back to the computer and its web browser is via an
				// OutputStream. We use a DataOutputStream to help us with this.
				DataOutputStream output =
						new DataOutputStream(connectionsocket.getOutputStream());

				//The method below is our implementation of the HTTP protocol. We call the method
				// with the input and output streams, and our HTTP handler will do the rest!
				http_handler(input, output);
			}
			catch (Exception e) { //catch any errors, and print them
				System.out.println("\nError:" + e.getMessage());
			}

		}

	}



	/** If you have a array of strings which are of this format:
	 * {"Key1: Property","Key2: Another Property"} 
	 * you can obtain the property for whichever key you want by using this method. */
	static String getProperty(String key, String[] data) {
		key += ":";
		for (String s : data)
			if (s.startsWith(key)) return s.substring(key.length()).trim();
		return null;
	}


	/** Here is our implementation of HTTP. */
	private static void http_handler(BufferedReader input, DataOutputStream output) {

		/* Step 1: We obtain the request data from the web client (the web browser) from the stream.
		 * We will store each line of the request header into a separate String in an array.
		 * 
		 * Here is an example of what an HTTP request from a web browser will look like:
		 * If we write the following URL in a web browser:
		 * 
		 * http://localhost:8080/thething.htm
		 * 
		 * the web browser will send the following data to the server (this program):
		 * 
		 * GET /thething.htm HTTP/1.1
		 * Host: localhost:8080
		 * User-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0
		 * [...] (a few more lines)
		 * 
		 * As mentioned, we will store each line of the above request into a String in an array.
		 */
		
		String[] request = new String[32];

		//Write each line of the data the web client sends into a String array
		System.out.println("Gathering data from the web browser. The request we got is:\n----");
		try {
			for (int i = 0; i < request.length; i++) {
				String str = input.readLine();
				if (str == null || str.trim().equals("")) break;
				System.out.println(str);
				request[i] = str;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("----\nFinished gathering data from the web browser.\n");


		//--------------------------------------------------------------------------------

		/* Step 2: Parse and interpret the data from the web browser
		 * 
		 * The very first line of the request contains important information about the protocol and
		 * the file we're requesting from the server. Every other line contains strings in the format
		 * 
		 * Data-Key: Property
		 * 
		 * Above we have a method called getProperty() that will help us obtain those properties.
		 */


		//Split the data from the first line of the request into separate strings by the space.
		String[] topData = request[0].split(" ");

		//Store some data into variables to give them human-readable names (instead of, say, topData[1])
		String requestMethod = topData[0]; //usually either "GET" or "POST", but can be a few other things
		String path = topData[1];     //the path of the file to get on the server, such as "/index.htm"
		String protocol = topData[2]; //Usually "HTTP/1.1"
		String userAgent = getProperty("User-Agent", request);


		System.out.println("The web browser is requesting a "+requestMethod);
		System.out.println("from the following path on the server: "+path);
		System.out.println("The user-agent of the web browser is: "+userAgent);

		//--------------------------------------------------------------------------------

		// Step 3: Send the response data to the web browser.
		
		//Start with the response header. Similar to the request, the first line
		//contains important information about the protocol, and every subsequent line
		//contains data in the form "Data-Key: Property"
		String responseHeader = "HTTP/1.0 200 OK\r\n" + 
				"Content-Type: text/html\r\n" + 
				"Connection: close\r\n" +
				"Server: ServerTest.java\r\n" +
				"Content-Type: text/html\r\n" +
				"\r\n";
		//The header ALWAYS ends with a new line!
		
		
		//and then after the new line, the contents of the HTML file start
		String responseData = "<html>\r\n" + 
				"<head>\r\n" + 
				"<title>Success</title>\r\n" + 
				"</head>\r\n" + 
				"<body>\r\n" + 
				"<p>It works! All page content will go here.<br />Isn't that neat?</p>\r\n" + 
				"<p>Some Java:<br />System.currentTimeMillis(): "+System.currentTimeMillis()+"</p>\r\n" + 
				"</body>\r\n" + 
				"</html>";
		
		
		//Now write the data. We send the header first, then the page content.
		System.out.println("\nSending data to the web browser...");
		try {
			output.writeBytes(responseHeader);
			output.writeBytes(responseData);
			output.close();
			//After closing the stream, it's over!
			System.out.println("It's over!\n");
		}
		catch (Exception e) {
		}

	}

}