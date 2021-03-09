package chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Functions 
{
	static void printMessage(Object obj)
	{
//		try
//		{
//			PrintStream o = new PrintStream(new File("./Chat.txt"));
//			PrintStream console = System.out;
//			System.setOut(o); 
//		    System.out.println(obj);
//		    System.setOut(console);
//		    System.out.println(obj);
//		} 
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		System.out.println(obj);
	}

}
