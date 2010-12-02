package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import fx.sunjoy.algo.impl.DiskTreap;

public class SetCommand extends AbstractCommand {

	@Override
	public void execute(DiskTreap<String, Serializable> diskTreap,String command, byte[] body, BufferedOutputStream os) throws Exception {
		String[] stuff = command.split(" ");
		String key = stuff[1];
		String valueType = stuff[2];
		Integer valueLength = Integer.parseInt(stuff[4]);
		
		if(valueType.equals("0")){
			String value = new String(Arrays.copyOf(body, valueLength));
			diskTreap.put(key, value);
		}else if(valueType.equals("1")){
			diskTreap.put(key, Arrays.copyOf(body, valueLength));
		}else if(valueType.equals("2")){
			Integer value = Integer.parseInt(new String(Arrays.copyOf(body, valueLength)));
			diskTreap.put(key, value);
		}else{
			os.write("ERROR\r\n".getBytes());
		}
		os.write("STORED\r\n".getBytes());
	}

}
