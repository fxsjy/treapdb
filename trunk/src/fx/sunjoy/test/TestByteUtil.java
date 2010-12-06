package fx.sunjoy.test;

import java.util.Arrays;

import fx.sunjoy.algo.impl.DiskTreapHeader;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.utils.ByteUtil;

public class TestByteUtil {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		DiskTreapNode node = new DiskTreapNode();
		System.out.println(ByteUtil.xgetBytesFromObject(node).length);
		node.key = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		node.value= 123123;
		System.out.println("origin:"+ByteUtil.xgetBytesFromObject(node).length);
		node.key = "thing10000000";
		node.fix = 456;
		System.out.println("raw:"+ByteUtil.dumps(node,1024).limit());
		DiskTreapNode node2 = ByteUtil.loads(ByteUtil.dumps(node,1024));
		System.out.println("node:"+node2);
		byte[] nodeBytes = ByteUtil.xgetBytesFromObject(node);
		byte[] block = Arrays.copyOf(nodeBytes, 440);
		System.out.println(ByteUtil.xgetObjectFromBytes(block));
		System.out.println(ByteUtil.xgetBytesFromObject(new DiskTreapHeader()).length);		
	}

}
