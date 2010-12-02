package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreap;


public class GetCommand extends AbstractCommand {
	@Override
	public void execute(DiskTreap<String, Serializable> diskTreap,String command,byte[] body, BufferedOutputStream os) throws Exception{
		String[] stuff = command.split(" ");
		String key = stuff[1];
		Serializable value = diskTreap.get(key);
		if(value!=null){
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
		value = null;
		os.write(("END\r\n").getBytes());
	}
}
