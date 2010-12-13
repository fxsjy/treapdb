package fx.sunjoy.server;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.server.gen.Pair;
import fx.sunjoy.server.gen.TreapDBService;
import fx.sunjoy.server.gen.TreapDBService.Iface;
import fx.sunjoy.utils.FastString;

public class TreapDBBinaryProtocolServer implements Iface{
	

	private String replicationRole = null ;
	
	
	private final DiskTreap<FastString, byte[]> treap ;

	
	private final int port ;
	
	private final TThreadPoolServer tr_server;
	
	public TreapDBBinaryProtocolServer(DiskTreap<FastString, byte[]> _diskTreap,int _port) throws TTransportException{
		this.treap = _diskTreap;
		this.port = _port;
		Factory protoFactory = new TBinaryProtocol.Factory(true, true);
		TreapDBService.Processor processor = new TreapDBService.Processor(this);
		tr_server = new TThreadPoolServer(processor,  new TServerSocket(port),protoFactory);
	}

	public void run(){
		tr_server.serve();
	}
	
	@Override
	public void put(String key, ByteBuffer value) throws TException {
		
		if(replicationRole != null && replicationRole.equalsIgnoreCase("Slave"))
		{
			return ;
		}
		byte[] content = addLeadingFlags(value);
		
		treap.put(new FastString(key), content) ;
		
		//treap.put(key, value.array());
	}

	//加上4个字节的标志位
	private byte[] addLeadingFlags(ByteBuffer value) {
		byte[] maskFlags = new byte[]{0,0,0,0} ;
		byte[] realvalue = value.array() ;
		byte[] content = new byte[maskFlags.length + realvalue.length] ;
		
		System.arraycopy(maskFlags, 0, content, 0, maskFlags.length) ;
		System.arraycopy(realvalue, 0, content, maskFlags.length, realvalue.length) ;
		return content;
	}

	@Override
	public ByteBuffer get(String key) throws TException {
		byte[] result = (byte[])treap.get(new FastString(key));
		if(result==null)return ByteBuffer.allocate(0);
		
		byte[] realvalue = new byte[result.length - 4] ;
		System.arraycopy(result, 4, realvalue, 0, realvalue.length);
		
		return ByteBuffer.wrap(realvalue);
		//return ByteBuffer.wrap(result);
	}
	
	private List<Pair> mapToList(Map<FastString, byte[]> r1) {
		List<Pair> r2 = new ArrayList<Pair>();
		for(Entry<FastString,byte[]> e: r1.entrySet()){
			byte[] realvalue = new byte[e.getValue().length - 4] ;
			System.arraycopy(e.getValue(), 4, realvalue, 0, realvalue.length) ;
			Pair pair = new Pair(new String(e.getKey().bytes), ByteBuffer.wrap(realvalue));
			r2.add( pair);
		}
		return r2;
	}

	@Override
	public List<Pair> prefix(String prefixStr, int limit,String startK, boolean asc)
			throws TException {
		FastString fastK = null;
		if(startK!=null)fastK = new FastString(startK);
		Map<FastString,byte[]> result = treap.prefix(new FastString(prefixStr), limit,fastK,asc);
		return mapToList(result);
	}


	@Override
	public List<Pair> kmax(int k) throws TException {
		Map<FastString,byte[]> result = treap.kmax(k);
		return mapToList(result);
	}

	@Override
	public List<Pair> kmin(int k) throws TException {
		Map<FastString,byte[]> result = treap.kmin(k);
		return mapToList(result);
	}

	@Override
	public List<Pair> range(String kStart, String kEnd, int limit)
			throws TException {
		Map<FastString,byte[]> result = treap.range(new FastString(kStart), new FastString(kEnd), limit);
		return mapToList(result);
	}

	@Override
	public boolean remove(String key) throws TException {

		// TODO Auto-generated method stub
		if(replicationRole != null && replicationRole.equalsIgnoreCase("Slave"))
		{
			return false;
		}
		
		return treap.delete(new FastString(key));

	}

	@Override
	public int length() throws TException {
		return treap.length();
	}


	public void setReplicationRole(String replicationRole) {
		this.replicationRole = replicationRole;
	}


	@Override
	public List<Pair> before(String key, int limit)
			throws TException {
		Map<FastString,byte[]> result = treap.before(new FastString(key), limit);
		return mapToList(result);
	}

	@Override
	public List<Pair> after(String key, int limit)
			throws TException {
		Map<FastString,byte[]> result = treap.after(new FastString(key), limit);
		return mapToList(result);
	}

	@Override
	public void bulkPut(Map<String, ByteBuffer> kvMap) throws TException {
		Map<FastString,byte[]> kvpairs = new TreeMap<FastString, byte[]>();
		for(Entry<String,ByteBuffer> e: kvMap.entrySet()){
			kvpairs.put(new FastString(e.getKey()), addLeadingFlags(e.getValue()));
		}
		treap.bulkPut(kvpairs);
	}

	@Override
	public List<Pair> bulkGet(List<String> keyList) throws TException {
		List<FastString> fstrList = new ArrayList<FastString>();
		for(String s: keyList){
			fstrList.add(new FastString(s));
		}
		Map<FastString,byte[] > result = treap.bulkGet(fstrList);
		return mapToList(result); 
	}

	@Override
	public List<Pair> bulkPrefix(List<String> prefixList, int limit,
			String startK, boolean asc) throws TException {
		FastString fastK = null;
		if(startK!=null) fastK = new FastString(startK);
		Map<FastString,byte[] > result = treap.bulkPrefix(prefixList, limit, fastK, asc);
		return mapToList(result);
	}

	
	
}
