package fx.sunjoy.algo.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import fx.sunjoy.algo.ITreap;


/*
 * Treap中的节点
 */
class TreapNode<K extends Comparable<K>,V>
{
	K key;
	V value;
	int fix=0;
	int r_size=0;
	int l_size=0;
	int rNo=-1;
	int lNo=-1;
}

/**
 * Treap 数据结构
 * @author Administrator
 *
 * @param <K> 键：需要可以比较大小
 * @param <V> 值
 */
public class MemTreap<K extends Comparable<K>,V> implements ITreap<K, V>  {
	
	int size;
	int rootNo;
	TreapNode<K,V>[] nodeAry;
	
	//默认的容量
	private static final int DEFAULT_CAPABILITY = 1000;
	
	@SuppressWarnings("unchecked")
	public MemTreap(int  capability){
		this.nodeAry = new TreapNode[capability];
		this.size = 0 ;
		this.rootNo = -1;
	}
	
	public MemTreap(){this(DEFAULT_CAPABILITY);};
	
	//写入
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#put(K, V)
	 */
	@Override
	public void put(K key,V value){
		this.rootNo = insert(this.rootNo,key,value);
	}
	
	//读出
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#get(K)
	 */
	@Override
	public V get(K key){
		int idx =  find(this.rootNo,key);
		if(idx==-1)return null;
		else return this.nodeAry[idx].value;
	}
	
	//范围查询
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#range(K, K)
	 */
	@Override
	public Map<K,V> range(K start,K end,int limit)
	{
		Map<K,V> result = new TreeMap<K, V>();
		collectRange(this.rootNo, start, end, result);
		return result;
	}
	
	@Override
	public Map<K,V> prefix(K prefixString,int limit){
		Map<K,V> results = new TreeMap<K,V>();
		prefixSearch(this.rootNo,prefixString,results);
		return results;
	}
	
	

	//长度
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#length()
	 */
	@Override
	public int length(){
		return this.nodeAry[this.rootNo].l_size+1+this.nodeAry[this.rootNo].r_size;
	}
	
	//删除
	/* (non-Javadoc)
	 * @see fx.sunjoy.algo.impl.ITreap#delete(K)
	 */
	@Override
	public boolean delete(K key){
		remove(this.rootNo,key);
		return true;
	}
	
	
	private void prefixSearch(int startNode, K prefixString,Map<K,V> results) {
		if(startNode==-1)return ;
		TreapNode<K, V> cur = this.nodeAry[startNode];
		if(prefixString.compareTo(cur.key)<=0){
			prefixSearch(cur.lNo, prefixString,results);
			if(isPrefixString(prefixString.toString(),cur.key.toString())){
				results.put(cur.key, cur.value);
			}
		}else{
			prefixSearch(cur.rNo, prefixString,results);
		}
	}
	
	private boolean isPrefixString(String prefixString, String key) {
		if(key.indexOf(prefixString)==0)return true;
		return false;
	}

	private void collectRange(int startNode, K start, K end,Map<K,V> values){
		if(startNode==-1){
			return;
		}
		TreapNode<K, V> node = this.nodeAry[startNode];
		int cp1 = node.key.compareTo(start);
		int cp2 = node.key.compareTo(end);
		collectRange(node.lNo, start, end, values);
		if(cp1>=0 && cp2<0){
			values.put(node.key, node.value);
		}
		collectRange(node.rNo, start, end, values);
	}
	
	//右旋
	private int rotateRight(int startNode){
		TreapNode<K,V> cur = this.nodeAry[startNode];
		int leftNo = cur.lNo;
		TreapNode<K,V> left = this.nodeAry[leftNo];
		int left_right = left.rNo;
		int left_right_size  = left.r_size;
		left.rNo = startNode;
		left.r_size += cur.r_size+1;
		cur.lNo  = left_right;
		cur.l_size = left_right_size;
		return leftNo;
	}
	
	//左旋
	private int rotateLeft(int startNode){
		TreapNode<K,V> cur = this.nodeAry[startNode];
		int rightNo = cur.rNo;
		TreapNode <K,V> right = this.nodeAry[rightNo];
		int right_left = right.lNo;
		int right_left_size = right.l_size;
		right.lNo = startNode;
		right.l_size += cur.l_size+1;
		cur.rNo = right_left;
		cur.r_size = right_left_size;
		return rightNo;
	}
	
	
	private int insert(int startNode,K key,V value){
		if(startNode==-1){
			TreapNode<K,V> newNode = new TreapNode<K,V>();
			newNode.key = key;
			newNode.value = value;
			newNode.fix = (int)(Math.random()*Integer.MAX_VALUE);
			if(size+1>this.nodeAry.length){
				resize();
			}
			this.nodeAry[this.size++] = newNode;
			return this.size-1;
			
		}else{
			TreapNode<K,V> currentNode = this.nodeAry[startNode];
			int cp = currentNode.key.compareTo(key);
			if(cp==0){
				this.nodeAry[startNode].value = value;
			}else if(cp<0){
				currentNode.r_size++;
				currentNode.rNo = insert(currentNode.rNo,key,value);
				if(this.nodeAry[currentNode.rNo].fix < currentNode.fix){
					startNode = rotateLeft(startNode);
				}
			}else if(cp>0){
				currentNode.l_size++;
				currentNode.lNo = insert(currentNode.lNo,key,value);
				if(this.nodeAry[currentNode.lNo].fix < currentNode.fix){
					startNode = rotateRight(startNode);
				}
			}
			
		}
		return startNode;
	}
	
	//容量扩充为原来的两倍
	private void resize() {
		this.nodeAry = Arrays.copyOf(this.nodeAry, this.nodeAry.length*2);
	}

	private int find(int startNode,K key){
		if(startNode==-1){
			return -1;
		}else{
			TreapNode<K,V> currentNode = this.nodeAry[startNode];
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
	
	private void remove(int startNode,K key){
		
	}

	@Override
	public Map<K, V> kmin(int k) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<K, V> kmax(int k) {
		// TODO Auto-generated method stub
		return null;
	}
}
