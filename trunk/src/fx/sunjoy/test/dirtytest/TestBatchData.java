package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestBatchData {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/sunjoy";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<Integer, Integer> treap = new DiskTreap<Integer,Integer>(64,new File(path),128<<20);
		List<Integer> keyList = new ArrayList<Integer>();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			key = i;
			Integer value = i;
			treap.put(key, value);
			if(i%100==0){
				keyList.add(key);
				System.out.println("inserting: "+i);
			}
		}
		System.out.println("put cost:"+ (System.currentTimeMillis()-t1)+" ms");
		
		t1 = System.currentTimeMillis();
		for(int i=0;i<1000;i++){
			Integer key = keyList.get(i);
			if(i%100==0){
				System.out.println("getting: "+i+":"+treap.get(key));
			}
		}
		
		System.out.println("get cost:"+ (System.currentTimeMillis()-t1)+" ms");
		//System.out.println(treap.get(123));
		System.out.println("length:"+treap.length());
		
		treap.close();
	}
}
