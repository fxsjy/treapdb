package fx.sunjoy.test.unittest;

import java.io.File;

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
