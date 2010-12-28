package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.ConvertUtil;
import fx.sunjoy.utils.FastString;


public class KthCommand extends AbstractCommand {
	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,String command,byte[] body, BufferedOutputStream os) throws Exception{
		String[] stuff = command.split(" ");
		Integer kth = Integer.parseInt(stuff[1]);
		boolean asc = Boolean.valueOf(stuff[2]);
		Entry<FastString,byte[]> obj = diskTreap.kth(kth, asc);
		if(obj!=null){
			byte[] content = obj.getValue();
			FastString key = obj.getKey();
			byte[] value = new byte[content.length - 4] ;
			byte[] flags = new byte[4] ;
			
			System.arraycopy(content, 0, flags, 0, flags.length) ;
			System.arraycopy(content, flags.length, value, 0, value.length) ;
			
			int realflags = ConvertUtil.byte2int(flags) ;
			
			os.write(("VALUE "+key+" " + realflags +" "+value.length+"\r\n").getBytes());
			os.write(value);
			os.write(("\r\n").getBytes());
		}
		
		
		os.write(("END\r\n").getBytes());
	}
}
