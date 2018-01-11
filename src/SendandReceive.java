import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendandReceive {
	public static byte[] receivePacket(int port, byte[] receiveData) throws IOException {

		DatagramSocket serverSocket = new DatagramSocket(port);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		serverSocket.receive(receivePacket);
		for (int i = 0; i < receiveData.length; i++) {
			receiveData[i] = receivePacket.getData()[i];
		}
		serverSocket.close();
		return receiveData;
	}

	public static void sendPacket(byte[] sendData, int port) throws IOException {
		DatagramSocket clientSocket = new DatagramSocket();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("localhost"),
				port);
		clientSocket.send(sendPacket);
		clientSocket.close();
	}
}
