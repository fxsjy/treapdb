package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;

import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;


public class RankCommand extends AbstractCommand {
	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap,String command,byte[] body, BufferedOutputStream os) throws Exception{
		String[] stuff = command.split(" ");
		String key = stuff[1];
		boolean asc = Boolean.valueOf(stuff[2]);
		Integer rank = diskTreap.rank(new FastString(key),asc);
		if(rank!=-1){
			os.write(rank.toString().getBytes());
			os.write(("\r\n").getBytes());
		}
		os.write(("END\r\n").getBytes());
	}
}
