package fx.sunjoy.algo;

import java.util.List;
import java.util.Map;

public interface ITreap<K extends Comparable<K>, V> {

	//写入
	public abstract void put(K key, V value);

	//读出
	public abstract V get(K key);
	
	public abstract Map<K,V> bulkGet(List<K> keys);
	
	public abstract void bulkPut(Map<K,V> pairs);
	
	//范围查询
	public abstract Map<K,V> range(K start, K end,int limit);

	//长度
	public abstract int length();

	//删除
	public abstract boolean delete(K key);

	//前缀
	public abstract Map<K,V> prefix(K prefixString,int limit,K startK, boolean asc);
	
	//批量前缀
	public abstract Map<K,V> bulkPrefix(List<String> prefixList, int limit,K startK, boolean asc);
	//<=key的
	public abstract Map<K,V> before(K key,int limit);
	
	//>=key的
	public abstract Map<K,V> after(K key,int limit);
	
	public Map<K,V> kmin(int k);
	
	public Map<K,V> kmax(int k);

}