package Ftw;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.Adler32;

public class SenderAutomat{


	private State currentState;
	private Transition[][] transition;
	private InetAddress ip;
	private DatagramSocket senderSocket;
	
	
	private static final int ALTERNATING_BIT_SIZE = 1;
	private static final int ACK_SIZE = 1;
	private static final int HEADER_LENGTH_SIZE = 4;
	private static final int HEADER_CHECKSUM = 8;
	private static final int PACKET_SIZE = 1400;
	private static final int HEADER_TOTAL_SIZE = ALTERNATING_BIT_SIZE + HEADER_LENGTH_SIZE + HEADER_CHECKSUM;
	public static final int DATA_SIZE = PACKET_SIZE - HEADER_TOTAL_SIZE;
	private static final int PORT = 6666;
	private byte[] data = new byte[DATA_SIZE];
	private int dataSize = 0;


									// Getter und Setter
	//-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-//	
	
	public int getDataSize() {
		return dataSize;
	}

	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public State getCurrentState() {
		return currentState;
	}

	public void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public DatagramSocket getSenderSocket() {
		return senderSocket;
	}

	public void setSenderSocket(DatagramSocket senderSocket) {
		this.senderSocket = senderSocket;
	}

	public SenderAutomat(DatagramSocket senderSocket, InetAddress ip) {
		this.ip = ip;
		this.senderSocket = senderSocket;
		
		currentState = State.IDLE;
		transition = new Transition[State.values().length][Msg.values().length];
		transition[State.IDLE.ordinal()][Msg.SUCCESSFUL_CONNECT.ordinal()] = new moveOnToSelect();
		transition[State.SELECT.ordinal()][Msg.SUCCESSFUL_SELECT.ordinal()] = new moveOnToSend();		
		transition[State.SEND.ordinal()][Msg.SUCCESSFUL_SEND.ordinal()] = new moveOnToWait();
		transition[State.WAIT.ordinal()][Msg.SUCCESSFUL_ACK.ordinal()] = new moveOnToSendNext();
		transition[State.WAIT.ordinal()][Msg.FAILED_ACK.ordinal()] = new moveOnToReSend();
		transition[State.WAIT.ordinal()][Msg.TIMEOUT.ordinal()] = new moveOnToReSend();
		transition[State.WAIT.ordinal()][Msg.SUCCESSFUL_FINISHED.ordinal()] = new moveOnToEnd();
		transition[State.END.ordinal()][Msg.BACK_TO_IDLE.ordinal()] = new moveOnToIdle();
	}
	
	public void processMsg(Msg input){
		System.out.println("INFO Received "+input+" in state "+currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if(trans != null){
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: " + currentState);
	}
	
	
	private DatagramPacket makePacket(byte alternatingByte) {
		
		byte[] alternatingBit = new byte[ALTERNATING_BIT_SIZE];
		alternatingBit[0] = alternatingByte;
		
		byte[] size = new byte[HEADER_LENGTH_SIZE];
		size = BytesUmrechnen.IntegerToBytes(getDataSize());
		
		byte[] headerAndData = new byte[PACKET_SIZE - HEADER_CHECKSUM];
		System.arraycopy(alternatingBit, 0, headerAndData, 0, alternatingBit.length);
		System.arraycopy(size, 0, headerAndData, ALTERNATING_BIT_SIZE, size.length);
		System.arraycopy(getData(), 0, headerAndData, ALTERNATING_BIT_SIZE + HEADER_LENGTH_SIZE, getData().length);
		
		Adler32 adler32 = new Adler32();
		adler32.update(headerAndData);
		
		byte[] checksum = new byte[HEADER_CHECKSUM];
		checksum = BytesUmrechnen.LongToBytes(adler32.getValue());
		
		byte[] packet = new byte[PACKET_SIZE];
		System.arraycopy(checksum, 0, packet, 0, checksum.length);
		System.arraycopy(headerAndData, 0, packet, HEADER_CHECKSUM, headerAndData.length);
		
		return new DatagramPacket(packet, packet.length ,getIp(), PORT);
	}
	
	
	
	
	
	//(\_/)
	//( •,•)
	//(")_(")		-		-		-		-		-		-		-		-		-		-		-
	
	abstract class Transition {
		abstract public State execute(Msg input);
	}
	
	
	//	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-//
	class moveOnToSelect extends Transition {
		@Override
		public State execute(Msg input){
			System.out.println("Successfully Connected to a Socket!");
			System.out.println("Automatstatus: Switching from IDLE to SELECT");
			return State.SELECT;
		}
	}
	//-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-//	

	class moveOnToSend extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Automatstatus: Send");
//			getSenderSocket().send();
			
			return State.WAIT;
		}
	}


	class moveOnToSendNext extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Recived ACK!");
			System.out.println("Automatstatus: Switching from Wait to Send");
			return State.SEND;
		}
	}

	class moveOnToWait extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Packet was send!");
			System.out.println("Automatstatus: Switching from Send to Wait");
			return State.WAIT;
		}
	}

	class moveOnToReSend extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Automatstatus: Switching from Wait to Resend");
			return State.RESEND;
		}
	}

	class moveOnToEnd extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Automatstatus: Switching from Wait to End");
			return State.END;
		}
	}

	class moveOnToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Sending Packet");
			return State.IDLE;
		}
	}
}
