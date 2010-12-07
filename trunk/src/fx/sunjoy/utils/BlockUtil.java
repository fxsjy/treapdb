package fx.sunjoy.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fx.sunjoy.algo.impl.DiskTreapHeader;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.algo.impl.LRUMap;

public class BlockUtil<K extends Comparable<K>,V extends Serializable> {
	

	//单个数据文件的最大值60G
	private static final long DATAFILE_MAX_SIZE = 0xfffffffffL;

	//索引中头部的大小150B
	private static final int HEADER_SIZE = 150;
	
	//索引的MMAP的初始值32M
	private long INIT_INDEX_MMAP_SIZE = 64<<20;
	private final int MMAP_PAGE_SIZE = 16<<20;
	
	//数据体中每一项目的大小，由用户设置
	private int blockSize = 0;
	
	private FileChannel indexFile = null;
	
	private String indexFileName = null;
	
	//数据文件，一个索引可以对应多个数据文件，因为数据文件可能一个装不下
	private Map<String,FileChannel> dataFileMap = new HashMap<String, FileChannel>();
	
	private Map<String,List<FileChannel>> readDataFileMap = new HashMap<String, List<FileChannel>>();
	
	
	//只读索引的打开,多个，增强并发读性能
	private List<FileChannel> readIndexFileList = new ArrayList<FileChannel>();
	private final int READ_INDEX_COUNT = 5;
	
	private Map<Integer,DiskTreapNode<K, V>> nodeCache =  new LRUMap<Integer, DiskTreapNode<K,V>>(100000);
	private final List<MappedByteBuffer> writeMMBuf = new ArrayList<MappedByteBuffer>();
	private final List<ByteBuffer> readMMBuf = new ArrayList<ByteBuffer>();
	
	
	private DiskTreapHeader headerCache = null;
	
	public int iocounter = 0;
	
	public BlockUtil(int block_size, File _indexFile,long mmapSize ) throws Exception{
		blockSize = block_size;
		INIT_INDEX_MMAP_SIZE = mmapSize;
		
		boolean newFile = false;
		if(!_indexFile.exists()){
			_indexFile.createNewFile();
			newFile = true;
		}
		
		indexFile = new RandomAccessFile(_indexFile,"rw").getChannel();
		
		initMMBuf();
		if(newFile){
			DiskTreapHeader header = new DiskTreapHeader();
			header.block_size = block_size;
			this.writeHeader(header);
		}
		
		indexFileName = _indexFile.getAbsolutePath();
		for(int i=0;i<READ_INDEX_COUNT;i++){
			FileChannel f = new RandomAccessFile(_indexFile,"r").getChannel();
			readIndexFileList.add(f);
		}
		
		int real_size = this.readHeader().block_size;
		if(real_size!=block_size){
			throw new Exception("block size mismtach.(您设置的索引块大小和实际索引文件既有大小不一致):"+real_size+"!="+block_size);
		}
		
	}
	
	private void initMMBuf() throws IOException {
		long tailPageStart = 0 ;
		long reminderSize = INIT_INDEX_MMAP_SIZE;
		for(int i=0;i<INIT_INDEX_MMAP_SIZE/MMAP_PAGE_SIZE-1;i++){
			writeMMBuf.add(indexFile.map(FileChannel.MapMode.READ_WRITE	, i*(long)MMAP_PAGE_SIZE	, MMAP_PAGE_SIZE+blockSize));
			tailPageStart += MMAP_PAGE_SIZE;
			reminderSize -= MMAP_PAGE_SIZE;
			readMMBuf.add(writeMMBuf.get(i).asReadOnlyBuffer());
		}
		MappedByteBuffer tailMMBuf = indexFile.map(FileChannel.MapMode.READ_WRITE, tailPageStart,reminderSize );
		writeMMBuf.add(tailMMBuf);
		readMMBuf.add(tailMMBuf.asReadOnlyBuffer());
		int p_no = 0;
		for(final MappedByteBuffer b: writeMMBuf){
			b.load();
			System.out.println("page"+p_no+" loaded.");
			p_no++;
		}
	}

	private FileChannel getReadOnlyIndex(){
		int idex =(int) (Math.random()*readIndexFileList.size());
		//System.out.println("random indx:"+idex);
		return readIndexFileList.get(idex);
	}
	
	private FileChannel getDataFile(String dataFileName) throws Exception{
		if(!dataFileMap.containsKey(dataFileName)){
			//RandomAccessFile file = new RandomAccessFile(dataFileName, "rw");
			FileOutputStream appender = new FileOutputStream(dataFileName,true);
			appender.write(new byte[0]);// work round of jdk's bug
			FileChannel ch = appender.getChannel();
			//System.out.println("~~~~~"+ch.position());
			dataFileMap.put(dataFileName, ch);
		}
		return dataFileMap.get(dataFileName);
	}
	
	private FileChannel getReadDataFile(String dataFileName) throws Exception{
		synchronized(readDataFileMap){
			if(!readDataFileMap.containsKey(dataFileName)){
				List<FileChannel> dupList = new ArrayList<FileChannel>();
				for(int i=0;i<READ_INDEX_COUNT;i++){
					dupList.add(new RandomAccessFile(dataFileName, "r").getChannel());
				}
				readDataFileMap.put(dataFileName, dupList);
			}
		}
		int idx = (int)(Math.random()*READ_INDEX_COUNT);
		return readDataFileMap.get(dataFileName).get(idx);
	}
	
