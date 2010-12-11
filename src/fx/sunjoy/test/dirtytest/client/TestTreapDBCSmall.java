package fx.sunjoy.test.dirtytest.client;

import java.nio.ByteBuffer;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TestTreapDBCSmall {
	public static void main(String[] args) throws Exception {
		ByteBuffer buf = ByteBuffer.allocate(10000);
		for(int i=0;i<10000;i++){buf.put((byte)'x');};
		buf.flip();
		TreapDBClient client = TreapDBClientFactory.getClient("localhost", 11812);
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			//Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			//System.out.println(String.format("%010d", key));
			client.put("thing"+i,buf);
			//String v = treap.get("thing"+i);
			if(i%100==0)
				System.out.println("geting:"+i);
		}
		System.out.println(System.currentTimeMillis()-t1);
		client.close();
	}
}
