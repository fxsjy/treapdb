package fx.sunjoy;


import java.io.File;
import java.io.IOException;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.repl.ReplSlave;
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
			System.out.println("./treapdb.sh [configure file path]") ;
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
		long mmap_size = (long)params.getMmapSize() << 20 ; //不加long会溢出为负数
		
		final int textport = params.getTextPort() ;
		final int thriftport = params.getThriftPort() ;
		String index_file_path = params.getIndexFilePath() ;
		
		final DiskTreap<FastString, byte[]>  diskTreap = new DiskTreap<FastString, byte[]>(index_block_size,new File(index_file_path),mmap_size);
		
		final TreapDBTextProtocolServer  Textserver = new TreapDBTextProtocolServer(diskTreap, textport);
		final TreapDBBinaryProtocolServer ThriftServer = new TreapDBBinaryProtocolServer(diskTreap, thriftport) ;
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        System.out.println("Running Shutdown Hook");
		        Textserver.close();
		        ThriftServer.close();
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
		int max_key_len = index_block_size-DiskTreapNode.STRING_KEY_OVER_HEAD;
		if(max_key_len > 127){
			throw new RuntimeException("max key length: 127(键长不能超过127B)");
		}
		System.out.println("Max Key Size:"+max_key_len+" Bytes");
		System.out.println("Index Block Size:"+index_block_size+" Bytes");
		System.out.println("Memory Map Size :"+mmap_size+" Bytes");
		System.out.println("Total Record Amount:"+diskTreap.length());
		
		/***********************master-slave************************************/
		String replicationRole = params.getReplicationRole() ;
		
		if(replicationRole != null && replicationRole.equalsIgnoreCase("Slave"))
		{
			Textserver.setReplicationRole("Slave") ;
			ThriftServer.setReplicationRole("Slave") ;
			
			String masterSource = params.getMasterSource() ;
			ReplSlave slave = new ReplSlave(masterSource, diskTreap) ;
			System.out.println("Server Role:" + replicationRole);
			System.out.println("My Master Is:" + masterSource);
			slave.start() ;
			
		}
		/***********************master-slave************************************/
		
		try {
			textServerThread.join();
			thriftServerThread.join() ;
		} catch (InterruptedException e) {
			System.out.println("InterruptedException by user.");
		}
	}
}
