package fx.sunjoy.test.dirtytest.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TestTreapDBClientMThread {
	public static void main(String[] args) throws Exception {
		final ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		
		final int count = 50000;
	
		long t1 = System.currentTimeMillis();
		Thread[] jobs = new Thread[20];
		for(int z=0;z<20;z++){
			Thread job = new Thread(){
				public void run(){
					try {
						TreapDBClient client = TreapDBClientFactory.getClient("localhost", 11812);
						
						for(int i=0;i<count;i++){
							Integer key = (int) (Math.random()*Integer.MAX_VALUE);
							client.put(""+key,buf);
							if(i%100==0)
								System.out.println("geting:"+i);
						}
					} catch (TTransportException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			jobs[z] = job;
			job.start();
		}
		
		for(int j=0;j<20;j++){
			jobs[j].join();
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