	public void finalize(){
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}finally{
			try {
				close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void close() throws IOException{
		if(indexFile!=null){
			if(dataFileMap!=null){
				for(Entry<String,FileChannel> e: dataFileMap.entrySet()){
					if(e.getValue()!=null){
						e.getValue().force(true);
						e.getValue().close();
					}
				}
			}
			if(readIndexFileList!=null){
				for(FileChannel f : readIndexFileList){
					f.close();
				}
			}
			for(MappedByteBuffer b: writeMMBuf){
				b.force();
			}
			indexFile.close();
			readIndexFileList = null;
			dataFileMap = null;
		}
	}
	
	private ByteBuffer xgetBytesN(FileChannel ch,long startPoint,int size) throws IOException{
		ByteBuffer buf = ByteBuffer.allocate(size);
		int ct =0;
		do{
			int n= ch.read(buf,startPoint+ct);
			if(n==-1)break;
			ct += n;
		}while(ct<size);
		buf.flip();
		return buf;
	}
	
	private ByteBuffer getBytesN(FileChannel ch,long startPoint,int size) throws IOException{
		if(startPoint+size>INIT_INDEX_MMAP_SIZE){
			return xgetBytesN(ch, startPoint, size);
		}
		ByteBuffer buf = ByteBuffer.allocate(size);
		ByteBuffer mm = readMMBuf.get((int)(startPoint/MMAP_PAGE_SIZE));
		synchronized (mm) {
			mm.position((int)(startPoint%MMAP_PAGE_SIZE));
			mm.get(buf.array());
		}
		return buf;
	}
	
	
	private void putBytes(FileChannel ch,long startPoint,ByteBuffer data) throws IOException{
		if(startPoint+data.limit()>INIT_INDEX_MMAP_SIZE){
			ch.write(data,startPoint);
			return;
		}
		int page_no = (int)(startPoint/MMAP_PAGE_SIZE);
		int record_no = (int)(startPoint % MMAP_PAGE_SIZE);
		writeMMBuf.get(page_no).position(record_no);
		writeMMBuf.get(page_no).put(data);
		data = null;
	}
	
	
	//读头部
	public  DiskTreapHeader readHeader() throws Exception {
		if(headerCache!=null)
			return headerCache;
		FileChannel rIndex = getReadOnlyIndex();
		iocounter++;
		ByteBuffer headerBytes = getBytesN(rIndex, 0, 150);
		DiskTreapHeader theHeader = (DiskTreapHeader) ByteUtil
				.loadHeader(headerBytes);
		headerCache = theHeader;
		headerBytes = null; 
		return theHeader;
	}
	
	//写头部
	public  void writeHeader(DiskTreapHeader header) throws Exception {
		headerCache = header;
		iocounter++;
		ByteBuffer headerBytes = ByteUtil.dumpHeader(header);
		putBytes(indexFile,0,headerBytes);
		headerBytes = null;
	}
	
	//读一个节点
	@SuppressWarnings("unchecked")
	public DiskTreapNode<K,V>  readNode(int pos,boolean loadValue) throws Exception {
		DiskTreapNode<K,V>  tmp;
		if(!loadValue &&  (tmp= nodeCache.get(pos))!=null){
			return tmp;
		}
		FileChannel rIndex = getReadOnlyIndex();
		iocounter++;
		ByteBuffer block = getBytesN(rIndex,HEADER_SIZE + (long) pos * blockSize, blockSize);
		DiskTreapNode<K, V> node = (DiskTreapNode<K, V>) ByteUtil.loads(block);
		block = null;
		if (loadValue) {
			File dataFileName = new File(indexFileName + ".data"
					+ node.valueFile);
			FileChannel dataFile = getReadDataFile(dataFileName
					.getAbsolutePath());
			iocounter++;
			ByteBuffer valueBytes = xgetBytesN(dataFile,node.valuePtr, node.valueLen);
			node.value = (V) ByteUtil.loadV(valueBytes.array());
			valueBytes = null;
		}
		if (node.value == null)
			nodeCache.put(pos, node);
		return node;
	}
	
	private void ensureFileExists(File dataFileName) throws IOException{
		if(!dataFileMap.containsKey(dataFileName.getAbsolutePath()) && !dataFileName.exists()){
			dataFileName.createNewFile();
		}
	}
	
	//写一个节点
	public void writeNode(int pos,DiskTreapNode<K,V> node,boolean changeValue) throws Exception{
		nodeCache.put(pos, node);
		if(changeValue){
			byte[] byteValue = ByteUtil.dumpV(node.value);
			node.value = null;

			File dataFileName = new File(indexFileName+".data"+node.valueFile);
			ensureFileExists(dataFileName);
			FileChannel dataFile = getDataFile(dataFileName.getAbsolutePath());
			while(dataFile.size()+byteValue.length>DATAFILE_MAX_SIZE){
				node.valueFile++;
				dataFileName = new File(indexFileName+".data"+node.valueFile);
				if(!dataFileName.exists()){
					dataFileName.createNewFile();
				}
				dataFile = getDataFile(dataFileName.getAbsolutePath());
			}
			node.valuePtr = (long)dataFile.position();
			//dataFile.position(dataFile.size());
			iocounter++;
			dataFile.write(ByteBuffer.wrap(byteValue));
			node.valueLen = byteValue.length;
			byteValue = null;
		}
		
		ByteBuffer block = ByteUtil.dumps(node,blockSize);
		if(block.limit()>blockSize){
			throw new Exception("key is too long(键太长了,可以TreapDB启动时调大索引块的size)");
		}
		
		iocounter++;
		
		putBytes(indexFile,HEADER_SIZE+(long)pos*blockSize,block);
		block = null;
	}
}
