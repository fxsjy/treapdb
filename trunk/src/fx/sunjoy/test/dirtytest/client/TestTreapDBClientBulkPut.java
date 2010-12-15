package fx.sunjoy.test.dirtytest.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TestTreapDBClientBulkPut {
	public static void main(String[] args) throws Exception {
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		
		String host ="localhost";
		int count = 10000001;
		if(args.length>0){host= args[0];};
		if(args.length>1){count = Integer.parseInt(args[1]);};
		
		
		TreapDBClient client = TreapDBClientFactory.getClient(host, 11812);
		
		
		long t1 = System.currentTimeMillis();
		Map<String,ByteBuffer> batchData = new HashMap<String, ByteBuffer>();
		
		for(int i=0;i<count;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			batchData.put("thing"+key,buf);
			if(i%10000==0){
				client.bulkPut(batchData);
				batchData = new HashMap<String, ByteBuffer>();//reset
			}
			if(i%100==0){
				System.out.println("inserting:"+i);
			}
		}
		System.out.println(System.currentTimeMillis()-t1);
		client.close();
	}
}
