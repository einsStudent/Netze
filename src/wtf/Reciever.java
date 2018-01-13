package wtf;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class Reciever {
	static final int TIMEOUT = 200;

	
	
	
	
	
	
	
	
	public static void main(String args[]) throws Exception {
		
		InetAddress ip = InetAddress.getByName(args[0]);
		DatagramSocket recieverSocket = new DatagramSocket();
		recieverSocket.setSoTimeout(TIMEOUT);
		RecieverAutomat recieverAutomat = new RecieverAutomat(recieverSocket, ip);
		recieverAutomat.processMsg(Msg.GO_TO_WAIT);
		

	}
}

