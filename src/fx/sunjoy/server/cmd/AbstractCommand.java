package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public abstract class AbstractCommand {

	/* (non-Javadoc)
	 * @see fx.sunjoy.server.cmd.ICommand#execute(java.lang.String, java.io.BufferedReader, java.io.PrintWriter)
	 */
	enum VALUE_TYPE{STRING,OBJECT,INTEGER};
	
	public abstract void execute(DiskTreap<FastString, byte[]> diskTreap,String command, byte[] body, BufferedOutputStream os) throws Exception;
	
	public static VALUE_TYPE getValueType(Object value){
		if(value instanceof String){
			return VALUE_TYPE.STRING;
		}
		else if(value instanceof Integer){
			return VALUE_TYPE.INTEGER;
		}

		else{
			return VALUE_TYPE.OBJECT;
		}
	}

}