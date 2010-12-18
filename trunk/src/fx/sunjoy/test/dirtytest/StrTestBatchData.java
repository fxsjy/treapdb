package fx.sunjoy.test.dirtytest;

import java.io.File;

import fx.sunjoy.algo.impl.DiskTreap;

public class StrTestBatchData {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/sunjoy";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<String, String> treap = new DiskTreap<String,String>(64,new File(path));
		//List<Integer> keyList = new ArrayList<Integer>();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			//Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			Integer value = i;
			treap.put(""+i, value.toString());
			//keyList.add(key);
			if(i%100==0){
				System.out.println("inserting: "+i);
			}
		}
		System.out.println("put cost:"+ (System.currentTimeMillis()-t1)+" ms");
		
		
		
		treap.close();
		
	}
}
