package fx.sunjoy.test.unittest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	
	public void testPrefix() throws Exception
	{
		
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
	 
		Map<String, Integer> result = strKeyIndex.prefix("aa", 1) ;
		assertNotNull(result) ;
		assertEquals(1, result.size()) ;
		assertEquals(123, (int)result.get("aab")) ;
		
		result = strKeyIndex.prefix("I", 1) ;
		assertNotNull(result) ;
		assertEquals(1, result.size()) ;
		assertEquals(456, (int)result.get("IBM")) ;
		
		result = strKeyIndex.prefix("18", 1) ;
		assertNotNull(result) ;
		assertEquals(1, result.size()) ;
		assertEquals(789, (int)result.get("18 year")) ;
		
		result = strKeyIndex.prefix("你好世", 1) ;
		assertNotNull(result) ;
		assertEquals(1, result.size()) ;
		assertEquals(7777, (int)result.get("你好世界")) ;
		
		strKeyIndex.close();
		
		//=======
		
		/*DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		
		Map<Integer, String> result_2 = intKeyIndex.prefix(-12, 1) ;
		assertNotNull(result_2) ;
		assertEquals(1, result_2.size()) ;
		System.out.println(result_2.size()) ;
		assertEquals("abc", result_2.get(-12)) ;
		
		result_2 = intKeyIndex.prefix(3, 1) ;
		assertNotNull(result_2) ;
		assertEquals(1, result_2.size()) ;
		System.out.println(result_2.size()) ;
		assertEquals("中国", result_2.get(345)) ;
		
		result_2 = intKeyIndex.prefix(0, 1) ;
		assertNotNull(result_2) ;
		assertEquals(1, result_2.size()) ;
		System.out.println(result_2.size()) ;
		assertEquals("zzz", result_2.get(0)) ;
		
		result_2 = intKeyIndex.prefix(13, 1) ;
		assertNotNull(result_2) ;
		assertEquals(1, result_2.size()) ;
		System.out.println(result_2.size()) ;
		assertEquals("def", result_2.get(133)) ;
		
		intKeyIndex.close();*/
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		
		Map<FastString, byte[]> result_3 = fastStringIndex.prefix(new FastString("aa"), 1) ;
		assertNotNull(result_3) ;
		assertEquals(1, result_3.size()) ;
		assertEquals("123", new String(result_3.get(new FastString("aab")))) ;
		
		result_3 = fastStringIndex.prefix(new FastString("IB"), 1) ;
		assertNotNull(result_3) ;
		assertEquals(1, result_3.size()) ;
		assertEquals("456", new String(result_3.get(new FastString("IBM")))) ;
		
		result_3 = fastStringIndex.prefix(new FastString("中科"), 1) ;
		assertNotNull(result_3) ;
		assertEquals(1, result_3.size()) ;
		assertEquals("11789", new String(result_3.get(new FastString("中科院计算所")))) ;
		
		result_3 = fastStringIndex.prefix(new FastString("18"), 1) ;
		assertNotNull(result_3) ;
		assertEquals(1, result_3.size()) ;
		assertEquals("你好", new String(result_3.get(new FastString("18 year")))) ;
		
		fastStringIndex.close();
	}
	
	public void testKmin() throws Exception
	{
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
		Map<String, Integer> result = strKeyIndex.kmin(1) ;
		assertNotNull(result) ;
		assertEquals(new Integer(789), result.get("18 year")) ;
		
		strKeyIndex.close() ;
		
		//=======
		
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		
		Map<Integer, String> result_2 = intKeyIndex.kmin(1) ;
		assertNotNull(result_2) ;
		assertEquals("abc", result_2.get(-12)) ;
 		
		intKeyIndex.close() ;
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		Map<FastString, byte[]> result_3 = fastStringIndex.kmin(1) ;
		assertNotNull(result_3) ;
		assertEquals("11789", new String(result_3.get(new FastString("中科院计算所")))) ;
		
		fastStringIndex.close() ;
		
	}
	
	public void testKmax() throws Exception{
		
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
		
		Map<String, Integer> result = strKeyIndex.kmax(1) ;
		assertNotNull(result) ;
		assertEquals(7777, (int)result.get("你好世界")) ;
		strKeyIndex.close() ;
		
		//=======
		
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		Map<Integer, String> result_2 = intKeyIndex.kmax(1) ;
		assertNotNull(result_2) ;
		assertEquals("中国", result_2.get(345)) ;
		
		intKeyIndex.close() ;
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		Map<FastString, byte[]> result_3 = fastStringIndex.kmax(1) ;
		assertNotNull(result_3) ;
		assertEquals("123", new String(result_3.get(new FastString("aab")))) ;
		
		fastStringIndex.close() ;
		
	}
	
	public void testRange() throws Exception{
		
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
		Map<String, Integer> result = strKeyIndex.range("18 year", "aab", 1) ;
		assertNotNull(result) ;
		assertEquals(789, (int)result.get("18 year")) ;
		
		strKeyIndex.close() ;
		
		//=======
		
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		Map<Integer, String> result_2 = intKeyIndex.range(0, 345, 1) ;
		assertNotNull(result_2) ;
		assertEquals("zzz", result_2.get(0)) ;
		intKeyIndex.close() ;
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		Map<FastString, byte[]> result_3 = fastStringIndex.range(new FastString("18 year"), new FastString("aab"), 1) ;
		assertNotNull(result_3) ;
		assertEquals("你好", new String(result_3.get(new FastString("18 year")))) ;
		
		fastStringIndex.close() ;
		
	}
	
	public void testAfter() throws Exception{
		
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
		Map<String, Integer> result = strKeyIndex.after("18 year", 2) ;
		assertNotNull(result) ;
		assertEquals(456, (int)result.get("IBM")) ;
		strKeyIndex.close() ;
		
		//=======
		
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		Map<Integer, String> result_2 = intKeyIndex.after(0, 2) ;
		assertNotNull(result_2) ;
		assertEquals("def", result_2.get(133)) ;
		intKeyIndex.close() ;
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		Map<FastString, byte[]> result_3 = fastStringIndex.after(new FastString("18 year"), 2) ;
		assertNotNull(result_3) ;
		assertEquals("456", new String(result_3.get(new FastString("IBM")))) ;
		fastStringIndex.close() ;
		
	}
	
	public void testBefore() throws Exception{
		
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(prepareDataStringKey());
		Map<String, Integer> result = strKeyIndex.before("18 year", 2) ;
		assertNotNull(result) ;
		assertEquals(789, (int)result.get("18 year")) ;
		strKeyIndex.close() ;
		
		//=======
		
		DiskTreap<Integer, String> intKeyIndex = new DiskTreap<Integer, String>(prepareDataFastIntKey());
		Map<Integer, String> result_2 = intKeyIndex.before(0, 2) ;
		assertNotNull(result_2) ;
		assertEquals("abc", result_2.get(-12)) ;
		intKeyIndex.close() ;
		
		//=======
		
		DiskTreap<FastString, byte[]> fastStringIndex = new DiskTreap<FastString, byte[]>(prepareDataFastStringKey());
		Map<FastString, byte[]> result_3 = fastStringIndex.before(new FastString("18 year"), 2) ;
		assertNotNull(result_3) ;
		assertEquals("11789", new String(result_3.get(new FastString("中科院计算所")))) ;
		fastStringIndex.close() ;
		
	}
	
	public void testLength() throws Exception{
		File strKeyIndexFile = prepareDataStringKey();
		DiskTreap<String, Integer> strKeyIndex = new DiskTreap<String, Integer>(strKeyIndexFile);
		assertEquals(strKeyIndex.length()	, 4);
		strKeyIndex.close();
	}
	
	public void testBulkPrefix() throws Exception{
		File indexFile = new File("tmp/bulk_prefix");
		indexFile.delete();
		DiskTreap<FastString, String> index = new DiskTreap<FastString, String>(indexFile);
		for(int i=0;i<1000;i++){
			for(int j=0;j<10;j++){
				index.put(new FastString("user"+i+":"+j), "hello");
			}
		}
		List<String> prefixList = new ArrayList<String>();
		prefixList.add("user1:");
		prefixList.add("user7:");
		prefixList.add("user8:");
		//long t1 = System.currentTimeMillis();
		//System.out.println(index.bulkPrefix(prefixList, 2, null, false));
		//index.prefix(new FastString("user1:"),30);
		//System.out.println(System.currentTimeMillis()-t1);
		assertEquals(
				"{user8:9=hello, user8:8=hello, user7:9=hello, user7:8=hello, user1:9=hello, user1:8=hello}",
				index.bulkPrefix(prefixList, 2, null, false).toString());
		//System.out.println(index.bulkPrefix(prefixList, 3, null, true));
		assertEquals(
				"{user1:0=hello, user1:1=hello, user1:2=hello, user7:0=hello, user7:1=hello, user7:2=hello, user8:0=hello, user8:1=hello, user8:2=hello}",
				index.bulkPrefix(prefixList, 3, null, true).toString());
		//System.out.println(index.bulkPrefix(prefixList, 3,new FastString("user8:9"),false));
		assertEquals(
				"{user8:9=hello, user8:8=hello, user8:7=hello, user7:9=hello, user7:8=hello, user7:7=hello, user1:9=hello, user1:8=hello, user1:7=hello}",
				index.bulkPrefix(prefixList, 3, new FastString("user8:9"),
						false).toString());
		index.close();
	}
	
}
