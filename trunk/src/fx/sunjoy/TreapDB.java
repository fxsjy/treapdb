package fx.sunjoy;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.server.TreapDBTextProtocolServer;

/**
 * TreapDB server talking memcached
 * @author sunjoy
 *
 */
public class TreapDB {
	public static void main(String[] args) throws Exception{
		int index_block_size = 128;
		long mmap_size = 64<<20;
		
		if(args.length<2){
			System.out.println("java -cp . fx.sunjoy.TreapDB [port] [file path name] [index block size(bytes)](optional) [memory map size(M)](optional)");
			return;
		}
		
		if(args.length>=3){
			index_block_size = Integer.parseInt(args[2]);
		}if(args.length==4){
			mmap_size = Long.parseLong(args[3])<<20;
		}
		final int port = Integer.parseInt(args[0]);
		final DiskTreap<String, Serializable>  diskTreap = new DiskTreap<String, Serializable>(index_block_size,new File(args[1]),mmap_size);
		
		final TreapDBTextProtocolServer  server = new TreapDBTextProtocolServer(diskTreap, port);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		      public void run() {
		        System.out.println("Running Shutdown Hook");
		        diskTreap.close();
		      }
	    });
		
		Thread t = new Thread(){
			public void run(){
				try {
					server.run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		t.setDaemon(true);
		t.start();
		String logo ="";
		logo+="  _____                     ____  ____\n";
		logo+=" |_   _| __ ___  __ _ _ __ |  _ \\| __ )\n";
		logo+="   | || '__/ _ \\/ _` | '_ \\| | | |  _ \\\n";
		logo+="   | || | |  __/ (_| | |_) | |_| | |_) |\n";
		logo+="   |_||_|  \\___|\\__,_| .__/|____/|____/\n";
		logo+="                     |_|\n";
		System.out.println(logo);
		System.out.println("Listening Port  : "+port);
		System.out.println("Index File Path :"+args[1]);
		System.out.println("Max Key Size:"+(index_block_size-34)+" Bytes");
		System.out.println("Index Block Size:"+index_block_size+" Bytes");
		System.out.println("Memory Map Size :"+mmap_size+" Bytes");
		System.out.println("Text Protocol Mode(Memcached Compatible)");
		try {
			t.join();
		} catch (InterruptedException e) {
			System.out.println("InterruptedException by user.");
		}
	}
}
