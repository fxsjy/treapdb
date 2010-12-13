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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import fx.sunjoy.algo.impl.DiskTreapHeader;
import fx.sunjoy.algo.impl.DiskTreapNode;
import fx.sunjoy.algo.impl.LRUMap;

public class BlockUtil<K extends Comparable<K>,V extends Serializable> {
	
	private AtomicInteger dataFileNO = new AtomicInteger(0);
	private AtomicLong currentFilePos = new AtomicLong(0); 
	
	private final int MAX_COUNT = 10000 ;
	
	private File dataFolder = null ;

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
			_indexFile.getAbsoluteFile().getParentFile().mkdirs();
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
		
		dataFolder = _indexFile.getAbsoluteFile().getParentFile() ;
		initDataFileNO(dataFolder) ;
		initCurrentFilePos() ;
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
			FileChannel ch = appender.getChannel();
			//System.out.println("~~~~~"+ch.position());
			ch.position(ch.size());// work round of jdk's bug
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
	
	@SuppressWarnings("unchecked")
	public void filleNodeValue(DiskTreapNode<K,V> node) throws Exception{
		if(node.valueFile!=-1){
			File dataFileName = new File(indexFileName + ".data"
					+ node.valueFile);
			FileChannel dataFile = getReadDataFile(dataFileName
					.getAbsolutePath());
			iocounter++;
			ByteBuffer valueBytes = xgetBytesN(dataFile,node.valuePtr, node.valueLen);
			node.value = (V)ByteUtil.loadV_MS(valueBytes.array()) ;//(V) ByteUtil.loadV(valueBytes.array());
			valueBytes = null;
		}else{
			node.value = (V)ConvertUtil.long2Bytes(node.valuePtr,node.valueLen);
		}
	}
	
	
	//读一个节点
	@SuppressWarnings("unchecked")
	public DiskTreapNode<K,V>  readNode(int pos,boolean loadValue) throws Exception {
		DiskTreapNode<K,V>  tmp;
		//System.out.println(">>"+nodeCache);
		if(!loadValue &&  (tmp= nodeCache.get(pos))!=null){
			return tmp;
		}
		FileChannel rIndex = getReadOnlyIndex();
		iocounter++;
		ByteBuffer block = getBytesN(rIndex,HEADER_SIZE + (long) pos * blockSize, blockSize);
		DiskTreapNode<K, V> node = (DiskTreapNode<K, V>) ByteUtil.loads(block);
		block = null;
		if (loadValue) {
			if(node.valueFile!=-1){
				File dataFileName = new File(indexFileName + ".data"
						+ node.valueFile);
				FileChannel dataFile = getReadDataFile(dataFileName
						.getAbsolutePath());
				iocounter++;
				ByteBuffer valueBytes = xgetBytesN(dataFile,node.valuePtr, node.valueLen);
				//node.value = (V) ByteUtil.loadV(valueBytes.array());
				node.value = (V)ByteUtil.loadV_MS(valueBytes.array()) ;
				valueBytes = null;
			}else{
				node.value = (V)ConvertUtil.long2Bytes(node.valuePtr,node.valueLen);
				//如果是小对象，不读数据文件了，直接从“借用”索引的地方保存
			}
		}
		if (node.value == null)
			nodeCache.put(pos, node);
		//System.out.println("<<"+nodeCache);
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
			//byte[] byteValue = ByteUtil.dumpV(node.value);
			byte[] byteValue = ByteUtil.dumpV_MS(node.key, node.value) ;
			
			if(node.valueFile==-1) node.valueFile=0;//忽略之前的影响
			
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
			
			//如果是小对象，不记录文件指针了，直接从“借用”索引的地方保存
			if(node.value instanceof byte[] && ((byte[])node.value).length<=8){
				node.valueFile = -1;
				node.valueLen = ((byte[])node.value).length;
				node.valuePtr = ConvertUtil.byte2Long((byte[])node.value);
			}
			node.value = null;
			if(node.valueFile>=0)
				dataFileNO.set(node.valueFile + 1) ;
			currentFilePos.set(dataFile.size()) ;
		}
		
		ByteBuffer block = ByteUtil.dumps(node,blockSize);
		iocounter++;
		
