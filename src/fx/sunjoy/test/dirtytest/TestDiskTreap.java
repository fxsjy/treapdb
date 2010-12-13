package fx.sunjoy.test.dirtytest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.ITreap;
import fx.sunjoy.algo.impl.DiskTreap;

public class TestDiskTreap {
	public static void main(String[] args) throws Exception{
		DiskTreap<Integer, Integer> treap = new DiskTreap<Integer,Integer>(new File("c:/test/sunjoy2"));
		List<Integer> keyList = new ArrayList<Integer>();
		System.out.println("length:"+treap.length());
		long t1 = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			key = i;
			Integer value = i;
			treap.put(key, value);
			keyList.add(key);
			if(i%1000==0){
				System.out.println("inserting: "+i);
			}
		}
		System.out.println("put cost:"+ (System.currentTimeMillis()-t1)+" ms");
		t1 = System.currentTimeMillis();
		for(int i=0;i<10000;i++){
			Integer key = keyList.get(i);
			if(i%100==0){
				System.out.println("getting: "+i+":"+treap.get(key));
			}
		}
		
		System.out.println("get cost:"+ (System.currentTimeMillis()-t1)+" ms");
		//System.out.println(treap.get(123));
		System.out.println("length:"+treap.length());
		
		System.out.println("====range search====");
		Map<Integer,Integer> result = treap.range(20, 40,5);
		for(Entry<Integer,Integer> e : result.entrySet()){
			System.out.println("key:"+e.getKey()+",value:"+e.getValue());
		}
		
		System.out.println("====kmin search====");
		result = treap.kmin(100);
		for(Entry<Integer,Integer> e : result.entrySet()){
			System.out.println("key:"+e.getKey()+",value:"+e.getValue());
		}
		

		System.out.println("====kmax search====");
		result = treap.kmax(100);
		for(Entry<Integer,Integer> e : result.entrySet()){
			System.out.println("key:"+e.getKey()+",value:"+e.getValue());
		}
		
		
		
		ITreap<String, Integer> treap2 = new DiskTreap<String,Integer>(new File("c:/test/lxx"));
		for(int i=0;i<10000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			Integer value = i;
			treap2.put(key.toString(), value);
			keyList.add(key);
			if(i%1000==0){
				System.out.println("inserting: "+i);
			}
		}
		
		System.out.println("====prefix search====");
		Map<String,Integer> result2 = treap2.prefix("123",5,"1239881542",false);
		for(Entry<String,Integer> e : result2.entrySet()){
			System.out.println("key:"+e.getKey()+",value:"+e.getValue());
		}
		
		
		
	}
}
