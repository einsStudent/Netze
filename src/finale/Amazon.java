package finale;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


class Amazone {

	private static final int PACKET_SIZE = 1400;
	private static final int HEADER_CHECKSUM = 8;
	private static final int HEADER_AB = 1;
	private static final int ACK = 1;
	private static final int DATA_SIZE = PACKET_SIZE - (HEADER_CHECKSUM + HEADER_AB + ACK);

	private static byte[] allData;
	
	public static void splitIntoPacketse(File f) throws IOException {

		byte[] buffer = new byte[DATA_SIZE];
		try (FileInputStream fis = new FileInputStream(f); BufferedInputStream bis = new BufferedInputStream(fis)) {
			int bytesAmount = 0;
			while ((bytesAmount = bis.read(buffer)) > 0) {
				allData = new byte[(int) f.length()];
				FileInputStream fileInputStream = new FileInputStream(f);
				fileInputStream.read(allData);
				fileInputStream.close();
			}
		}
			
		int allDataSize = allData.length;
		int fullDataPackets = allDataSize / DATA_SIZE;
		int lastPacketSize = allDataSize % DATA_SIZE;
		int allDataPackets = fullDataPackets;
		if(lastPacketSize != 0) {
			allDataPackets = fullDataPackets + 1;
		}
		
		// byte array with data for the number of packets that has to be sent
		byte[] allDataPacketsBytes = ByteUtils.intToBytes(allDataPackets);
		
	}
}
