package fx.sunjoy.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fx.sunjoy.algo.impl.DiskTreapHeader;
import fx.sunjoy.algo.impl.DiskTreapNode;


public class ByteUtil {
	private static final byte STRING = 0,INT=1,FLOAT=2,OBJ=3,ZIPSTRING=7,BYTEARRAY=8,ZIPBYTEARRAY=9;
	
	public static ByteBuffer dumpHeader(DiskTreapHeader header){
		ByteBuffer buf = ByteBuffer.allocate(150);
		buf.putInt(header.rootNo);
		buf.putInt(header.size);
		buf.putInt(header.block_size);
		buf.flip();
		return buf;
	}
	
	public static DiskTreapHeader loadHeader(ByteBuffer buf){
		DiskTreapHeader header = new DiskTreapHeader();
		header.rootNo = buf.getInt();
		header.size = buf.getInt();
		header.block_size= buf.getInt();
		buf = null;
		return header;
	}
	
	public static Object loadV(byte[] valueBytes) throws Exception{
		ByteBuffer buf = ByteBuffer.wrap(valueBytes);
		byte valueType = buf.get();
		if(valueType==ZIPSTRING){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			return new String(unzipBytes(dest));
		}else if(valueType==STRING){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			return new String(dest);
		}
		else if(valueType==INT){
			return buf.getInt();
		}else if(valueType==FLOAT){
			return  buf.getFloat();
		}else if(valueType==BYTEARRAY){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			return dest;
		}else if(valueType==ZIPBYTEARRAY){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			return unzipBytes(dest);
		}
		return xgetObjectFromBytes(Arrays.copyOfRange(valueBytes,1,valueBytes.length));
	}
	
