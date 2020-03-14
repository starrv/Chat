package chat;

public class IDGenerator 
{

	private static final char[] abcVal = 
	{
		'X','T','V','L','U','Q','G','M',
		'I','R','Z','D','W','S','J','Y',
		'F','O','N','B','E','A','H','C',
		'P','K', 'x', 't','v','l','u','q','g',
		'm','i','r','z','d','w','s','j','y',
		'f','o','n','b','e','a','h','c','p',
		'k','_', 	
	};
	private String[] userIDs;
	private int userIDCount=0;
	private int maxSize;
	
	public IDGenerator(int size)
	{
		userIDs=new String[size];
		maxSize=size;
	}
	
	private int findID(String id)
	{
		if(!(id.equalsIgnoreCase("")))
		{
			for(int i=0; i<userIDCount; i++)
			{
				if(userIDs[i].equals(id))
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	public String generateID(int idLength)
	{
		String id="";
		if(userIDCount<maxSize)
		{
			for(int i=0; i<idLength; i++)
			{
				id+=abcVal[(int) Math.floor(Math.random()*abcVal.length)];
			}
			if(!(id.equalsIgnoreCase("")) || findID(id)<0)//add ID to array
			{
				userIDs[userIDCount]=id;
				userIDCount++;
				return id;
			}
			else
			{
				id="";
				generateID(idLength);
			}
			return id;
		}
		else
		{
			Functions.printMessage("Can't generate UserID.  Maxed out.");
			return id;
		}
	}
	
	public void deleteID(String id)
	{
		int index=findID(id);
		if(index>=0)
		{
			userIDs[index]="";
			userIDCount--;
		}
	}
}
