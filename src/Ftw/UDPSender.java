package Ftw;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPSender {

	private static final int TIMEOUT = 100;
	static byte[] sendData = new byte[1400];
	byte[] receiveData = new byte[1400];
	public InetAddress IPAddress = InetAddress.getByName("");
	public static File file = new File("");
	public String dateiName;
	public int dateiNameGroesse;
	public int sequNummer = 0;
	public int ack;
	static int port = 6666;
	byte[] allDataPacketsBytes;

	public UDPSender(String datapath, String host) throws IOException {
		IPAddress = InetAddress.getByName(host);
		file = new File(datapath);

	}

	public static void main(String args[]) throws Exception {
		System.out.println("Enter ip");
		InetAddress ip = InetAddress.getByName("localhost");
		DatagramSocket senderSocket = new DatagramSocket();
		senderSocket.setSoTimeout(TIMEOUT);
		SenderAutomat senderAutomat = new SenderAutomat(senderSocket, ip);
		System.out.println("Enter file Path");
		File f = new File("C:\\Users\\Redd\\Downloads\\Komisch.png");
		String fileName = f.getName();
		byte[] fileNameBytes = BytesUmrechnen.StringToBytes(fileName);

		byte[] allData = new byte[(int) file.length()];
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(allData);
		fileInputStream.close();

		// calculating the number of packets that has to be sent (and the size
		// of the last packet)
		int allDataSize = allData.length;
		int fullDataPackets = allDataSize / SenderAutomat.DATA_SIZE;
		int lastPacketSize = allDataSize % SenderAutomat.DATA_SIZE;
		int allDataPackets = fullDataPackets;
		if (lastPacketSize != 0) {
			allDataPackets = fullDataPackets + 1;
		}

		// byte array with data for the number of packets that has to be sent
		byte[] allDataPacketsBytes = BytesUmrechnen.IntegerToBytes(allDataPackets);

		// current data packet (index)
		int i = 0;
		
		// index of next to send data from the allData array
		int srcPos = 0;
		int indexOfLastPacket = allDataPackets - 1;
		
		
		boolean isData = false;
		boolean isFilename = true;

		
		
		while(i < allDataPackets) {
			
			// data for the senderAutomat
			byte[] data = new byte[SenderAutomat.DATA_SIZE];
			
			
			// if bytes that will be sent are file data bytes
			if(isData) {
				// calculating index of source position from allData array as a multiple of DATA_SIZE in one udp segment
				srcPos = i * SenderAutomat.DATA_SIZE;
				
				// if last packet (preparing bytes)
				if(i == indexOfLastPacket) {
					senderAutomat.setDataSize(lastPacketSize);
					System.arraycopy(allData, srcPos, data, 0, lastPacketSize);
					i++;
					
				// else not last packet (preparing bytes)
				} else {
					senderAutomat.setDataSize(SenderAutomat.DATA_SIZE);
					System.arraycopy(allData, srcPos, data, 0, SenderAutomat.DATA_SIZE);
					i++;
				}
			// else one packet for the filename
			} else {
				if(isFilename) {
					senderAutomat.setDataSize(fileNameBytes.length);
					System.arraycopy(fileNameBytes, 0, data, 0, fileNameBytes.length);
					isFilename = false;
				// and one packet for the number of packets that will be sent
				} else {
					senderAutomat.setDataSize(allDataPacketsBytes.length);
					System.arraycopy(allDataPacketsBytes, 0, data, 0, allDataPacketsBytes.length);
					isData = true;
				}
			}
			
			// give the data to the Automat
			senderAutomat.setData(data);
		
		int loopcounter = allDataPackets;

		senderAutomat.processMsg(Msg.START);
		byte[] packet = new byte[SenderAutomat.RESPONSE_SIZE];
		DatagramPacket datagramResponcePacket = new DatagramPacket(packet, packet.length);

		
		while (loopcounter != 0) {
			senderAutomat.processMsg(Msg.START);
			senderAutomat.processMsg(Msg.SENDING);

			while(true){
			try {
				senderSocket.receive(datagramResponcePacket);
				loopcounter--;
				senderAutomat.setRecievedPacket(datagramResponcePacket);
				senderAutomat.getAck(datagramResponcePacket);
				senderAutomat.isSameAck(datagramResponcePacket);

				if (senderAutomat.isSameAck(datagramResponcePacket)) {

					if (senderAutomat.getAck(datagramResponcePacket) == (byte) 0) {
						senderAutomat.setNextAckCounter((byte) 1);
						senderAutomat.processMsg(Msg.GO_TO_SENDING);
						senderAutomat.processMsg(Msg.SENDING);
						break;
					}
					else if (senderAutomat.getAck(datagramResponcePacket) == (byte) 1) {
						senderAutomat.setNextAckCounter((byte) 0);
						senderAutomat.processMsg(Msg.GO_TO_SENDING);
						senderAutomat.processMsg(Msg.SENDING);
						break;
					}
				}
				else {
					senderAutomat.processMsg(Msg.FAILED_ACK);
				}

			}
			catch (SocketTimeoutException e) {
				senderAutomat.processMsg(Msg.TIMEOUT);
				
			}
			}
			senderAutomat.processMsg(Msg.BACK_TO_IDLE);
		}

	}


}
}