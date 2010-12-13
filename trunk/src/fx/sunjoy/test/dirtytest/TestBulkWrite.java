package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestBulkWrite {
	public static void main(String[] args) throws Exception {
		String path = "H:/test/treapdb";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		//String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		Map<FastString, Serializable> writeBuffer = new TreeMap<FastString, Serializable>();
		
		for(int i=0;i<1000001;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			writeBuffer.put(new FastString("thing"+String.format("%010d", key)), buf.array());
			if(i%10000==0){
				treap.bulkPut(writeBuffer);
				writeBuffer.clear();
			}
			if(i%100==0){
				System.out.println("geting:"+i);
			}
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
