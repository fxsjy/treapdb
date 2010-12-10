package fx.sunjoy.repl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.ByteUtil;
import fx.sunjoy.utils.ConvertUtil;
import fx.sunjoy.utils.FastString;

public class ReplSlave {
	
	//定时从Master获取数据
	private Timer slaveTimer = null ; 
	
	private Socket slaveSocket = null ;
	
	//master的地址 host:port
	private String masterSource = null ; 
	
	private DiskTreap<FastString, byte[]> db = null ;
	
	
	public ReplSlave(String masterSource, DiskTreap<FastString, byte[]> db)
	{
		this.masterSource = masterSource ;
		this.db = db ;
	}
	
	public void start() 
	{
		
			connect2Master() ;
			
			try {
				slaveTimer = new Timer() ;
				slaveTimer.schedule(new TimerTask(){
						
						private BufferedInputStream bis = new BufferedInputStream(slaveSocket.getInputStream()) ;
						private BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(slaveSocket.getOutputStream())) ;
						
						@Override
						public void run() 
						{
							try{
								syncToMaster(bwriter, bis) ;
							}
							catch(Exception e)
							{
								slaveTimer.cancel() ;
								try
								{
									slaveSocket.close() ;
								} catch (IOException e1) {
								}
								start() ;
							}
							
						}
					}, 0, 4000) ;
			} 
			catch (IOException e) {
				
				slaveTimer.cancel() ;
				try
				{
					slaveSocket.close() ;
				} catch (IOException e1) {
				}
			}
	
			
		
		/*try
		{
			String host = masterSource.split(":")[0] ;
			int port = Integer.valueOf(masterSource.split(":")[1]) ;
			slaveSocket = new Socket(host, port);
			slaveTimer.schedule(new TimerTask(){
				
				private BufferedInputStream bis = new BufferedInputStream(slaveSocket.getInputStream()) ;
				private BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(slaveSocket.getOutputStream())) ;
				
				@Override
				public void run() 
				{
					syncToMaster(bwriter, bis) ;
				}
			}, 0, 5000) ;
		}
		catch(Exception e)
		{
			e.printStackTrace() ;
			return ;
		}*/
		
	}
	
	public void stop()
	{
		if(slaveTimer != null)
		{
			slaveTimer.cancel() ;
			try 
			{
				slaveSocket.close() ;
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void connect2Master()
	{
		while(true)
		{
			try 
			{
				String host = masterSource.split(":")[0] ;
				int port = Integer.valueOf(masterSource.split(":")[1]) ;
				slaveSocket = new Socket(host, port);
				System.out.println("Connected to master " + masterSource) ;
				return ;
			} 
			catch (Exception e) 
			{
				System.out.println("can't connect to master " + masterSource ) ;
				System.out.println("reconnect 3 seconds later...") ;
				try {
					Thread.sleep(3000) ;
				} 
				catch (InterruptedException e1) {
				}
			}
		}
	}
	
	private void storeData(byte[] key_value)
	{
		try 
		{
			Map<String, byte[]> result = ByteUtil.parseKeyValue(key_value) ;
			for(String key : result.keySet())
			{
				System.out.println("key:" + key) ;
				System.out.println("value:" + new String(result.get(key))) ;
				db.put(new FastString(key), result.get(key)) ;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void deleteData(String key)
	{
		db.delete(new FastString(key)) ;
	}
	
	private void syncToMaster(BufferedWriter writer, BufferedInputStream bis) throws Exception
	{
		
			String command = "sync " + (db.getDataFileNO() - 1) + " " + db.getCurrentFilePos() +"\r\n";
			writer.write(command) ;
			writer.flush() ;
			
			while(true)
			{
				byte[] key_length = readExpectData(bis, 1) ;
				if(key_length[0] == 0)
				{
					return ;
				}
				
				byte[] key = readExpectData(bis, key_length[0]) ;
				
				byte[] type = readExpectData(bis, 1) ;
				if(type[0] == -1)
				{
					//执行删除操作
					deleteData(new String(key)) ;
					continue ;
				}
				
				byte[] value_length = readExpectData(bis, 4) ;
				
				byte[] reverse = new byte[4] ;
				for(int i = 0 ; i < 4; i++)
				{
					reverse[i] = value_length[3-i] ;
				}
				int vl = ConvertUtil.byte2int(reverse) ;
				
				byte [] value = readExpectData(bis, vl) ;
				
				byte[] key_value = new byte[1 + key.length + 1 + 4 + value.length] ;
				
				System.arraycopy(key_length, 0, key_value, 0, key_length.length) ;
				System.arraycopy(key, 0, key_value, key_length.length, key.length) ;
				System.arraycopy(type, 0, key_value, key_length.length + key.length, type.length) ;
				System.arraycopy(value_length, 0, key_value, key_length.length + key.length+ type.length, value_length.length);
				System.arraycopy(value, 0, key_value, key_length.length + key.length+ type.length +value_length.length, value.length) ;
				
				storeData(key_value) ;
			}

		
		
	}
	
	private byte[] readExpectData(BufferedInputStream bis, int expectnum) throws Exception
	{
		int count = 0 ;
		int total = 0 ;
		byte[] result = new byte[expectnum] ;
		while(total < expectnum)
		{
			byte[] temp_buffer = new byte[expectnum - total] ;
			count = bis.read(temp_buffer) ;
			if(count < 0)
			{
				return null ;
			}
			System.arraycopy(temp_buffer, 0, result, total, count) ;
			total += count ;
		}
		return result ;
	}
	
	

}
