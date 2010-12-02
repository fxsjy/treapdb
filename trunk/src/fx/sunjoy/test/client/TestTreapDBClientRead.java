package fx.sunjoy.test.client;

import java.nio.ByteBuffer;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TestTreapDBClientRead {
	public static void main(String[] args) throws Exception {
		String host ="localhost";
		if(args.length>0){host= args[0];};
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		TreapDBClient client = TreapDBClientFactory.getClient(host, 11811);
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			String v = new String(client.get("thing"+i).array());
			if(i%100==0)
				System.out.println("geting:"+i+":"+v);
		}
		System.out.println(System.currentTimeMillis()-t1);
		client.close();
	}
}
