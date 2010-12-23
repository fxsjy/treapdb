package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestBugBulkWrite {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/bulkput";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		
		long t1 = System.currentTimeMillis();
		Map<FastString, Serializable> writeBuffer = new TreeMap<FastString, Serializable>();
		
		for(int i=0;i<270001;i++){
			writeBuffer.put(new FastString(""+String.format("%04d", i)), buf.array());
			if(i%3000==0){
				treap.bulkPut(writeBuffer);
				writeBuffer.clear();
			}
			if(i%100==0){
				System.out.println("geting:"+i);
			}
		}
		
		//Set<FastString> keys = treap.kmin(1000).keySet();
		for(int i=0;i<1000;i++){
			Serializable value = treap.get(new FastString(""+String.format("%04d", i)));
			System.out.println(i+""+value);
		}
		System.out.println("length:"+treap.length());
		System.out.println(System.currentTimeMillis()-t1);
	}
}
