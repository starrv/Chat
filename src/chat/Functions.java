package chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;


public class Functions 
{
	final static PrintStream console = System.out;
	
	static void printMessage(String message)
	{
		try
		{
			File file=new File("./log.txt");
			Path path=null;
			if(!file.exists())
			{
				Files.createFile(file.toPath());
				path=file.toPath();
			}
			else
			{
				path=FileSystems.getDefault().getPath("./log.txt");
			}
			String fileContents="";
			Charset charset = Charset.forName("US-ASCII");
			try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			    String line = null;
			    while ((line = reader.readLine()) != null) {
			        fileContents+=line;
			    }
			} catch (IOException x) {
			    System.err.format("IOException: %s%n", x);
			}
			System.out.println(fileContents);
			PrintStream o = new PrintStream(file);
			System.setOut(o); 
			System.out.println(fileContents+message);
			
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			System.setOut(console);
		    System.out.println(message);
			e.printStackTrace();
		} 
	}

}