		putBytes(indexFile,HEADER_SIZE+(long)pos*blockSize,block);
		block = null;
	}
	
	@SuppressWarnings("unchecked")
	public List<V> getNewData(int slavaDataFileNum, long slaveSyncPos) throws Exception
	{	
		System.out.println("slave data fileNo: "+slavaDataFileNum + "> " + slaveSyncPos) ;
		
		//一次最多传输MAX_COUNT个value
		
		int masterDataFileNum = dataFileNO.get() - 1 ;
		long masterFilePos = currentFilePos.get() ;
		
		System.out.println("master(me) data fileNo: "+masterDataFileNum + ", offset: " + masterFilePos) ;
		
		List<V> result = new ArrayList<V>() ;
		
		if(masterDataFileNum > slavaDataFileNum)
		{
			int count = 0;
			long max_pos = 0 ;
			long cur_pos = 0 ;
			
			for(int i = slavaDataFileNum; i <= masterDataFileNum; i++)
			{
					FileChannel dataFile = new RandomAccessFile(indexFileName + ".data" + i, "r").getChannel() ;
					if(i == slavaDataFileNum)
					{
						max_pos = dataFile.size() ;
						cur_pos = slaveSyncPos ;
						dataFile.position(cur_pos) ;
					}
					else if( i == masterDataFileNum)
					{
						dataFile.position(0) ;
						max_pos = masterFilePos ;
						cur_pos = 0 ;
					}
					else
					{
						dataFile.position(0) ;
						max_pos = dataFile.size() ;
						cur_pos = 0 ;
					}
					
					while( count < MAX_COUNT && cur_pos < max_pos)
					{
						byte[] sub_result = getNextData(dataFile) ;
						cur_pos += sub_result.length ;
						result.add((V)sub_result) ;
						count++;
					}
					
					dataFile.close() ;
			}
		}
		else
		{
			if(masterFilePos > slaveSyncPos)
			{
				int count = 0 ;
				FileChannel dataFile = new RandomAccessFile(indexFileName + ".data" + masterDataFileNum, "r").getChannel() ;//fis.getChannel() ;
				dataFile.position(slaveSyncPos) ;
				while(count < MAX_COUNT && masterFilePos > slaveSyncPos)
				{
					byte[] sub_result = getNextData(dataFile) ;
					slaveSyncPos += sub_result.length ;
					result.add((V)sub_result) ;
					count++;
				}
				
				dataFile.close() ;
			}
		}
		
		return result ;
	}
	
	private byte[] getNextData(FileChannel dataFile) throws Exception
	{
		ByteBuffer key_length_buffer = ByteBuffer.allocate(1) ;
		dataFile.read(key_length_buffer) ;
		byte[] key_length = key_length_buffer.array() ;
		
		ByteBuffer key_buffer = ByteBuffer.allocate(key_length[0]) ;
		dataFile.read(key_buffer) ;
		byte[] key = key_buffer.array() ;
		
		//System.out.println(new String(key)) ;
		
		ByteBuffer type_buffer = ByteBuffer.allocate(1) ;
		dataFile.read(type_buffer) ;
		byte[] type = type_buffer.array() ;
		
		if(type[0] == -1)
		{
			byte[] sub_result = new byte[1 + key_length[0] + 1] ;
			
			System.arraycopy(key_length, 0, sub_result, 0, key_length.length) ;
			System.arraycopy(key, 0, sub_result, key_length.length, key.length) ;
			System.arraycopy(type, 0, sub_result, key_length.length + key.length, type.length) ;
			
			return sub_result ;
		}
		
		ByteBuffer value_length_buffer = ByteBuffer.allocate(4) ;
		dataFile.read(value_length_buffer) ;
		byte[] value_length = value_length_buffer.array() ;
		value_length_buffer.position(0) ;
		int value_length_int = value_length_buffer.getInt() ;
		
		//System.out.println(value_length_int) ;
		
		ByteBuffer value_buffer = ByteBuffer.allocate(value_length_int) ;
		dataFile.read(value_buffer) ;
		byte[] value = value_buffer.array() ;
		
		byte[] sub_result = new byte[1 + key_length[0] + 1 +4 + value_length_int] ;
		
		System.arraycopy(key_length, 0, sub_result, 0, key_length.length) ;
		System.arraycopy(key, 0, sub_result, key_length.length, key.length) ;
		System.arraycopy(type, 0, sub_result, key_length.length + key.length, type.length) ;
		System.arraycopy(value_length, 0, sub_result, key_length.length + key.length + type.length, value_length.length) ;
		System.arraycopy(value, 0, sub_result, key_length.length + key.length + type.length +value_length.length , value.length) ;
		
		return sub_result ;
	}
	
	private void initDataFileNO(File dataFolder) throws Exception
	{
		if(dataFolder.isDirectory())
		{
			File[] files = dataFolder.listFiles() ;
			for(int i = 0 ; i < files.length; i++)
			{
				if(files[i].getAbsolutePath().startsWith(indexFileName + ".data"))
				{
					dataFileNO.getAndIncrement() ;
				}
			}
			
			if(dataFileNO.get() == 0)
			{
				dataFileNO.set(1) ;
			}
		}
	}
	
	private void initCurrentFilePos() throws Exception
	{
		int datafile = dataFileNO.get() - 1;
		if(datafile >= 0)
		{
			String dataFileName = indexFileName + ".data" + datafile ;
			FileChannel dataFile = getDataFile(dataFileName) ;
			currentFilePos.getAndSet(dataFile.size()) ;
		}
		
	}
	
	public int getDataFileNO()
	{
		return dataFileNO.get() ;
	}
	
	public long getCurrentFilePos()
	{
		return currentFilePos.get() ;
	}
	
	public void addDeleteInfo(K key) throws Exception
	{
		int datafileNO = dataFileNO.get() - 1 ;
		File dataFileName = new File(indexFileName+".data"+datafileNO);
		ensureFileExists(dataFileName);
		FileChannel dataFile = getDataFile(dataFileName.getAbsolutePath());
		
		int delete_info_length = 0 ;
		
		byte[] key_content = key.toString().getBytes() ;
		
		//if(key instanceof FastString)
		//{
			delete_info_length += 1 + key_content.length + 1 ;
		//}
		
		byte[] deleteInfo = new byte[delete_info_length] ;
		
		deleteInfo[0] = (byte)key_content.length ;
		System.arraycopy(key_content, 0, deleteInfo, 1, key_content.length) ;
		deleteInfo[deleteInfo.length - 1] = -1 ;
		
		while(dataFile.size()+ delete_info_length > DATAFILE_MAX_SIZE)
		{
			datafileNO++;
			dataFileName = new File(indexFileName+".data"+datafileNO);
			if(!dataFileName.exists()){
				dataFileName.createNewFile();
			}
			dataFile = getDataFile(dataFileName.getAbsolutePath());
		}
		
		dataFile.write(ByteBuffer.wrap(deleteInfo));
		
		dataFileNO.set(datafileNO + 1) ;
		currentFilePos.set(dataFile.size()) ;
	}
}
