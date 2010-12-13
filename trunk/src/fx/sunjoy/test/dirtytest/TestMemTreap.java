package fx.sunjoy.test.dirtytest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fx.sunjoy.algo.ITreap;
import fx.sunjoy.algo.impl.MemTreap;

public class TestMemTreap {

	public static void main(String[] args) {
		ITreap<Integer, Integer> treap = new MemTreap<Integer,Integer>();
		List<Integer> keyList = new ArrayList<Integer>();
		
		for(int i=0;i<10000;i++){
			Integer key = (int) (Math.random()*Integer.MAX_VALUE);
			Integer value = i;
			treap.put(key, value);
			keyList.add(key);
		}
		for(int i=0;i<10000;i++){
			Integer key = keyList.get(i);
			System.out.println(treap.get(key));
		}
		
		//System.out.println(treap.get(123));
		System.out.println("length:"+treap.length());
		
		ITreap<String, Integer> treap2 = new MemTreap<String, Integer>();
		
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			String key = (int) (Math.random()*Integer.MAX_VALUE)+"";
			Integer value = i;
			treap2.put(key, value);
		}
		System.out.println("treap cost:"+(System.currentTimeMillis()-t1));
		
		TreeMap<String,Integer> aMap = new TreeMap<String, Integer>();
		t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			String key = (int) (Math.random()*Integer.MAX_VALUE)+"";
			Integer value = i;
			aMap.put(key, value);
		}
		System.out.println("treemap cost:"+(System.currentTimeMillis()-t1));
		
		/*for(int i=0;i<1000000;i++){
			String key = keyList2.get(i);
			System.out.println(treap2.get(key));
		}*/
		
		System.out.println("length:"+treap2.length());
		
		t1 = System.currentTimeMillis();
		System.out.println("=======range search=========");
		Map<Integer,Integer> result = treap.range(0,2000000,5);
		for(Integer n : result.keySet()){
			System.out.println(n+":"+result.get(n));
		}
		System.out.println("range search cost:"+(System.currentTimeMillis()-t1));
		
		t1 = System.currentTimeMillis();
		System.out.println("=======prefix search=========");
		Map<String,Integer> result2 = treap2.prefix("123",5,"",true);
		for(String n : result2.keySet()){
			System.out.println(n+":"+result2.get(n));
		}
		System.out.println("prefix search cost:"+(System.currentTimeMillis()-t1));
	}

}
