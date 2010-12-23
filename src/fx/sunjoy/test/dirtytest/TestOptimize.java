package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestOptimize {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/opt";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),20<<20);
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<200000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			FastString realKey = new FastString("thing"+String.format("%010d", key));
			treap.put(realKey,buf.array());
			if(i%10000==0){
				treap.optimize(1024);
			}
			if(i%100==0){
				System.out.println("inserting "+i);
			}
		}
		System.out.println("length:"+treap.length());
		System.out.println(System.currentTimeMillis()-t1);
	}
}
