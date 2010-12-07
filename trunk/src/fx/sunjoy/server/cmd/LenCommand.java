package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreap;

public class LenCommand extends AbstractCommand {

	@Override
	public void execute(DiskTreap<String, byte[]> diskTreap,
			String command, byte[] body, BufferedOutputStream os)
			throws Exception {
		Integer len = diskTreap.length();
		os.write((""+len+"\r\n").getBytes());
		os.write(("END\r\n").getBytes());
	}

}
