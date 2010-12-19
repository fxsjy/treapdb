package fx.sunjoy.algo.impl;

import java.io.Serializable;

@SuppressWarnings("serial")
public class DiskTreapHeader implements Serializable{
	public int rootNo=-1;
	public int size = 0;
	public int block_size = 0;
	public int deletedNode = -1;
}
