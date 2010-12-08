package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class DelCommand extends AbstractCommand{

	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,
			String command, byte[] body, BufferedOutputStream os)
			throws Exception {
		String[] stuff = command.split(" ");
		String key = stuff[1];
		boolean delOK = diskTreap.delete(new FastString(key));
		if(delOK){
			os.write(("DELETED\r\n").getBytes());
		}else{
			os.write(("NOT_FOUND\r\n").getBytes());
		}
	}

}
