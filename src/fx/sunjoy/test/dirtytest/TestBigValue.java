package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestBigValue {
	public static void main(String[] args) throws Exception {
		DiskTreap<String, Serializable> treap = new DiskTreap<String,Serializable>(64,new File("c:/test/treapdb"),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(200000);
		for(int i=0;i<125000;i++){buf.put((byte)(Math.random() * 256));};
		buf.flip();
		byte[] data = buf.array();
		
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			//System.out.println(String.format("%010d", key));
			treap.put("thing"+String.format("%010d", key),data);
			//String v = treap.get("thing"+i);
			if(i%100==0)
				System.out.println("geting:"+i);
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
