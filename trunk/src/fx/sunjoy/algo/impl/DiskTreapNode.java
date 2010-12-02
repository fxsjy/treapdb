package fx.sunjoy.algo.impl;

import java.io.Serializable;

@SuppressWarnings("serial")
/**
 * 序列化后不能超过440个字节
 */
public class DiskTreapNode<K extends Comparable<K>,V extends Serializable> implements Serializable{
	
	public byte keyType; //0 string ,1 int, 2 float
	public K key;//max string length 250
	
	transient public V value;
	public byte valueFile = 0;
	public long valuePtr = 0;
	public int valueLen = 0;
	public int fix=0;
	public int r_size=0;
	public int l_size=0;
	public int rNo=-1;
	public int lNo=-1;
	
	public String toString(){
		return key.toString()+"=>"+value;
	}
}
