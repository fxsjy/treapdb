package fx.sunjoy.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TreapDBShellClient {

	public static void main(String[] args) 
	{
		String host = null ;
		int port = -1 ;
		
		if(args.length < 2)
		{
			System.out.println("please enter treapdb's host and port") ;
			return ;
		}
		else
		{
			host = args[0] ;
			port = Integer.valueOf(args[1]) ;
		}
		
		TreapDBShellClient client = new TreapDBShellClient(host , port) ;
		try 
		{
			client.start() ;
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private String db_host = null;
	private int db_port = -1 ;
	private Socket clientSocket = null ;
	
	public TreapDBShellClient(String db_host, int db_port)
	{
		this.db_host = db_host ;
		this.db_port = db_port ;
	}
	
	public void start() throws Exception
	{
		clientSocket = new Socket(db_host, db_port) ;
		InputStream in = clientSocket.getInputStream() ;
		OutputStream out = clientSocket.getOutputStream() ;
		BufferedOutputStream bos = new BufferedOutputStream(out) ;
		BufferedReader breader = new BufferedReader(new InputStreamReader(in)) ;
		
		System.out.println("Connected to " + db_host + ":" + db_port) ;
		System.out.print(">") ;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)) ;
		String content = null ;
		while((content = reader.readLine()) != null)
		{
			if(content.length() > 0)
			{
				if(content.equals("quit"))
				{
					System.out.println("bye") ;
					return ;
				}
				
				bos.write((content+ "\r\n").getBytes()) ;
				if(content.startsWith("set "))
				{	
					System.out.print(">") ;
					content = reader.readLine() ;
					if(content != null)
					{
						bos.write((content + "\r\n").getBytes()) ;
					}
				}
				
				bos.flush() ;
				
				while(true)
				{
					String answer = breader.readLine() ;
					if(answer != null)
					{
						System.out.print(">") ;
						System.out.println(new String(answer.getBytes())) ;
						if(answer.equals("END") || answer.equals("STORED") ||
								answer.equals("ERROR") || answer.equals("DELETED") || answer.equals("NOT_FOUND"))
						{
							break ;
						}
					}
				}
				
				System.out.print(">") ;
			}
			else
			{
				System.out.print(">") ;
			}
		}
	}
}
