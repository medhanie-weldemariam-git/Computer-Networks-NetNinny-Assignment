package Web_Proxy;

import java.io.*;
import java.net.*;


public class Web_proxy_thread extends Thread 
{
	private Socket socket = null;

	public Web_proxy_thread(Socket socket) 
	{
		super("ProxyThread");//No attached javadoc source
		this.socket = socket;
	}

	public void run() 
	{
		byte[] reply = new byte[16384];

		while (true) 
		{
			Socket server = null;

			try 
			{
				//WIAT A CONNECTION ON THE LOCAL PORT 
				final InputStream streamFromClient = socket.getInputStream();
				final OutputStream streamToClient = socket.getOutputStream();  

				InputStreamReader inputStreamReader = new InputStreamReader(streamFromClient);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

				String client_request_url = bufferedReader.readLine();
				String temp_url = null;
				String request=client_request_url+"\r\n";
				try
				{
					while((temp_url = bufferedReader.readLine()).length() > 0)
					{
						request+=temp_url+"\r\n";      	
					}
					request+="\r\n";
				}
				catch (Exception e){}

				try 
				{   
					/*CHECK IF THE REQUEST CONTAINS BAD WORD*/
					if(isBadword(client_request_url))
					{               
						PrintWriter print_toclient = new PrintWriter(streamToClient, true);
						print_toclient.print("Sorry, but the Web page that you were trying to access is inappropriate.\n\n" + "Net Ninny");
						print_toclient.flush();
						socket.close();
						break;
					}
					else
					{
						/*MAKE A CONNECTION TO THE REAL SERVER*/          
						server = new Socket(getHost(client_request_url), 80);
					}
				} 

				catch (IOException e){}

				final InputStream streamFromServer = server.getInputStream();
				final OutputStream streamToServer = server.getOutputStream();

				/**transmit bytes from client to server**/           
				streamToServer.write(request.getBytes());
				int responseByte;
				boolean hasContent =false;
				while((responseByte = streamFromServer.read(reply)) != -1) 
				{
					String responseLine=new String(reply,"UTF-8");//get the string form of the response reply in bytes
					if(responseLine.contains("Content-Type: text/html"))
					{
						hasContent=true;
					}
					if(hasContent)
					{
						if(isBadword(responseLine))
						{

							PrintWriter print_toclient = new PrintWriter(streamToClient, true);
							print_toclient.print("Sorry, but the Web page that you were trying to access is inappropriate.\n\n" + "Net Ninny");
							print_toclient.flush();
							socket.close();
							break;
						}
					}  
					streamToClient.write(reply, 0, responseByte);		    	
					streamToClient.flush();	
				}		       
				streamToClient.close(); //SERVER CLOSED CONNECTION TO US SO WE CLOSE THE CONNECTION TO CLIENT
			} 
			catch (IOException e) {} 
			catch(Exception e){}
			finally 
			{
				try 
				{
					if (server != null)
						server.close();
					if (socket != null)
						socket.close();
				} 
				catch (IOException e){}
			}
		}
	}
//Get the url of the client request
	public static String getHost(String url)
	{
		if(url == null || url.length() == 0)
			return "";

		int doubleslash = url.indexOf("//");
		if(doubleslash == -1)
			doubleslash = 0;
		else
			doubleslash += 2;

		int end = url.indexOf('/', doubleslash);
		end = end >= 0 ? end : url.length();

		int port = url.indexOf(':', doubleslash);
		end = (port > 0 && port < end) ? port : end;

		return url.substring(doubleslash, end);
	}
//filtering for a bad words
	public static boolean isBadword(String user_req)
	{ 
		String[] myStringArray = {"SpongeBob","Sponge_Bob","spongebob","britneyspears","Paris_hilton", "Paris_Hilton", "parishilton", "Britney_Spears"};
		for(int i=0; i < myStringArray.length; i++)
		{
			if(user_req.contains(myStringArray[i]))
			{
				return true;
			}
		}
		return false;
	}
}
