package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreap;

public class PrefixCommand extends AbstractCommand{

	@Override
	public void execute(DiskTreap<String, Serializable> diskTreap,
			String command, byte[] body, BufferedOutputStream os)
			throws Exception {
		String[] stuff = command.split(" ");
		String prefix = stuff[1];
		Integer limit = Integer.parseInt(stuff[2]);
		Map<String, Serializable> result = diskTreap.prefix(prefix,limit);
		for(Entry<String,Serializable> e :result.entrySet()){
			os.write((e.getKey()+",\t"+e.getValue()+"\r\n").getBytes());
		}
		os.write(("END\r\n").getBytes());
	}

}
