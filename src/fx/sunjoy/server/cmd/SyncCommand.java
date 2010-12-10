package fx.sunjoy.server.cmd;

import java.io.BufferedOutputStream;
import java.util.List;
import fx.sunjoy.algo.impl.DiskTreap;
import fx.sunjoy.utils.FastString;

public class SyncCommand extends AbstractCommand {

	@Override
	public void execute(DiskTreap<FastString, byte[]> diskTreap, String command,
			byte[] body, BufferedOutputStream os) throws Exception 
	{
		String[] stuff = command.split(" ");
		int dataFileNO = Integer.valueOf(stuff[1]) ;
		long syncPos = Long.valueOf(stuff[2]) ;
		
		List<byte[]> key_data = diskTreap.Sync(dataFileNO, syncPos) ;
		
		if(key_data != null)
		{
			for(int i = 0 ; i < key_data.size(); i++)
			{
				os.write(key_data.get(i)) ;
			}
		}
		os.write(new byte[]{0}) ;
	}

}
