package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestSmallKey {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/smallkey";
		if(args.length>0){
			path = args[0];
		}
		new File(path).delete();
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),64<<20);
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.put(new String("abcd").getBytes());
		buf.flip();
		//String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		List<FastString> keys = new ArrayList<FastString>();
		for(int i=0;i<1000000;i++){
			FastString realKey = new FastString(""+i);
			treap.put(realKey,buf.array());
			if(i%100==0){
				keys.add(realKey);
				System.out.println("geting:"+i);
			}
		}
		
		System.out.println(System.currentTimeMillis()-t1);
	}
}
