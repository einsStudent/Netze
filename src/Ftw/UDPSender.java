package Ftw;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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


	public UDPSender(String datapath, String host) throws IOException {
		IPAddress = InetAddress.getByName(host);
		file = new File(datapath);

	}

	@SuppressWarnings("resource")
	public void run() throws IOException, InterruptedException {

		dateiName = file.getName();
		dateiNameGroesse = dateiName.length();
		System.out.println(dateiNameGroesse);

		try {
			InputStream in = new FileInputStream(file);
			sendData = new byte[BytesUmrechnen.IntegerToBytes(sequNummer).length
					+ BytesUmrechnen.IntegerToBytes(dateiNameGroesse).length
					+ BytesUmrechnen.StringToBytes(dateiName).length
					+ BytesUmrechnen.IntegerToBytes(in.available()).length];
			System.out.println(in.available());
			for (int i = 0; i < 4; i++) {
				sendData[i] = BytesUmrechnen.IntegerToBytes(sequNummer)[i];
				sendData[i + 4] = BytesUmrechnen.IntegerToBytes(dateiNameGroesse)[i];
				sendData[i + 8] = BytesUmrechnen.IntegerToBytes(in.available())[i];
			}
			for (int i = 0; i < dateiName.length(); i++) {
				sendData[i + 12] = BytesUmrechnen.StringToBytes(dateiName)[i];
			}
			
			
			SendandReceiv.sendPacket(sendData, port);
			//
			byte[] filedata = new byte[in.available()];

			in.read(filedata);
			// in.close();

			int sendzahl = filedata.length / 1400;
			sendData = new byte[1400];
			int t = 0;
			int counter = 0;
			

			while (counter < sendzahl) {

				for (int i = 0; i < sendData.length; i++) {

					sendData[i] = filedata[i + t];
				}
				Thread.sleep(100);
				SendandReceiv.sendPacket(sendData, port);
				t = t + 1400;
				counter++;

				// clientSocket.send(sendPacket);

				System.out.println("counter sender " + counter + " " + sendData.length + " " + t);
			}
			//////////////////////////////////////////////////////////////////////////

			byte[] lastData = new byte[filedata.length - t];
			System.out.println(lastData.length);
			for (int i = 0; i < lastData.length; i++) {
				lastData[i] = filedata[i + t];
			}
			Thread.sleep(100);
			SendandReceiv.sendPacket(lastData, port);

			System.out.println("counter sender " + "" + (counter + 1) + "  " + lastData.length);

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public static byte[] getSendData() {
		return sendData;
	}

	public static int getPort() {
		return port;
	}

	public static void main(String args[]) throws Exception {

		UDPSender sender = new UDPSender("C:\\Users\\alex\\Documents\\xxx\\Uebung7\\Uebung7\\bouma.png", "localhost");
		System.out.println(file);
		sender.run();
		
		InetAddress ip = InetAddress.getByName(args[0]);
		DatagramSocket senderSocket = new DatagramSocket();
		senderSocket.setSoTimeout(TIMEOUT);
		SenderAutomat senderAutomat = new SenderAutomat(senderSocket, ip);
		senderAutomat.processMsg(Msg.SUCCESSFUL_SEND);
	}

}