package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.ConvertUtil;
import fx.sunjoy.utils.FastString;


public class BulkGetCommand extends AbstractCommand {
	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,String command,byte[] body, BufferedOutputStream os) throws Exception{
		String[] stuff = command.split(" ");
		String keys = stuff[1];
		String[] tmp = keys.split(",");
		List<FastString> keyList = new ArrayList<FastString>();
		for(String t: tmp){
			keyList.add(new FastString(t));
		}
		
		Map<FastString,byte[]> contentMap = diskTreap.bulkGet(keyList);
		
		for(Entry<FastString, byte[]> entry: contentMap.entrySet()){
			FastString key = entry.getKey();
			byte[] content = entry.getValue();
			if(content!=null){
				byte[] value = new byte[content.length - 4] ;
				byte[] flags = new byte[4] ;
				
				System.arraycopy(content, 0, flags, 0, flags.length) ;
				System.arraycopy(content, flags.length, value, 0, value.length) ;
				
				int realflags = ConvertUtil.byte2int(flags) ;
				
				os.write(("VALUE "+key+" " + realflags +" "+value.length+"\r\n").getBytes());
				os.write(value);
				os.write(("\r\n").getBytes());
			}
		}
		
		os.write(("END\r\n").getBytes());
	}
}
