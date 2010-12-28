package fx.sunjoy.algo.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fx.sunjoy.algo.ITreap;
import fx.sunjoy.utils.BlockUtil;
import fx.sunjoy.utils.ByteUtil;



public class DiskTreap<K extends Comparable<K>,V extends Serializable> implements ITreap<K, V> {
	
	private static final int DEFAULT_BLOCK_SIZE = 165;

	//用于读写文件
	private final BlockUtil<K, V> blockUtil;
	
	//控制读写并发，读时可以读，读时不能写，写时不能读
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	
	private final LRUMap<K, V> valueCache = new LRUMap<K, V>(100000);
	private final LRUMap<K, V> bigValueCache = new LRUMap<K, V>(100);
	
	//用户可以自行设置block_size来调整索引item的大小,默认为DEFAULT_BLOCK_SIZE
	public DiskTreap(int block_size, File _indexFile,long mmapSize) throws Exception{
		this.blockUtil = new BlockUtil<K,V>(block_size, _indexFile,mmapSize);
	}
	
	public DiskTreap(int block_size, File _indexFile) throws Exception{
		this.blockUtil = new BlockUtil<K,V>(block_size, _indexFile,64<<20);
	}
	
	public DiskTreap(File _indexFile) throws Exception{
		this(DEFAULT_BLOCK_SIZE,_indexFile,64<<20);
	}
	
	public void finalize(){
		this.close();
	}
	
