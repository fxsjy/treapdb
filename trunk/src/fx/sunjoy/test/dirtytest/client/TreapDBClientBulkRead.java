package fx.sunjoy.test.dirtytest.client;

import java.util.ArrayList;
import java.util.List;

import fx.sunjoy.client.TreapDBClient;
import fx.sunjoy.client.TreapDBClientFactory;

public class TreapDBClientBulkRead {
	public static void main(String[] args) throws Exception {
		String host ="localhost";
		if(args.length>0){host= args[0];};
		
		TreapDBClient client = TreapDBClientFactory.getClient(host, 11812);
		long t1 = System.currentTimeMillis();
		List<String> keyBuffer = new ArrayList<String>();
		
		for(int i=0;i<1000000;i++){
			keyBuffer.add( new String("thing"+String.format("%010d", i)));
			if(i%1000==0){
				System.out.println("geting:"+i+":"+client.bulkGet(keyBuffer).size());
				keyBuffer.clear();
			}
		}
		System.out.println(System.currentTimeMillis()-t1);
		client.close();
	}
}
