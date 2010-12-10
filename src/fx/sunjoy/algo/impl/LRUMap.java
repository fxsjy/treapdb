package fx.sunjoy.algo.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LRUMap<K,V> extends LinkedHashMap<K,V> {

    /**
         * 
         */
        private static final long serialVersionUID = -6791021890755690917L;
        private int maxCapacity;
        private ReentrantLock lock = new ReentrantLock();

    // ------------------------------------------------------------ Constructors

    public LRUMap(int maxCapacity) {
        super(maxCapacity, 0.75f, true);
        this.maxCapacity = maxCapacity;        
    }

    // ---------------------------------------------- Methods from LinkedHashMap

    protected boolean removeEldestEntry(@SuppressWarnings("rawtypes") Map.Entry eldest) {
        return (size() > maxCapacity);   
    }
    
    @Override
    //不用读写锁，因为LRU原理中read也会引发修改操作
    public V get(Object key) {
    	// TODO Auto-generated method stub
    	try{
    		lock.lock();
    		return super.get(key);
    	}finally{
    		lock.unlock();
    	}
    }
    
    public V put(K key, V value) {
    	try{
    		lock.lock();
    		return super.put(key, value);
    	}finally{
    		lock.unlock();
    	}
    };
    // TEST: com.sun.faces.TestLRUMap_local
}
