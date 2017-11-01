package Web_Proxy;

import java.io.*;
import java.net.*;

public class Web_proxy 
{
	public static void main(String[] args) throws IOException 
	  {
        ServerSocket serverSocket = null;      
        boolean listening = true;
        
	    try 
	    {
	      int localport = 8888;  
	      serverSocket = new ServerSocket(localport);	      
	      System.out.println("Starting the proxy on port number: " + localport);
	    } 
	    catch (Exception e){}

	    while (listening) 
        {
            new Web_proxy_thread(serverSocket.accept()).start();         
        }
        serverSocket.close();
	  }
}
