package fx.sunjoy.test;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestBatchRead {
	public static void main(String[] args) throws Exception {
		DiskTreap<String, Serializable> treap = new DiskTreap<String,Serializable>(64,new File("c:/test/treapdb"),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		//String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			//System.out.println(String.format("%010d", key));
			treap.put("thing"+String.format("%010d", key),buf.array());
			//String v = treap.get("thing"+i);
			if(i%100==0)
				System.out.println("geting:"+i);
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
