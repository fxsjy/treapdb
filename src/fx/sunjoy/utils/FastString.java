package fx.sunjoy.utils;

public class FastString implements Comparable<FastString> {

	public final byte[] bytes;
	
	public FastString(String originalStr){
		bytes = originalStr.getBytes();
		originalStr = null;
	}
	
	public FastString(byte[] bb){
		bytes = bb;
	}
	
	@Override
	public int compareTo(FastString o) {
		byte[] other = o.bytes;
		int len1 = bytes.length;
		int len2 = other.length;
		int n = Math.min(len1, len2);
		int k = 0;
		int lim = n;
		while (k < lim) {
			byte c1 = bytes[k];
			byte c2 = other[k];
			if (c1 != c2) {
				return c1 - c2;
			}
			k++;
		}
		return len1-len2;
	}

	@Override
	public String toString() {
		return new String(bytes);
	}
}
