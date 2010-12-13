package fx.sunjoy.utils;

public class FastString implements Comparable<FastString> {

	public final byte[] bytes;
	
	private String backString ;
	public FastString(String originalStr){
		bytes = originalStr.getBytes();
		this.backString = new String(bytes);
		originalStr = null;
	}
	
	public FastString(byte[] bb){
		bytes = bb;
		this.backString = new String(bytes);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null)return false;
		if(obj==this)return true;
		if(obj instanceof FastString){
			return compareTo((FastString)obj)==0;
		}
		else if(obj instanceof String){
			return this.backString.equals(obj);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.backString.hashCode();
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
		return backString;
	}
}
