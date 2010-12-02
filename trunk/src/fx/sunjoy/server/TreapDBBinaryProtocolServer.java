package fx.sunjoy.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.server.gen.TreapDBService;
import fx.sunjoy.server.gen.TreapDBService.Iface;

public class TreapDBBinaryProtocolServer implements Iface{
	
	private final DiskTreap<String, byte[]> treap ;
	
	private final int port ;
	
	private final TThreadPoolServer tr_server;
	
	public TreapDBBinaryProtocolServer(DiskTreap<String, byte[]> _diskTreap,int _port) throws TTransportException{
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
		treap.put(key, value.array());
	}

	@Override
	public ByteBuffer get(String key) throws TException {
		byte[] result = (byte[])treap.get(key);
		if(result==null)return ByteBuffer.allocate(0);
		return ByteBuffer.wrap(result);
	}
	
	private Map<String, ByteBuffer> byteArraytoBuffer(Map<String, byte[]> r1) {
		Map<String,ByteBuffer> r2 = new HashMap<String, ByteBuffer>();
		for(Entry<String,byte[]> e: r1.entrySet()){
			r2.put(e.getKey(), ByteBuffer.wrap(e.getValue()) );
		}
		return r2;
	}

	@Override
	public Map<String, ByteBuffer> prefix(String prefixStr, int limit)
			throws TException {
		Map<String,byte[]> result = treap.prefix(prefixStr, limit);
		return byteArraytoBuffer(result);
	}


	@Override
	public Map<String, ByteBuffer> kmax(int k) throws TException {
		Map<String,byte[]> result = treap.kmax(k);
		return byteArraytoBuffer(result);
	}

	@Override
	public Map<String, ByteBuffer> kmin(int k) throws TException {
		Map<String,byte[]> result = treap.kmin(k);
		return byteArraytoBuffer(result);
	}

	@Override
	public Map<String, ByteBuffer> range(String kStart, String kEnd, int limit)
			throws TException {
		Map<String,byte[]> result = treap.range(kStart, kEnd, limit);
		return byteArraytoBuffer(result);
	}
	
	
	
}
