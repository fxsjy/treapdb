package fx.sunjoy.client;

import java.io.IOException;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;

public class TreapDBClientFactory {
	
	public static TreapDBClient getClient(String ip,int port) throws TTransportException, IOException{
		TSocket transport = new TSocket(ip,port);
		
		TProtocol protocol = new TBinaryProtocol(transport);
		transport.open();
		TreapDBClient client = new TreapDBClient(protocol, port,transport) ;
		return client;
	}
}
