package fx.sunjoy.test.dirtytest;

import java.nio.ByteBuffer;

import fx.sunjoy.algo.impl.MemTreap;

public class TestMemTreap2 {
	public static void main(String[] args) {
		MemTreap<String, String> treap = new MemTreap<String, String>();
		ByteBuffer buf = ByteBuffer.allocate(10);
		for(int i=0;i<10;i++){buf.put((byte)'x');};
		buf.flip();
		String data = new String(buf.array());
		long t1 = System.currentTimeMillis();
		for(int i=0;i<10000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			treap.put("thing"+key,data);
			//String v = treap.get("thing"+i);
			if(i%100==0)
				System.out.println("geting:"+i);
		}
		System.out.println(System.currentTimeMillis()-t1);
	}
}
