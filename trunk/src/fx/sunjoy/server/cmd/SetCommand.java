package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.ConvertUtil;
import fx.sunjoy.utils.FastString;

public class SetCommand extends AbstractCommand {

	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,String command, byte[] body, BufferedOutputStream os) throws Exception {
		
		String[] stuff = command.split(" ");
		String key = stuff[1];
		String valueType = stuff[2];
		Integer valueLength = Integer.parseInt(stuff[4]);
		
		byte[] flags = ConvertUtil.int2byte(Integer.valueOf(valueType)) ;
		
		byte[] content = new byte[flags.length + valueLength] ;
		
		System.arraycopy(flags, 0, content, 0, flags.length) ;
		System.arraycopy(body, 0, content, flags.length, valueLength) ;
		
		diskTreap.put(new FastString(key), content);
		
		os.write("STORED\r\n".getBytes());
	}

}
