package Ftw;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Adler32;

public class SenderAutomat{


	private State currentState;
	private Transition[][] transition;
	private InetAddress ip;
	private DatagramSocket senderSocket;
	
	
	private static final int ALTERNATING_BIT_SIZE = 1;
	static final int ACK_SIZE = 1;
	static final int HEADER_LENGTH_SIZE = 4;
	static final int HEADER_CHECKSUM = 8;
	static final int PACKET_SIZE = 1400;
	static final int HEADER_TOTAL_SIZE = ALTERNATING_BIT_SIZE + HEADER_LENGTH_SIZE + HEADER_CHECKSUM;
	static final int DATA_SIZE = PACKET_SIZE - HEADER_TOTAL_SIZE;
	static final int RESPONSE_SIZE = HEADER_CHECKSUM + ALTERNATING_BIT_SIZE;
	static final int PORT = 6666;
	private byte[] data = new byte[DATA_SIZE];
	private int dataSize = 0;
	private DatagramPacket recievedPacket;
	private DatagramPacket lastPacket;
	private DatagramPacket nextPacket;
	private Byte NextAckCounter;
	
	
	public Byte getNextAckCounter() {
		return NextAckCounter;
	}

	public void setNextAckCounter(Byte Ack) {
		this.NextAckCounter = Ack;
	}
	
	
	public DatagramPacket getRecievedPacket() {
		return recievedPacket;
	}

	public void setRecievedPacket(DatagramPacket recievedPacket) {
		this.recievedPacket = recievedPacket;
	}

	public DatagramPacket getLastPacket() {
		return lastPacket;
	}

	public void setLastPacket(DatagramPacket lastPacket) {
		this.lastPacket = lastPacket;
	}

	public DatagramPacket getNextPacket() {
		return nextPacket;
	}

	public void setNextPacket(DatagramPacket nextPacket) {
		this.nextPacket = nextPacket;
	}

	public Byte getCurrentAckCounter() {
		return currentAckCounter;
	}

	public void setCurrentAckCounter(Byte currentAckCounter) {
		this.currentAckCounter = currentAckCounter;
	}

	private Byte currentAckCounter;
	
	boolean sameAck;


									// Getter und Setter
	//-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-	-//	
	
	public int getDataSize() {
		return dataSize;
	}
	public boolean getIsSameAck() {
		return sameAck;
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
		transition[State.IDLE.ordinal()][Msg.START.ordinal()] = new StartAutomat();
		transition[State.SEND.ordinal()][Msg.SENDING.ordinal()] = new sendPacket();
		transition[State.WAIT.ordinal()][Msg.SUCCESSFUL_ACK.ordinal()] = new moveOnToSend();
		transition[State.WAIT.ordinal()][Msg.GO_TO_SENDING.ordinal()] = new moveOnToSend();
		transition[State.WAIT.ordinal()][Msg.FAILED_ACK.ordinal()] = new moveOnToReSend();
		transition[State.WAIT.ordinal()][Msg.TIMEOUT.ordinal()] = new moveOnToReSend();
		transition[State.END.ordinal()][Msg.BACK_TO_IDLE.ordinal()] = new moveOnToIdle();
		transition[State.RESEND.ordinal()][Msg.SUCCESSFUL_RESEND.ordinal()] = new moveOnToWait();

	}
	
	public void processMsg(Msg input){
		System.out.println("INFO Received "+input+" in state "+currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if(trans != null){
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: " + currentState);
	}
	
	
		byte getAck(DatagramPacket datagramPacket) {
		
		byte[] packet = new byte[RESPONSE_SIZE];
		packet = datagramPacket.getData();
		
		// received ack/data
		byte[] ack = new byte[ACK_SIZE];
		System.arraycopy(packet, HEADER_CHECKSUM, ack, 0, ACK_SIZE);
		if(ack[0] == (byte) 0){
			setNextAckCounter((byte) 1);
		}else{
			setNextAckCounter((byte) 0);
		}
		
		return ack[0];
		
		}


		boolean isSameAck(DatagramPacket recievedPacket){
			if(getCurrentAckCounter() == getAck(recievedPacket)){
				
					return true;
				
				}
			return false;
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
	//(")_(")		-		-		-		-		-		-		-		-		-		-		-	-//
	
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

	
	//ZUSTAND WAIT
	class moveOnToSend extends Transition {
		@Override
		public State execute(Msg input) {
			
			System.out.println("Automatstatus: Send");
			return State.SEND;
		}
	}
	
	class moveOnToWait extends Transition {
		@Override
		public State execute(Msg input) {
			return State.WAIT;
		}
	}

	class moveOnToReSend extends Transition {
		@Override
		public State execute(Msg input) {
			try {
				getSenderSocket().send(lastPacket);
				currentAckCounter = getAck(lastPacket);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			
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
	class sendPacket extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Sending Packet");
			try {
				DatagramPacket packet = makePacket(currentAckCounter);
				getSenderSocket().send(packet);
				currentAckCounter = getAck(packet);

			}
			catch (IOException e) {
				e.printStackTrace();
			}
			processMsg(Msg.SUCCESSFUL_SEND);
			System.out.println("Automatstatus: Send");
			return State.WAIT;
		}
	}
	class StartAutomat extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Automat is Started. Moving to Send");
			return State.SEND;
		}
	}
	class waiting extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Waiting...");
			return State.WAIT;
		}
	}

}
