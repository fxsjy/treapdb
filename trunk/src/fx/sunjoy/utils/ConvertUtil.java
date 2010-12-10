package fx.sunjoy.utils;

public class ConvertUtil {
	
	public static byte[] int2byte(int value)
	{
		byte[] result = new byte[4];
		
		result[0] = (byte) (value & 0xff);// 最低位 
		result[1] = (byte) ((value >> 8) & 0xff);// 次低位 
		result[2] = (byte) ((value >> 16) & 0xff);// 次高位 
		result[3] = (byte) (value >>> 24);// 最高位,无符号右移。 
		return result; 
	}
	
	public static int byte2int(byte[] value) { 

		int result = (value[0] & 0xff) | ((value[1] << 8) & 0xff00) // | 表示安位或 
		| ((value[2] << 24) >>> 8) | (value[3] << 24); 
		
		return result; 
	} 

}
