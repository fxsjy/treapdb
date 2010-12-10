package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fx.sunjoy.algo.impl.DiskTreap;

public class TestDelete {
	public static void main(String[] args) throws Exception {
		DiskTreap<Integer, Integer> treap = new DiskTreap<Integer,Integer>(new File("c:/test/sunjoy2"));
		int oldLength = treap.length();
		System.out.println("old length:"+oldLength);
		for(int i=0;i<100;i++){
			treap.put(i, i);
		}
		System.out.println("after inserting:"+treap.length());
		
		for(int i=0;i<100;i+=2){
			treap.delete(i);
		}
		
		System.out.println("after deleting:"+treap.length());
		
		for(int i=0;i<100;i++){
			System.out.println( treap.get(i) );
		}
		
		List<Integer> keyList = new ArrayList<Integer>();
		for(int i=0;i<10;i++){
			Integer key = (int)(Math.random()*Integer.MAX_VALUE);
			keyList.add(key);
			treap.put(key, i);
		}
		System.out.println("after inserting:"+treap.length());
		
		for(Integer key: keyList){
			treap.delete(key);
		}
		
		System.out.println("after deleting:"+treap.length());
	}
}
