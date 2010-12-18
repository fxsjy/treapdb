package fx.sunjoy.test.dirtytest;

import java.io.File;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestRemovePrefix {
	public static void main(String[] args) throws Exception {
		new File("c:/test/sunjoy3").delete();
		DiskTreap<String, Integer> treap = new DiskTreap<String,Integer>(new File("c:/test/sunjoy3"));
		for(int i=0;i<100;i++){
			treap.put("thing"+i, i);
		}
		System.out.println(treap.length());
		for(int i=0;i<9;i++){
			treap.removePrefix("thing"+i);
		}
		System.out.println(treap.length());
		System.out.println(treap.kmax(100));
		treap.delete("thing9");
		System.out.println(treap.length());
		System.out.println(treap.kmax(100));
	}
}
