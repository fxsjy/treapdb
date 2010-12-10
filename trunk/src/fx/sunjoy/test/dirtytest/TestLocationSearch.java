package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class TestLocationSearch {
	public static void main(String[] args) throws Exception {
		String path = "c:/test/location";
		if(args.length>0){
			path = args[0];
		}
		DiskTreap<FastString, Serializable> treap = new DiskTreap<FastString,Serializable>(64,new File(path),64<<20);
		for(int i=1;i<=10;i++){
			treap.put(new FastString(i+""), i);
		}
		
		System.out.println(treap.before(new FastString("4"), 5));
		System.out.println(treap.after(new FastString("4"), 5));
		
		DiskTreap<Integer, Serializable> treap2 = new DiskTreap<Integer,Serializable>(64,new File("c:/test/location2"),64<<20);
		for(int i=1;i<=10;i++){
			treap2.put(i, i);
		}
		
		System.out.println(treap2.before(4, 5));
		System.out.println(treap2.after(4, 5));
		
	}
}
