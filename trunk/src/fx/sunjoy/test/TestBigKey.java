package fx.sunjoy.test;

import java.io.File;
import java.nio.ByteBuffer;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestBigKey {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/sunjoy";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<String, String> treap = new DiskTreap<String, String>(554,new File(path));
		ByteBuffer buf = ByteBuffer.allocate(100);
		for(int i=0;i<100;i++){buf.put((byte)'x');};
		buf.flip();
		String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			String key = getRandomStr(512);
			//System.out.println(key);
			treap.put(key, data);
			if(i%100==0){
				System.out.println("inserting: "+i);
			}
		}
		System.out.println("put cost:"+ (System.currentTimeMillis()-t1)+" ms");
	}

	private static String getRandomStr(int size) {
		ByteBuffer buf = ByteBuffer.allocate(size);
		int idx = (int)(Math.random()*26);
		char[] ary = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
		for(int i=0;i<size;i++){
			buf.put((byte)ary[idx]);
			idx = (int)(Math.random()*26);
		};
		buf.flip();
		String data = new String(buf.array());
		return data;
	}
}
