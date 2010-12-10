package fx.sunjoy.test.dirtytest;

import java.io.Serializable;

import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.utils.ByteUtil;
import fx.sunjoy.utils.FastString;

public class TestByteUtil {

	public static void main(String[] args) throws Exception {
		DiskTreapNode<FastString,Serializable> node = new DiskTreapNode<FastString,Serializable>();
		System.out.println(ByteUtil.xgetBytesFromObject(node).length);
		node.key = new FastString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		node.value= 123123;
		node.key = new FastString("thing10000000");
		node.fix = 456;
		System.out.println("raw:"+ByteUtil.dumps(node,1024).limit());
		/*DiskTreapNode node2 = ByteUtil.loads(ByteUtil.dumps(node,1024));
		System.out.println("node:"+node2);
		byte[] nodeBytes = ByteUtil.xgetBytesFromObject(node);
		byte[] block = Arrays.copyOf(nodeBytes, 440);
		System.out.println(ByteUtil.xgetObjectFromBytes(block));
		System.out.println(ByteUtil.xgetBytesFromObject(new DiskTreapHeader()).length);*/
		long t1 = System.currentTimeMillis();
		for(int i=0;i<10000000;i++){
			node.key = new FastString("thing"+i+"");
			ByteUtil.dumps(node,64);
		}
		System.out.println(System.currentTimeMillis()-t1);
	}

}
