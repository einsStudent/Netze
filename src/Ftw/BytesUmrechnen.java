package Ftw;
import java.nio.ByteBuffer;

public class BytesUmrechnen {
	static byte[] stringTobytes;
	static byte[] IntegerTobytes;
	static byte[] LongTobytes;
	static int BytesToInteger;
	static long BytesToLong;
	static String string;

	public static byte[] StringToBytes(String string) {
		return stringTobytes = string.getBytes();
	}
	public static String BytesToString(byte[] byt) {
		return string = new String(byt);
	}
	
	public static byte[] IntegerToBytes(int integer) {
		return ByteBuffer.allocate(4).putInt(integer).array();
	}

	public static int BytesToInteger(byte[] bytes) {
		ByteBuffer.allocate(4);
		return ByteBuffer.wrap(bytes).getInt();
	}

	public static byte[] LongToBytes(long longe) {
		return ByteBuffer.allocate(8).putLong(longe).array();
	}
	public static long BytesToLong(byte[] bytes) {
		ByteBuffer.allocate(8);
		return ByteBuffer.wrap(bytes).getLong();
	}
	
	public static void main(String args[]) throws Exception{
		BytesUmrechnen b =new BytesUmrechnen();
		int i =2222;
		IntegerTobytes= BytesUmrechnen.IntegerToBytes(i);
		 System.out.println(IntegerTobytes);
		 BytesToInteger=BytesUmrechnen.BytesToInteger(IntegerTobytes);
		 System.out.println( BytesToInteger);
		 String s="saify";
		 stringTobytes=BytesUmrechnen.StringToBytes(s);
		 System.out.println(stringTobytes);
		 
		 System.out.println(BytesUmrechnen.BytesToString(stringTobytes));
		int t = 13045/1400;
		 System.out.println(t);
		
	}
}
