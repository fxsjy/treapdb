package fx.sunjoy.client;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import fx.sunjoy.server.gen.TreapDBService.Client;

public class TreapDBClient extends Client {

	private TTransport transport;
	
	public TreapDBClient(TProtocol prot,int port, TTransport ftransport) throws TTransportException {
		super(prot);
		this.transport  = ftransport;
	}
	
	public void close(){
		this.transport.close();
	}

}
