package finale;

import java.io.File;

public class SenderMain {

	public static void main(String[] args) {
	
		File file = new File(args[0]);
		String fileName = file.getName();
		byte[] fileNameBytes = ByteUtils.stringToBytes(fileName);

	}

}