	//关闭索引和数据文件
	public void close(){
		try{
			if(this.blockUtil!=null)this.blockUtil.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//写入
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#put(K, V)
	 */
	@Override
	public void put(K key,V value){
		DiskTreapHeader header;
		commitToCache(key, value);
		try {
			lock.writeLock().lock();
			header = this.blockUtil.readHeader();
			int rootNo = insert(header.rootNo,key,value);
			header = this.blockUtil.readHeader();
			header.rootNo = rootNo;
			this.blockUtil.writeHeader(header);
			//System.out.println("rootNo:"+rootNo);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.writeLock().unlock();
		}
	}

	private void commitToCache(K key, V value) {
		if(ByteUtil.isSmallObj(value)){
			valueCache.put(key, value);
			bigValueCache.remove(key);
		}else{
			valueCache.remove(key);
			bigValueCache.put(key, value);
		}
	}
	
	public void optimize(int swapCount){
		DiskTreapHeader header;
		try {
			lock.writeLock().lock();
			header = this.blockUtil.readHeader();
			int rootNo = header.rootNo;
			if(rootNo==-1)return;
			
			//1.找出Treap的前n层节点：集合A
			Queue<DiskTreapNode<K, V>> setA = new LinkedList<DiskTreapNode<K, V>>();
			Queue<Integer> queue = new LinkedList<Integer>();
			Queue<DiskTreapNode<K, V>> setB = new LinkedList<DiskTreapNode<K,V>>();
			Set<K> bucketA = new TreeSet<K>();
			Set<K> bucketB = new TreeSet<K>();
			
			queue.add(rootNo);
			int treapLength = this.length();
			
			while(!queue.isEmpty()){
				int nodeNumber = queue.poll();
				DiskTreapNode<K,V> tmpNode = this.blockUtil.readNode(nodeNumber,false);
				setA.add(tmpNode);
				bucketA.add(tmpNode.key);
				if(tmpNode.lNo!=-1){
					queue.add(tmpNode.lNo);
				}
				if(tmpNode.rNo!=-1){
					queue.add(tmpNode.rNo);
				}
				if(setA.size()>=treapLength || setA.size()>=swapCount){
					break;
				}
			}
			
			//2.找出在IndexFile中排在前面的setA.size()个节点: 集合B
			int z = 0;
			while(setB.size()<setA.size()){
				DiskTreapNode<K,V> tmpNode = this.blockUtil.readNode(z, false);
				if(tmpNode.fix!=Integer.MAX_VALUE){//not deleted node
					setB.offer(tmpNode);
					bucketB.add(tmpNode.key);
				}
				z++;
			}
			
			List<DiskTreapNode<K,V>> setA_B = new ArrayList<DiskTreapNode<K,V>>();
			List<DiskTreapNode<K,V>> setB_A = new ArrayList<DiskTreapNode<K,V>>();
			for(DiskTreapNode<K,V> node: setA){
				if(!bucketB.contains(node.key)){
					setA_B.add(node);
				}
			}
			for(DiskTreapNode<K,V> node: setB){
				if(!bucketA.contains(node.key)){
					setB_A.add(node);
				}
			}
			System.out.println(setA_B.size()+":"+setB_A.size());
			//3.交换A-B与B-A
			for(int i=0;i<setA_B.size();i++){
				swapNode(setA_B.get(i).key,setB_A.get(i).key);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.writeLock().unlock();
		}
		
	}
	
	//交换两个节点的物理位置
	private void swapNode(K k_A,K k_B) throws Exception {
		Map<Integer,Integer> pAMap = new HashMap<Integer,Integer>();
		Map<Integer,Integer> pBMap = new HashMap<Integer,Integer>();
		Map<Integer,Integer> pADirectionMap = new HashMap<Integer, Integer>();
		Map<Integer,Integer> pBDirectionMap = new HashMap<Integer, Integer>();
		DiskTreapHeader header;
		header = this.blockUtil.readHeader();
		int rootNo = header.rootNo;
		
		int A_NO = findWithParent(rootNo, k_A, pAMap,pADirectionMap);
		int B_NO = findWithParent(rootNo, k_B, pBMap,pBDirectionMap);
		
		Integer PA_NO = pAMap.get(A_NO);
		Integer PB_NO = pBMap.get(B_NO);
		
		DiskTreapNode<K,V> nodeA = this.blockUtil.readNode(A_NO, false);
		DiskTreapNode<K,V> nodeB = this.blockUtil.readNode(B_NO, false);
		
		DiskTreapNode<K,V> nodePA = this.blockUtil.readNode(PA_NO, false);
		if(pADirectionMap.get(A_NO)==0){//left child
			nodePA.lNo = B_NO;
		}
		else if(pADirectionMap.get(A_NO)==1){
			nodePA.rNo = B_NO;
		}else{
			header.rootNo = B_NO;
		}
		DiskTreapNode<K,V> nodePB = this.blockUtil.readNode(PB_NO, false);
		if(pBDirectionMap.get(B_NO)==0){//left child
			nodePB.lNo = A_NO;
		}
		else if(pBDirectionMap.get(B_NO)==1){
			nodePB.rNo = A_NO;
		}else{
			header.rootNo = A_NO;
		}
		if(B_NO == PA_NO){
			this.blockUtil.writeNode(B_NO, nodeA, false);
			this.blockUtil.writeNode(A_NO, nodePA, false);
		}
		else if(A_NO==PB_NO){
			this.blockUtil.writeNode(A_NO, nodeB, false);
			this.blockUtil.writeNode(B_NO, nodePB, false);
		}
		else{
			this.blockUtil.writeNode(A_NO, nodeB, false);
			this.blockUtil.writeNode(B_NO, nodeA, false);
			this.blockUtil.writeNode(PA_NO, nodePA, false);
			this.blockUtil.writeNode(PB_NO, nodePB, false);
		}
		this.blockUtil.writeHeader(header);
	}

	//读出
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#get(K)
	 */
	@Override
	public V get(K key){
		DiskTreapHeader header;
		if(valueCache.containsKey(key)){
			return valueCache.get(key);
		}else if(bigValueCache.containsKey(key)){
			return bigValueCache.get(key);
		}
		try {
			lock.readLock().lock();
			header = this.blockUtil.readHeader();
			int idx =  find(header.rootNo,key);
			if(idx==-1)return null;
			else{
				DiskTreapNode< K, V> node = this.blockUtil.readNode(idx, true);
				commitToCache(key, node.value);
				return node.value;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			lock.readLock().unlock();
		}
	}

	

	@Override
	/**
	 * 取key的范围在[start,end)区间的数据
	 */
	public Map<K,V> range(K start, K end,int limit) {
		lock.readLock().lock();
		try {
			Map<K,V> result = new LinkedHashMap<K, V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			collectRange(header.rootNo, start, end, result,limit);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	/*
	 * 取k个最小的
	 */
	public Map<K,V> kmin(int k){
		lock.readLock().lock();
		try {
			Map<K,V> result = new LinkedHashMap<K, V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			int len = length();
			if(k>len)k=len;
			if(k<0)k=0;
			collectKMin(header.rootNo, k,result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}
	
	public Map<K,V> kmax(int k){
		lock.readLock().lock();
		try {
			Map<K,V> result = new LinkedHashMap<K, V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			int len = length();
			if(k>len)k=len;
			if(k<0)k=0;
			collectKMax(header.rootNo, k,result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}
	
	private void collectKMax(int startNode, int k, Map<K, V> result) throws Exception {
		if(startNode==-1){
			return;
		}
		DiskTreapNode<K, V> node =  this.blockUtil.readNode(startNode,false);
		if(k<=node.r_size){
			collectKMax(node.rNo,k,result);
		}else if(k==node.r_size+1){
			collectKMax(node.rNo,node.r_size,result);
			this.blockUtil.filleNodeValue(node);
			result.put(node.key, node.value);
		}else{
			collectKMax(node.rNo,node.r_size,result);
			this.blockUtil.filleNodeValue(node);
			result.put(node.key, node.value);
			collectKMax(node.lNo,k-node.r_size-1,result);
		}
	}

	private void collectKMin(int startNode, int k,Map<K,V> result) throws Exception {
		if(startNode==-1){
			return;
		}
		DiskTreapNode<K, V> node =  this.blockUtil.readNode(startNode,false);
		if(k<=node.l_size){
			collectKMin(node.lNo,k,result);
		}else if(k==node.l_size+1){
			this.blockUtil.filleNodeValue(node);
			collectKMin(node.lNo,node.l_size,result);
			result.put(node.key, node.value);
		}else{
			collectKMin(node.lNo,node.l_size,result);
			this.blockUtil.filleNodeValue(node);
			result.put(node.key, node.value);
			collectKMin(node.rNo,k-node.l_size-1,result);
		}
	}

	private void collectRange(int startNode, K start, K end,Map<K,V> values,int limit) throws Exception{
		if(startNode==-1){
			return;
		}
		if(start.compareTo(end)>=0){
			throw new RuntimeException("invalid range:"+start+" to "+ end);
		}
		if(values.size()>=limit){
			return ;
		}
		DiskTreapNode<K, V> node =  this.blockUtil.readNode(startNode,false);
		int cp1 = node.key.compareTo(start);
		int cp2 = node.key.compareTo(end);
		
		if(cp1>=0 && cp2<0){
			collectRange(node.lNo, start, end, values,limit);
			if(values.size()>=limit){
				return ;
			}
			this.blockUtil.filleNodeValue(node);
			values.put(node.key, node.value);
			collectRange(node.rNo, start, end, values,limit);
		}
		if(cp1<0)
			collectRange(node.rNo, start, end, values,limit);
		if(cp2>=0)
			collectRange(node.lNo, start, end, values,limit);
	}
	
	@Override
	/**
	 * 库中数据条目数
	 */
	public int length() {
		lock.readLock().lock();
		try {
			DiskTreapHeader header = this.blockUtil.readHeader();
			int rootNo = header.rootNo;
			//System.out.println("rootNo:"+rootNo);
			if(rootNo==-1){
				return 0;
			}
			DiskTreapNode<K,V> root = this.blockUtil.readNode(rootNo, false);
			return root.l_size+1+root.r_size;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}finally{
			lock.readLock().unlock();
		}
	}

	@Override
	//这个版本的实现会有空间浪费
	public boolean delete(K key) {
		DiskTreapHeader header;
		evictFromCache(key);
		try {
			lock.writeLock().lock();
			int old_length = length();
			header = this.blockUtil.readHeader();
			int rootNo = remove(header.rootNo,key);
			header = this.blockUtil.readHeader();
			header.rootNo = rootNo;
			this.blockUtil.writeHeader(header);
			if(old_length==length()){
				return false; //not found
			}
			
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.writeLock().unlock();
		}
	}

	private void evictFromCache(K key) {
		this.valueCache.remove(key);
		this.bigValueCache.remove(key);
	}

	private int remove(int startNode, K key) throws Exception {
		if(startNode == -1) return -1;
		DiskTreapNode<K,V> currentNode = this.blockUtil.readNode(startNode, false);
		int cp = currentNode.key.compareTo(key);
		if(cp==0){
			if(currentNode.fix!=Integer.MAX_VALUE){
				currentNode.fix = Integer.MAX_VALUE;
				this.blockUtil.writeNode(startNode, currentNode, false);
				//在data文件中加入删除记录信息
				this.blockUtil.addDeleteInfo(key) ;
				pushDeletedNode(startNode, currentNode);
			}
			
			if(currentNode.lNo==-1 && currentNode.rNo==-1){
				return -1;
			}else if(currentNode.rNo==-1){
				return currentNode.lNo;
			}else if(currentNode.lNo==-1){
				return currentNode.rNo;
			}else{
				DiskTreapNode<K, V> leftNode = this.blockUtil.readNode(currentNode.lNo, false);
				DiskTreapNode<K, V> rightNode = this.blockUtil.readNode(currentNode.rNo, false);
				if(leftNode.fix<rightNode.fix){
					int newstartNodeNo = rotateRight(startNode);
					DiskTreapNode<K, V> newstartNode = this.blockUtil.readNode(newstartNodeNo, false);
					newstartNode.rNo = remove(startNode, key);
					if(newstartNode.rNo==-1){
						newstartNode.r_size = 0;
					}else{
						DiskTreapNode<K, V>  rN = this.blockUtil.readNode(newstartNode.rNo,false);
						newstartNode.r_size = 1+rN.l_size+rN.r_size;
					}
					this.blockUtil.writeNode(newstartNodeNo,newstartNode,false);
					return newstartNodeNo;
				}else{
					int newstartNodeNo = rotateLeft(startNode);
					DiskTreapNode<K, V> newstartNode = this.blockUtil.readNode(newstartNodeNo, false);
					newstartNode.lNo  = remove(startNode, key);
					if(newstartNode.lNo==-1){
						newstartNode.l_size = 0;
					}else{
						DiskTreapNode<K, V>  lN = this.blockUtil.readNode(newstartNode.lNo,false);
						newstartNode.l_size = 1+lN.l_size+lN.r_size;
					}
					this.blockUtil.writeNode(newstartNodeNo,newstartNode,false);
					return newstartNodeNo;
				}
			}
		}else if(cp<0){
			currentNode.rNo = remove(currentNode.rNo,key);
			if(currentNode.rNo==-1){
				currentNode.r_size = 0;
			}else{
				DiskTreapNode<K, V>  rN = this.blockUtil.readNode(currentNode.rNo,false);
				currentNode.r_size = 1+rN.l_size+rN.r_size;
			}
			this.blockUtil.writeNode(startNode, currentNode, false);
		}else if(cp>0){
			currentNode.lNo = remove(currentNode.lNo,key);
			if(currentNode.lNo==-1){
				currentNode.l_size = 0;
			}else{
				DiskTreapNode<K, V>  lN = this.blockUtil.readNode(currentNode.lNo,false);
				currentNode.l_size = 1+lN.l_size+lN.r_size;
			}
			this.blockUtil.writeNode(startNode, currentNode, false);
		}
		return startNode;
	}

	public Map<K,V> prefix(K prefixString,int limit) {
		return prefix(prefixString,limit,null,true);
	}
	//前缀搜索
	public Map<K,V> prefix(K prefixString,int limit,K startK,boolean asc) {
		lock.readLock().lock();
		try {
			Map<K,V> results = new LinkedHashMap<K,V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			prefixSearch(header.rootNo,prefixString,results,limit,startK,asc);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}
	
	/**
	 * 
	 * @param startNode 搜索起始的节点
	 * @param prefixString 前缀串
	 * @param results 结果搜集容器
	 * @param limit   限制，最大收集量
	 * @param startK  从startK以后开始收集
	 * @param asc     顺序还是逆序
	 * @throws Exception
	 */
	private void prefixSearch(int startNode, K prefixString,Map<K,V> results,int limit,K startK,boolean asc) throws Exception {
		if(startNode==-1)return ;
		if(results.size()>=limit)return;
		DiskTreapNode<K, V> cur = this.blockUtil.readNode(startNode, false);
		
		if(startK!=null){
			int cp = cur.key.compareTo(startK);
			if(cp<0 && asc){
				prefixSearch(cur.rNo, prefixString,results,limit,startK,asc);
				return;
			}
			if(cp>0 && !asc){
				prefixSearch(cur.lNo, prefixString,results,limit,startK,asc);
				return;
			}
		}
		if(prefixString.compareTo(cur.key)<=0){
			if(isPrefixString(prefixString.toString(),cur.key.toString())){
				if(asc){
					prefixSearch(cur.lNo, prefixString,results,limit,startK,asc);
					if(results.size()>=limit)return;
					this.blockUtil.filleNodeValue(cur);
					results.put(cur.key, cur.value);
					prefixSearch(cur.rNo, prefixString,results,limit,startK,asc);
				}else{
					prefixSearch(cur.rNo, prefixString,results,limit,startK,asc);
					if(results.size()>=limit)return;
					this.blockUtil.filleNodeValue(cur);
					results.put(cur.key, cur.value);
					prefixSearch(cur.lNo, prefixString,results,limit,startK,asc);
				}
			}else{
				prefixSearch(cur.lNo, prefixString,results,limit,startK,asc);
			}
		}else{
			prefixSearch(cur.rNo, prefixString,results,limit,startK,asc);
		}
	}
	
	private boolean isPrefixString(String prefixString, String key) {
		if(key.indexOf(prefixString)==0)return true;
		return false;
	}

	
	private void pushDeletedNode(int nodeNo,DiskTreapNode<K, V> deletedNode) throws Exception{
		DiskTreapHeader header = this.blockUtil.readHeader();
		deletedNode.valuePtr = -1;
		if(header.deletedNode==-1){
			header.deletedNode = nodeNo;
			this.blockUtil.writeNode(nodeNo, deletedNode, false);
		}else{
			int tmp = header.deletedNode;
			header.deletedNode = nodeNo;
			deletedNode.valuePtr = tmp;
			this.blockUtil.writeNode(nodeNo, deletedNode, false);
		}
		this.blockUtil.writeHeader(header);
	}
	
	private int popDeletedNode() throws Exception{
		DiskTreapHeader header = this.blockUtil.readHeader();
		if(header.deletedNode == -1){
			return -1;
		}else{
			int topNodeNo = header.deletedNode;
			DiskTreapNode<K, V> topNode = this.blockUtil.readNode(topNodeNo, false);
			int nextNodeNo = (int)topNode.valuePtr;
			header.deletedNode = nextNodeNo;
			this.blockUtil.writeHeader(header);
			return topNodeNo;
		}
	}
	
	private int insert(int startNode,K key,V value) throws Exception{
		if(startNode==-1){
			DiskTreapHeader header = this.blockUtil.readHeader();
			DiskTreapNode<K,V> newNode = new DiskTreapNode<K,V>();
			newNode.key = key;
			newNode.value = value;
			newNode.fix = (int)(Math.random()*Integer.MAX_VALUE);
			if(header.deletedNode == -1){
				this.blockUtil.writeNode(header.size, newNode, true);
				header.size++;
				this.blockUtil.writeHeader(header);
				return header.size-1;
			}else{
				int pos = popDeletedNode();
				this.blockUtil.writeNode(pos,newNode,true);
				return pos;
			}
		}else{
			DiskTreapNode<K,V> currentNode = this.blockUtil.readNode(startNode, false);
			int cp = currentNode.key.compareTo(key);
			if(cp==0){
				currentNode.value = value;
				this.blockUtil.writeNode(startNode, currentNode, true);
			}else if(cp<0){
				currentNode.rNo = insert(currentNode.rNo,key,value);
				DiskTreapNode<K, V> rightNode = this.blockUtil.readNode(currentNode.rNo, false);
				currentNode.r_size = rightNode.r_size+1+rightNode.l_size;
				this.blockUtil.writeNode(startNode, currentNode, false);
				
				if(rightNode.fix < currentNode.fix){
					startNode = rotateLeft(startNode);
				}
				
			}else if(cp>0){
				currentNode.lNo = insert(currentNode.lNo,key,value);
				DiskTreapNode< K, V> leftNode = this.blockUtil.readNode(currentNode.lNo, false);
				currentNode.l_size = leftNode.l_size+1+leftNode.r_size;
				this.blockUtil.writeNode(startNode, currentNode, false);
				
				if(leftNode.fix < currentNode.fix){
					startNode = rotateRight(startNode);
				}
			}
		}
		return startNode;
	}
	

	//右旋
	private int rotateRight(int startNode) throws Exception{
		DiskTreapNode<K,V> cur = this.blockUtil.readNode(startNode, false);
		int leftNo = cur.lNo;
		DiskTreapNode<K,V> left = this.blockUtil.readNode(leftNo, false);
		int left_right = left.rNo;
		int left_right_size  = left.r_size;
		left.rNo = startNode;
		left.r_size += cur.r_size+1;
		cur.lNo  = left_right;
		cur.l_size = left_right_size;
		this.blockUtil.writeNode(startNode, cur, false);
		this.blockUtil.writeNode(leftNo, left, false);
		return leftNo;
	}
	
	//左旋
	private int rotateLeft(int startNode) throws Exception{
		DiskTreapNode<K,V> cur = this.blockUtil.readNode(startNode, false);
		int rightNo = cur.rNo;
		DiskTreapNode <K,V> right = this.blockUtil.readNode(rightNo, false);
		int right_left = right.lNo;
		int right_left_size = right.l_size;
		right.lNo = startNode;
		right.l_size += cur.l_size+1;
		cur.rNo = right_left;
		cur.r_size = right_left_size;
		this.blockUtil.writeNode(startNode, cur, false);
		this.blockUtil.writeNode(rightNo, right, false);
		return rightNo;
	}
	
	private int find(int startNode,K key) throws Exception{
		if(startNode==-1){
			return -1;
		}else{
			DiskTreapNode<K, V> currentNode = this.blockUtil.readNode(startNode, false);
			int cp = currentNode.key.compareTo(key);
			if(cp==0){
				return startNode;
			}else if(cp<0){
				return find(currentNode.rNo,key);
			}else if(cp>0){
				return  find(currentNode.lNo,key);
			}
		}
		return -1;
	}
	
	private int findWithParent(int startNode,K key,Map<Integer,Integer> parentMap,Map<Integer,Integer> directionMap) throws Exception{
		if(startNode==-1){
			return -1;
		}else{
			DiskTreapNode<K, V> currentNode = this.blockUtil.readNode(startNode, false);
			int cp = currentNode.key.compareTo(key);
			if(cp==0){
				if(!parentMap.containsKey(startNode)){
					parentMap.put(startNode, -1);
					directionMap.put(startNode, -1);
				}
				return startNode;
			}else if(cp<0){
				parentMap.put(currentNode.rNo, startNode);
				directionMap.put(currentNode.rNo,1); //right child
				return findWithParent(currentNode.rNo,key,parentMap,directionMap);
			}else if(cp>0){
				parentMap.put(currentNode.lNo, startNode);
				directionMap.put(currentNode.lNo,0); //left child
				return  findWithParent(currentNode.lNo,key,parentMap,directionMap);
			}
		}
		return -1;
	}
	
	public List<V> Sync(int dataFileNO, long syncPos) throws Exception
	{
		List<V> result = this.blockUtil.getNewData(dataFileNO, syncPos) ;
		
		return result ;
	}
	
	public int getDataFileNO()
	{
		return this.blockUtil.getDataFileNO() ;
	}
	
	public long getCurrentFilePos()
	{
		return this.blockUtil.getCurrentFilePos() ;
	}
	@Override
	public Map<K, V> before(K key, int limit) {
		lock.readLock().lock();
		try {
			Map<K,V> results = new LinkedHashMap<K,V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			prevSearch(header.rootNo,key,results,limit);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	private void prevSearch(int startNode, K key, Map<K, V> results, int limit) throws Exception {
		if(startNode==-1)return ;
		if(results.size()>=limit)return;
		DiskTreapNode<K, V> cur = this.blockUtil.readNode(startNode, false);
		int cp = cur.key.compareTo(key);
		if(cp<0){
			prevSearch(cur.rNo, key, results, limit);
			if(results.size()>=limit)return;
			this.blockUtil.filleNodeValue(cur);
			results.put(cur.key, cur.value);
			prevSearch(cur.lNo, key, results, limit);
		}
		else if(cp==0){
			if(results.size()>=limit)return;
			this.blockUtil.filleNodeValue(cur);
			results.put(cur.key, cur.value);
			prevSearch(cur.lNo, key, results, limit);
		}else{
			prevSearch(cur.lNo, key, results, limit);
		}
	}

	@Override
	public Map<K, V> after(K key, int limit) {
		lock.readLock().lock();
		try {
			Map<K,V> results = new LinkedHashMap<K,V>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			nextSearch(header.rootNo,key,results,limit);
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	private void nextSearch(int startNode, K key, Map<K, V> results, int limit) throws Exception {
		if(startNode==-1)return ;
		if(results.size()>=limit)return;
		DiskTreapNode<K, V> cur = this.blockUtil.readNode(startNode, false);
		int cp = cur.key.compareTo(key);
		if(cp>0){
			nextSearch(cur.lNo, key, results, limit);
			if(results.size()>=limit)return;
			this.blockUtil.filleNodeValue(cur);
			results.put(cur.key, cur.value);
			nextSearch(cur.rNo, key, results, limit);
		}
		else if(cp==0){
			if(results.size()>=limit)return;
			this.blockUtil.filleNodeValue(cur);
			results.put(cur.key, cur.value);
			nextSearch(cur.rNo, key, results, limit);
		}else{
			nextSearch(cur.rNo, key, results, limit);
		}
	}

	@Override
	public Map<K,V> bulkGet(List<K> keys) {
		lock.readLock().lock();
		try {
			Map<K,V> result = new TreeMap<K,V>();
			TreeSet<K> keySet = new TreeSet<K>();
			
			for(K k : keys){
				if(valueCache.containsKey(k)){
					result.put(k, valueCache.get(k));
				}else if(bigValueCache.containsKey(k)){
					result.put(k, bigValueCache.get(k));
				}else{
					keySet.add(k); //cache miss
				}
			}
			
			DiskTreapHeader header = this.blockUtil.readHeader();
			realBulkGet(header.rootNo,keySet,result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	private void realBulkGet(int startNode, NavigableSet<K> keySet,Map<K,V> result) throws Exception {
		if(startNode==-1 || keySet.isEmpty())return;
		DiskTreapNode<K, V> cur = this.blockUtil.readNode(startNode, false);
		if(keySet.contains(cur.key)){
			this.blockUtil.filleNodeValue(cur);
			result.put(cur.key, cur.value);
			commitToCache(cur.key, cur.value);
		}
		NavigableSet<K> leftKeySet = keySet.headSet(cur.key,false);
		NavigableSet<K>  rightKeySet = keySet.tailSet(cur.key,false);
		realBulkGet(cur.lNo, leftKeySet, result);
		realBulkGet(cur.rNo, rightKeySet, result);
	}

	@Override
	public void bulkPut(Map<K, V> pairs) {
		DiskTreapHeader header;
		try {
			lock.writeLock().lock();
			header = this.blockUtil.readHeader();
			int rootNo = bulkInsert(header.rootNo,new TreeMap<K,V>(pairs));
			header = this.blockUtil.readHeader();
			header.rootNo = rootNo;
			this.blockUtil.writeHeader(header);	
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.writeLock().unlock();
		}
	}

	private int bulkInsert(int startNode, NavigableMap<K, V> pairs) throws Exception {
		if(pairs.isEmpty())return startNode;
		if(startNode==-1){
			for(Entry<K,V> e : pairs.entrySet()){
				//commitToCache(e.getKey(), e.getValue());
				startNode = insert(startNode, e.getKey(), e.getValue());
			}
			return startNode;
		}else{
			DiskTreapNode<K,V> currentNode = this.blockUtil.readNode(startNode, false);
			int oldNode = startNode;
			
			if(pairs.containsKey(currentNode.key)){
				currentNode.value = pairs.get(currentNode.key);
				//commitToCache(currentNode.key, currentNode.value);
				this.blockUtil.writeNode(startNode, currentNode, true);
			}
			
			NavigableMap<K, V> rightPart = pairs.tailMap(currentNode.key,false);
			NavigableMap<K, V> leftPart = pairs.headMap(currentNode.key, false);
			
			int rfix = Integer.MAX_VALUE;
			int lfix = Integer.MAX_VALUE;
			
			currentNode.rNo = bulkInsert(currentNode.rNo, rightPart);
			if(currentNode.rNo!=-1){
				DiskTreapNode<K, V> rightNode = this.blockUtil.readNode(currentNode.rNo, false);
				currentNode.r_size = rightNode.r_size+1+rightNode.l_size;
				rfix = rightNode.fix;
			}
			
			
			currentNode.lNo = bulkInsert(currentNode.lNo,leftPart);
			if(currentNode.lNo!=-1){
				DiskTreapNode< K, V> leftNode = this.blockUtil.readNode(currentNode.lNo, false);
				currentNode.l_size = leftNode.l_size+1+leftNode.r_size;
				lfix = leftNode.fix;
			}
			this.blockUtil.writeNode(oldNode, currentNode, false);
			
			if(rfix<lfix){
				if(rfix<currentNode.fix)
					startNode = rotateLeft(oldNode);
				if(lfix<currentNode.fix){
					DiskTreapNode< K, V> tmpNode = this.blockUtil.readNode(startNode, false);
					tmpNode.lNo = rotateRight(oldNode);
					this.blockUtil.writeNode(startNode,tmpNode,false);
				}
			}else{
				if(lfix<currentNode.fix)
					startNode = rotateRight(oldNode);
				if(rfix<currentNode.fix){
					DiskTreapNode< K, V> tmpNode = this.blockUtil.readNode(startNode, false);
					tmpNode.rNo = rotateLeft(oldNode);
					this.blockUtil.writeNode(startNode, tmpNode, false);
				}
			}
			
			return startNode;
		}
	}

	@Override
	public Map<K, V> bulkPrefix(List<String> prefixList, int limit, K startK,
			boolean asc) {
		lock.readLock().lock();
		try {
			Map<K,V> result = new LinkedHashMap<K,V>();
			TreeSet<String> keySet = new TreeSet<String>(prefixList);
			Map<String,Integer> counter = new TreeMap<String, Integer>();
			DiskTreapHeader header = this.blockUtil.readHeader();
			for(String p : prefixList){
				counter.put(p, 0);
			}
			realBulkPrefix(header.rootNo,keySet,result,limit,startK,asc,counter);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	
	private void realBulkPrefix(int startNode, TreeSet<String> keySet,
			Map<K, V> result, int limit, K startK, boolean asc,Map<String,Integer> counter) throws Exception {
		if(startNode==-1 || keySet.isEmpty())return;
		
		DiskTreapNode<K, V> cur  = this.blockUtil.readNode(startNode, false);
		String strKey = cur.key.toString();
		NavigableSet<String> lessPart = keySet.headSet(strKey, true);
		NavigableSet<String> greaterPart = keySet.tailSet(strKey, false);
		TreeSet<String> left = new TreeSet<String>();
		TreeSet<String> middle = new TreeSet<String>();
		TreeSet<String> right;
		
		for(String p: lessPart){
			if(isPrefixString(p, strKey)){
				middle.add(p);
			}else{
				left.add(p);
			}
		}
		
		right = new TreeSet<String>(greaterPart);
		if(asc){//顺序
			if(startK!=null && startK.compareTo(cur.key)>0){
				realBulkPrefix(cur.rNo,keySet,result,limit,startK,asc,counter);
				return;
			}
			realBulkPrefix(cur.lNo,left,result,limit,startK,asc,counter);
			realBulkPrefix(cur.lNo,middle,result,limit,startK,asc,counter);
			if(middle.size()>0){
				for(String prfix: middle){
					counter.put(prfix, counter.get(prfix)+1);
					if(counter.get(prfix)<=limit){
						this.blockUtil.filleNodeValue(cur);
						result.put(cur.key, cur.value);
					}else{
						middle.remove(prfix);
					}
				}
			}
			realBulkPrefix(cur.rNo,middle,result,limit,startK,asc,counter);
			realBulkPrefix(cur.rNo,right,result,limit,startK,asc,counter);
		}else{//逆序
			if(startK!=null && startK.compareTo(cur.key)<0){
				realBulkPrefix(cur.lNo,keySet,result,limit,startK,asc,counter);
				return;
			}
			realBulkPrefix(cur.rNo,right,result,limit,startK,asc,counter);
			realBulkPrefix(cur.rNo,middle,result,limit,startK,asc,counter);
			if(middle.size()>0){
				for(String prfix: middle){
					counter.put(prfix, counter.get(prfix)+1);
					if(counter.get(prfix)<=limit){
						this.blockUtil.filleNodeValue(cur);
						result.put(cur.key, cur.value);
					}else{
						middle.remove(prfix);
					}
				}
			}
			realBulkPrefix(cur.lNo,middle,result,limit,startK,asc,counter);
			realBulkPrefix(cur.lNo,left,result,limit,startK,asc,counter);
			
		}
	}

	@Override
	public boolean removePrefix(K prefixString) {
		DiskTreapHeader header;
		try {
			lock.writeLock().lock();
			int old_length = length();
			header = this.blockUtil.readHeader();
			int rootNo = realRemovePrefix(header.rootNo,prefixString);
			header = this.blockUtil.readHeader();
			header.rootNo = rootNo;
			this.blockUtil.writeHeader(header);
			if(old_length==length()){
				return false; //not found
			}
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.writeLock().unlock();
		}
	}

	private int realRemovePrefix(int startNode, K prefixString) throws Exception {
		if(startNode == -1) return -1;
		DiskTreapNode<K,V> currentNode = this.blockUtil.readNode(startNode, false);
		int cp = prefixString.compareTo(currentNode.key);
		if(cp<=0){
			if(isPrefixString(prefixString.toString(), currentNode.key.toString())){
				if(currentNode.fix!=Integer.MAX_VALUE){
					//在data文件中加入删除记录信息
					currentNode.fix = Integer.MAX_VALUE;
					this.blockUtil.writeNode(startNode, currentNode, false);
					this.blockUtil.addDeleteInfo(currentNode.key) ;
					pushDeletedNode(startNode, currentNode);
					evictFromCache(currentNode.key);
				}
				if(currentNode.lNo==-1 && currentNode.rNo==-1 ){
					return -1;
				}else if(currentNode.rNo==-1){
					return realRemovePrefix(currentNode.lNo,prefixString);
				}else if(currentNode.lNo==-1){
					return realRemovePrefix(currentNode.rNo,prefixString);
				}else{
					DiskTreapNode<K, V> leftNode = this.blockUtil.readNode(currentNode.lNo, false);
					DiskTreapNode<K, V> rightNode = this.blockUtil.readNode(currentNode.rNo, false);
					int newstartNodeNo=-1;
					
					if(leftNode.fix<rightNode.fix){
						newstartNodeNo = rotateRight(startNode);
					}else{
						newstartNodeNo = rotateLeft(startNode);
					}
					DiskTreapNode<K, V> newstartNode = this.blockUtil.readNode(newstartNodeNo, false);
					
					newstartNode.rNo = realRemovePrefix(newstartNode.rNo, prefixString);
					if(newstartNode.rNo==-1){
						newstartNode.r_size = 0;
					}else{
						DiskTreapNode<K, V>  rN = this.blockUtil.readNode(newstartNode.rNo,false);
						newstartNode.r_size = 1+rN.l_size+rN.r_size;
					}
					newstartNode.lNo = realRemovePrefix(newstartNode.lNo,prefixString);
					if(newstartNode.lNo==-1){
						newstartNode.l_size = 0;
					}else{
						DiskTreapNode<K, V>  lN = this.blockUtil.readNode(newstartNode.lNo,false);
						newstartNode.l_size = 1+lN.l_size+lN.r_size;
					}
					this.blockUtil.writeNode(newstartNodeNo,newstartNode,false);
					if(isPrefixString(prefixString.toString(), newstartNode.key.toString())){
						return realRemovePrefix(newstartNodeNo, prefixString);
					}
					return newstartNodeNo;
				}
			}else{
				currentNode.lNo = realRemovePrefix(currentNode.lNo, prefixString);
				if(currentNode.lNo==-1){
					currentNode.l_size = 0;
				}else{
					DiskTreapNode<K, V>  lN = this.blockUtil.readNode(currentNode.lNo,false);
					currentNode.l_size = 1+lN.l_size+lN.r_size;
				}
				this.blockUtil.writeNode(startNode, currentNode, false);
			}
		}else{
			currentNode.rNo = realRemovePrefix(currentNode.rNo, prefixString);
			if(currentNode.rNo==-1){
				currentNode.r_size = 0;
			}else{
				DiskTreapNode<K, V>  rN = this.blockUtil.readNode(currentNode.rNo,false);
				currentNode.r_size = 1+rN.l_size+rN.r_size;
			}
			this.blockUtil.writeNode(startNode, currentNode, false);
		}
		return startNode;
	}

	@Override
	public Entry<K, V> kth(int k, boolean asc) {
		lock.readLock().lock();
		try {
			DiskTreapHeader header = this.blockUtil.readHeader();
			if(k>length())k=0;
			if(k<0)k=0;
			return findKth(header.rootNo, k,asc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	/**
	 * 第K个
	 * @param rootNo
	 * @param k
	 * @param asc 顺序数还是逆序数
	 * @return
	 * @throws Exception
	 */
	private Entry<K, V> findKth(int rootNo, int k, boolean asc) throws Exception {
		if(rootNo==-1){
			return null;
		}
		final DiskTreapNode<K, V> startNode = this.blockUtil.readNode(rootNo, false);
		if(asc){
			if(k<=startNode.l_size){
				return findKth(startNode.lNo,k,true);
			}else if(k==startNode.l_size+1){
				this.blockUtil.filleNodeValue(startNode);
				return  new Entry<K, V>() {

					@Override
					public K getKey() {
						return startNode.key;
					}
					@Override
					public V getValue() {
						return startNode.value;
					}
					@Override
					public V setValue(V value) {
						return null;
					}
					
					@Override
					public String toString() {
						return "{"+getKey()+"=>"+getValue()+"}";
					}
				};
			}else{
				return findKth(startNode.rNo,k-startNode.l_size-1,true);
			}
		}else{
			if(k<=startNode.r_size){
				return findKth(startNode.rNo,k,false);
			}else if(k==startNode.r_size+1){
				this.blockUtil.filleNodeValue(startNode);
				return new Entry<K,V>(){
					@Override
					public K getKey() {
						return startNode.key;
					}
					@Override
					public V getValue() {
						return startNode.value;
					}
					@Override
					public V setValue(V value) {
						return null;
					}
					@Override
					public String toString() {
						return "{"+getKey()+"=>"+getValue()+"}";
					}
				};
			}else{
				return findKth(startNode.lNo,k-startNode.r_size-1,false);
			}
		}
	}

	/**
	 * 排名
	 */
	@Override
	public int rank(K key, boolean asc) {
		lock.readLock().lock();
		try {
			DiskTreapHeader header = this.blockUtil.readHeader();
			return getRank(header.rootNo,key,asc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			lock.readLock().unlock();
		}
	}

	private int getRank(int rootNo, K key, boolean asc) throws Exception {
		if(rootNo==-1)return -1;
		DiskTreapNode<K, V> startNode = this.blockUtil.readNode(rootNo, false);
		int cp = key.compareTo(startNode.key);
		if(asc){
			if(cp==0){
				return startNode.l_size+1; 
			}else if(cp<0){
				return getRank(startNode.lNo,key,true);
			}else{
				int r_rank = getRank(startNode.rNo,key,true);
				if(r_rank==-1)return -1;
				return startNode.l_size+1+r_rank;
			}
		}else{
			if(cp==0){
				return startNode.r_size+1;
			}else if(cp>0){
				return getRank(startNode.rNo,key,false);
			}else{
				int l_rank = getRank(startNode.lNo,key,false);
				if(l_rank==-1)return -1;
				return startNode.r_size+1+l_rank;
			}
		}
	}

}
