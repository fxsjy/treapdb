package fx.sunjoy.test.client;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TestTreapDBCSeq {
	public static void main(String[] args) throws Exception {
		ByteBuffer buf = ByteBuffer.allocate(100);
		String host ="localhost";
		int count = 100000;
		if(args.length>0){host= args[0];};
		if(args.length>1){count = Integer.parseInt(args[1]);};
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		TreapDBClient client = TreapDBClientFactory.getClient(host, 11811);
		long t1 = System.currentTimeMillis();
		System.out.println("will put "+count+" keys.");
		for(int i=0;i<count;i++){
			//Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			//System.out.println(String.format("%010d", key));
			client.put("thing"+i,buf);
			//String v = treap.get("thing"+i);
			if(i%100==0)
				System.out.println("geting:"+i);
		}
		Map<String,ByteBuffer> results = client.prefix("thing", 100);
		for(Entry<String, ByteBuffer> e: results.entrySet()){
			System.out.println(e.getKey()+":"+new String(e.getValue().array()));
		}
		System.out.println(System.currentTimeMillis()-t1);
		System.out.println("before deleting:"+client.length());
		for(int i=0;i<10;i++){
			client.remove("thing"+i);
		}
		System.out.println("after deleting:"+client.length());
		client.close();
	}
}
