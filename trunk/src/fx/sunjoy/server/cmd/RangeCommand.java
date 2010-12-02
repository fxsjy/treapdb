package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreap;

public class RangeCommand extends AbstractCommand{

	@Override
	public void execute(DiskTreap<String, Serializable> diskTreap,
			String command, byte[] body, BufferedOutputStream os)
			throws Exception {
		String[] stuff = command.split(" ");
		String start = stuff[1];
		String end = stuff[2];
		Integer limit = Integer.parseInt(stuff[3]);
		Map<String, Serializable> result = diskTreap.range(start, end,limit);
		for(Entry<String,Serializable> e :result.entrySet()){
			os.write((e.getKey()+",\t"+e.getValue()+"\r\n").getBytes());
		}
		os.write(("END\r\n").getBytes());
		
	}

}
