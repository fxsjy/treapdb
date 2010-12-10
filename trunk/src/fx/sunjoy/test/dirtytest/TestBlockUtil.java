package fx.sunjoy.test.dirtytest;

import java.io.File;

import fx.sunjoy.algo.impl.DiskTreapHeader;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.utils.BlockUtil;

public class TestBlockUtil {
	private static final int BLOCK_SIZE = 160;

	public static void main(String[] args) throws Exception{
		BlockUtil<String,String> blockUtil = new BlockUtil<String,String>(BLOCK_SIZE, new File("c:/test/hello"),64<<20);
		for(int i=0;i<20000;i++){
			DiskTreapNode<String,String> node = new DiskTreapNode<String,String>();
			node.key = i+"";
			node.value = (i+1)+"";
			blockUtil.writeNode(i, node,true);
		}
		blockUtil.close();
		
		blockUtil = new BlockUtil<String,String>(BLOCK_SIZE, new File("c:/test/hello"),64<<20);
		for(int i=0;i<20000;i++){
			DiskTreapNode<String,String> node  = blockUtil.readNode(i, true);
			System.out.println(node.value);
		}
		
		blockUtil.close();
		
		DiskTreapHeader header = new DiskTreapHeader();
		header.rootNo = 0;
		header.size = 10;
		blockUtil = new BlockUtil<String,String>(BLOCK_SIZE, new File("c:/test/hello"),64<<20);
		blockUtil.writeHeader(header);
		System.out.println(blockUtil.readHeader().size);
		blockUtil.close();
		blockUtil = new BlockUtil<String,String>(BLOCK_SIZE, new File("c:/test/hello"),64<<20);
		System.out.println(blockUtil.readHeader().size);
	}
}
