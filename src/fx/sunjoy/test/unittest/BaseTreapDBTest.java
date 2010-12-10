package fx.sunjoy.test.unittest;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class BaseTreapDBTest extends TestCase {
	
	private File prepareDataStringKey() throws Exception{
		File indexFile = new File("tmp/str_key_index");
		indexFile.delete();
		DiskTreap<String, Integer> treap = new DiskTreap<String, Integer>(indexFile);
		treap.put("aab", 123);
		treap.put("IBM", 456);
		treap.put("18 year", 789);
		treap.put("你好世界", 7777);
		treap.close();
		return indexFile;
	}
	
	
	private File prepareDataFastStringKey() throws Exception{
		File indexFile = new File("tmp/fast_str_key_index");
		indexFile.delete();
		DiskTreap<FastString, byte[]> treap = new DiskTreap<FastString, byte[]>(indexFile);
		treap.put(new FastString("aab"), "123".getBytes());
		treap.put(new FastString("IBM"), "456".getBytes());
		treap.put(new FastString("中科院计算所"), "11789".getBytes());
		treap.put(new FastString("18 year"), "你好".getBytes());
		treap.close();
		return indexFile;
	}
	
	private File prepareDataFastIntKey() throws Exception{
		File indexFile = new File("tmp/int_key_index");
		indexFile.delete();
		DiskTreap<Integer, String> treap = new DiskTreap<Integer, String>(indexFile);
		treap.put(-12,"abc");
		treap.put(345, "中国");
		treap.put(0,"zzz");
		treap.put(133,"def");
		treap.close();
		return indexFile;
	}
	
	public void testBulkGet() throws Exception{
		File strKeyIndexFile = prepareDataStringKey();
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(strKeyIndexFile);
		String result =  strKeyIndex.bulkGet(Arrays.asList(new String[]{"IBM","18 year","haha","你好世界"})).toString() ;
		assertEquals(result, "{18 year=789, IBM=456, 你好世界=7777}");
		strKeyIndex.close();
		//=======
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		result =  intKeyIndex.bulkGet(Arrays.asList(new Integer[]{-12,345,133,99,0})).toString() ;
		assertEquals(result, "{-12=abc, 0=zzz, 133=def, 345=中国}");
		intKeyIndex.close();
		//=======
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		int size =  fastStringIndex.bulkGet(Arrays.asList(new FastString[]{new FastString("18 year"),new FastString("IBM")})).size() ;
		assertEquals(size, 2);
		fastStringIndex.close();
	}
	
	public void testBlukPut() throws Exception{
		File indexFile = new File("tmp/bulkput");
		indexFile.delete();
		
		DiskTreap<String, Integer> index = new DiskTreap<String, Integer>(indexFile);
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("foo", 1);
		map.put("footbar", 2);
		map.put("azb", 3);
		map.put("dddddd", 4);
		map.put("aojiao", 5);
		
		Map<String,Integer> map2 = new HashMap<String, Integer>();
		map2.put("azb", 13);
		map2.put("footbar", 14);
		map2.put("sjy", 140);
		
		index.bulkPut(map);
		index.bulkPut(map2);
		index.close();
		
		index = new DiskTreap<String, Integer>(indexFile);
		assertEquals(index.prefix("a", 10).toString(), "{aojiao=5, azb=13}");
		assertEquals(index.prefix("f", 10).toString(), "{foo=1, footbar=14}");
		assertEquals(index.get("dddddd").toString(),"4");
		assertEquals(index.length(),6);
		index.close();
	}
	
	public void testGet() throws Exception{
		File strKeyIndexFile = prepareDataStringKey();
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(strKeyIndexFile);
		assertEquals(strKeyIndex.get("aab"),new Integer(123));
		assertEquals(strKeyIndex.get("IBM"),new Integer(456));
		assertEquals(strKeyIndex.get("18 year"),new Integer(789));
		assertEquals(strKeyIndex.get("你好世界"),new Integer(7777));
		strKeyIndex.close();
		strKeyIndexFile.delete();
		//=======
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		assertEquals(intKeyIndex.get(-12), "abc");
		assertEquals(intKeyIndex.get(0), "zzz");
		assertEquals(intKeyIndex.get(133), "def");
		assertEquals(intKeyIndex.get(345), "中国");
		intKeyIndex.close();
		//=======
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		assertEquals(new String(fastStringIndex.get(new FastString("aab"))), "123");
		assertEquals(new String(fastStringIndex.get(new FastString("IBM"))), "456");
		assertEquals(new String(fastStringIndex.get(new FastString("中科院计算所"))), "11789");
		assertEquals(new String(fastStringIndex.get(new FastString("18 year"))), "你好");
		fastStringIndex.close();
	}
	
	public void testPrefix(){
		
	}
	
	public void testKmin(){
		
	}
	
	public void testKmax(){
		
	}
	
	public void testRange(){
		
	}
	
	public void testAfter(){
		
	}
	
	public void testBefore(){
		
	}
	
	public void testLength() throws Exception{
		File strKeyIndexFile = prepareDataStringKey();
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(strKeyIndexFile);
		assertEquals(strKeyIndex.length()	, 4);
	}
	
}
