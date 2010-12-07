package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.ConvertUtil;


public class GetCommand extends AbstractCommand {
	@Override
	public void execute(DiskTreap<String, byte[]> diskTreap,String command,byte[] body, BufferedOutputStream os) throws Exception{
		String[] stuff = command.split(" ");
		String key = stuff[1];
		byte[] content = diskTreap.get(key);
		
		byte[] value = new byte[content.length - 4] ;
		byte[] flags = new byte[4] ;
		
		System.arraycopy(content, 0, flags, 0, flags.length) ;
		System.arraycopy(content, flags.length, value, 0, value.length) ;
		
		int realflags = ConvertUtil.byte2int(flags) ;
		
		os.write(("VALUE "+key+" " + realflags +" "+value.length+"\r\n").getBytes());
		os.write(value);
		os.write(("\r\n").getBytes());
		
		/*if(value!=null){
			VALUE_TYPE valueType = getValueType(value);
			
			if(valueType==VALUE_TYPE.STRING){
				String svalue = (String)value;
				//System.out.println(svalue);
				os.write(("VALUE "+key+" 0 "+svalue.getBytes().length+"\r\n").getBytes());
				os.write(svalue.getBytes());
				os.write(("\r\n").getBytes());
			}
			else if(valueType==VALUE_TYPE.INTEGER){
				os.write(("VALUE "+key+" 2 "+value.toString().length()+"\r\n").getBytes());
				os.write((value.toString()+"\r\n").getBytes());
			}else{
				byte[] bytes = (byte[])value;
				os.write(("VALUE "+key+" 1 "+bytes.length+"\r\n").getBytes());
				os.write(bytes);
				os.write(("\r\n").getBytes());
			}
		}
		value = null;*/
		
		
		
		os.write(("END\r\n").getBytes());
	}
}