	public static byte[] dumpV(Serializable value) throws Exception{
		ByteBuffer buf = null;
		if(value instanceof String){
			String svalue = (String)value;
			if(svalue.length()>(5<<10)){
				byte[] bytes = zipBytes(svalue.getBytes());
				buf = ByteBuffer.allocate(bytes.length+5);
				buf.put(ZIPSTRING);
				buf.putInt(bytes.length); //zip string's length
				buf.put(bytes);
			}else{
				byte[] bytes = svalue.getBytes();
				buf = ByteBuffer.allocate(bytes.length+5);
				buf.put(STRING);
				buf.putInt(bytes.length); //raw string's length
				buf.put(bytes);
			}
		}else if(value instanceof Integer){
			buf = ByteBuffer.allocate(5);
			buf.put(INT);
			buf.putInt((Integer)value);
		}else if(value instanceof Float){
			buf = ByteBuffer.allocate(9);
			buf.put(FLOAT);
			buf.putFloat((Float)value);
		}else if(value instanceof byte[]){
			byte[] bytes = (byte[])value;
			if(bytes.length < (5<<10)){
				buf = ByteBuffer.allocate(bytes.length+5);
				buf.put(BYTEARRAY);
				buf.putInt(bytes.length);
				buf.put(bytes);
			}else{
				bytes = zipBytes(bytes);
				buf = ByteBuffer.allocate(bytes.length+5);
				buf.put(ZIPBYTEARRAY);
				buf.putInt(bytes.length);
				buf.put(bytes);
			}
		}
		else{
			byte[] bytes = xgetBytesFromObject(value);
			buf = ByteBuffer.allocate(bytes.length+1);
			buf.put(OBJ);
			buf.put(bytes);
		}
		int size = buf.position();
		byte[] dest = new byte[size];
		buf.flip();
		buf.get(dest);
		return dest;
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static ByteBuffer dumps(DiskTreapNode node,int maxBlockSize){
		ByteBuffer buf = ByteBuffer.allocate(maxBlockSize);
		if(node.key instanceof FastString){
			byte[] bytes = ((FastString)node.key).bytes;
			buf.put((byte)13);
			buf.putInt(bytes.length); //string's length
			buf.put(bytes);
		}
		else if(node.key instanceof String){
			byte[] bytes = node.key.toString().getBytes();
			buf.put((byte)0);
			buf.putInt(bytes.length); //string's length
			buf.put(bytes);
		}else if(node.key instanceof Integer){
			buf.put((byte)1);
			buf.putInt((Integer)node.key);
		}else if(node.key instanceof Long){
			buf.put((byte)11);
			buf.putLong((Long)node.key);
		}else if(node.key instanceof Float){
			buf.put((byte)2);
			buf.putFloat((Float)node.key);
		}else{
			throw new RuntimeException("not supported key type");
		}
		buf.put(node.valueFile);
		buf.putLong(node.valuePtr);
		buf.putInt(node.valueLen);
		buf.putInt(node.fix);
		buf.putInt(node.r_size);
		buf.putInt(node.l_size);
		buf.putInt(node.rNo);
		buf.putInt(node.lNo);
		buf.flip();
		return buf;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DiskTreapNode loads(ByteBuffer buf){
		DiskTreapNode node = new DiskTreapNode();
		byte keyType = buf.get();
		node.keyType = keyType;
		if(keyType==13){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			node.key = new FastString(dest);
		}
		else if(keyType==0){
			int strLen = buf.getInt();
			byte[] dest = new byte[strLen];
			buf.get(dest);
			node.key = new String(dest);
		}else if(keyType==1){
			node.key = buf.getInt();
		}else if(keyType==11){
			node.key = buf.getLong();
		}else if(keyType==2){
			node.key = buf.getFloat();
		}
		node.valueFile = buf.get();
		node.valuePtr = buf.getLong();
		node.valueLen = buf.getInt();
		node.fix = buf.getInt();
		node.r_size = buf.getInt();
		node.l_size = buf.getInt();
		node.rNo = buf.getInt();
		node.lNo = buf.getInt();
		buf = null;
		return node;
	}
	
	public static byte[] xgetBytesFromObject(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        byte[] bytes= bo.toByteArray();
        bo.close();
        oo.close();
        return bytes;
    }
	
	public static boolean isSmallObj(Serializable obj){
		if(obj instanceof String){
			if(obj.toString().length()<500){
				return true;
			}else{
				return false;
			}
		}
		else if(obj instanceof byte[]){
			if(((byte[]) obj).length<500){
				return true;
			}else{
				return false;
			}
		}
		else if(!(obj instanceof Integer) && !(obj instanceof Float)){
			try {
				if(xgetBytesFromObject(obj).length>1000){
					return false;
				}
			} catch (Exception e) {
				System.err.println("waring:===");
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public static Object xgetObjectFromBytes(byte[] bytes) throws Exception{
		if(bytes ==null || bytes.length==0)
				return null;
		ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
		ObjectInputStream oi = new ObjectInputStream(bi);
		Object obj = oi.readObject();
		bi.close();
		oi.close();
		return obj;
	}
	
	public static byte[] unzipBytes(byte[] bytes) throws Exception{
		Inflater decompressor = new Inflater();
		decompressor.setInput(bytes);
		// Create an expandable byte array to hold the decompressed data
		ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);

		// Decompress the data
		byte[] buf = new byte[1024];
		while (!decompressor.finished()) {
		    try {
		        int count = decompressor.inflate(buf);
		        bos.write(buf, 0, count);
		    } catch (DataFormatException e) {
		    }
		}
		try {
		    bos.close();
		} catch (IOException e) {
		}
		// Get the decompressed data
		byte[] decompressedData = bos.toByteArray();
		return decompressedData;
	}
	
	public static byte[] zipBytes(byte[] origin) throws Exception {
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_SPEED);

		// Give the compressor the data to compress
		compressor.setInput(origin);
		compressor.finish();

		// Create an expandable byte array to hold the compressed data.
		// You cannot use an array that's the same size as the orginal because
		// there is no guarantee that the compressed data will be smaller than
		// the uncompressed data.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(origin.length);

		// Compress the data
		byte[] buf = new byte[1024];
		while (!compressor.finished()) {
		    int count = compressor.deflate(buf);
		    bos.write(buf, 0, count);
		}
		try {
		    bos.close();
		} catch (IOException e) {
		}

		// Get the compressed data
		byte[] compressedData = bos.toByteArray();
		return compressedData;
    }
	
	public static long getCRC32(byte[] bytes){
		CRC32 engine = new CRC32();
		engine.update(bytes);
		return engine.getValue();
	}
	
}
