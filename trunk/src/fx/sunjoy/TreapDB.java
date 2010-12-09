package fx.sunjoy;


import java.io.File;
import java.io.IOException;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.server.TreapDBBinaryProtocolServer;
import fx.sunjoy.server.TreapDBTextProtocolServer;
import fx.sunjoy.utils.ConfigUtil;
import fx.sunjoy.utils.FastString;

/**
 * TreapDB server talking memcached
 * @author sunjoy
 *
 */
public class TreapDB {
	public static void main(String[] args) throws Exception{

		if(args.length < 1)
		{
			System.out.println("java -cp . fx.sunjoy.TreapDB [configure file path]") ;
			return ;
		}
		
		String configFilePath = args[0] ;
		ConfigUtil params = new ConfigUtil(configFilePath) ;
		
		if(!params.isValidConfigFile())
		{
			System.out.println("configure file is not valid") ;
			return ;
		}
		
		int index_block_size = params.getIndexBlockSize() ;
		long mmap_size = params.getMmapSize() << 20 ; 
		
		final int textport = params.getTextPort() ;
		final int thriftport = params.getThriftPort() ;
		String index_file_path = params.getIndexFilePath() ;
		
		final DiskTreap<FastString, byte[]>  diskTreap = new DiskTreap<FastString, byte[]>(index_block_size,new File(index_file_path),mmap_size);
		
		final TreapDBTextProtocolServer  Textserver = new TreapDBTextProtocolServer(diskTreap, textport);
		final TreapDBBinaryProtocolServer ThriftServer = new TreapDBBinaryProtocolServer(diskTreap, thriftport) ;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        System.out.println("Running Shutdown Hook");
		        diskTreap.close();
		      }
	    });
		
		Thread textServerThread = new Thread(){
			public void run(){
				try {
					Textserver.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		textServerThread.setDaemon(true);
		textServerThread.start();
		
		
		Thread thriftServerThread = new Thread(){
			public void run(){
				ThriftServer.run();
			}
		};
		thriftServerThread.setDaemon(true);
		thriftServerThread.start();
		
		String logo ="";
		logo+="  _____                     ____  ____\n";
		logo+=" |_   _| __ ___  __ _ _ __ |  _ \\| __ )\n";
		logo+="   | || '__/ _ \\/ _` | '_ \\| | | |  _ \\\n";
		logo+="   | || | |  __/ (_| | |_) | |_| | |_) |\n";
		logo+="   |_||_|  \\___|\\__,_| .__/|____/|____/\n";
		logo+="                     |_|\n";
		System.out.println(logo);
		System.out.println("Listening memcached protocol port  : "+textport);
		System.out.println("Listening thrift protocol port  : "+thriftport);
		System.out.println("Index File Path :"+index_file_path);
		System.out.println("Max Key Size:"+(index_block_size-DiskTreapNode.STRING_KEY_OVER_HEAD)+" Bytes");
		System.out.println("Index Block Size:"+index_block_size+" Bytes");
		System.out.println("Memory Map Size :"+mmap_size+" Bytes");
		System.out.println("Total Record Amount:"+diskTreap.length());
		try {
			textServerThread.join();
			thriftServerThread.join() ;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException by user.");
		}
	}
}
