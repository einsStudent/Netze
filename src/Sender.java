import java.net.DatagramPacket;

public class Sender {

	private static final int PORT = 8042;
	private static final int PACKET_SIZE = 1400;
	private static final int HEADER_CHECKSUM = 8;
	private static final int HEADER_LENGTH = 4;
	private static final int HEADER_SIZE = 13;
	private static final int DATA_SIZE = PACKET_SIZE - HEADER_SIZE;
	
	private State currentState;
	private Transition[][] transition;
	private byte[] data = new byte[DATA_SIZE];
	private int dataSize = 0;
	
	private byte alternatingBit;
	
	enum State {
		IDLE, SELECT, SEND, WAIT, RESEND, END
	}

	enum Msg {
		TIMEOUT, BACK_TO_IDLE, SUCCESSFUL_SEND, SUCCESSFUL_SELECT, SUCCESSFUL_FINISHED,
		SUCCESSFUL_RESENT, FAILED_ACK, SUCCESSFUL_CONNECT, SUCCESSFUL_ACK,

	}

	public Sender() {
		
		currentState = State.IDLE;

		transition = new Transition[State.values().length][Msg.values().length];
		
		//In IDLE u get select you start moveOnToSelect
		transition[State.IDLE.ordinal()][Msg.SUCCESSFUL_CONNECT.ordinal()] = new moveOnToSelect();
		
		transition[State.SELECT.ordinal()][Msg.SUCCESSFUL_SELECT.ordinal()] = new moveOnToSend();		
		
		transition[State.SEND.ordinal()][Msg.SUCCESSFUL_SEND.ordinal()] = new moveOnToWait();
		
		transition[State.WAIT.ordinal()][Msg.SUCCESSFUL_ACK.ordinal()] = new moveOnToSendNext();
		transition[State.WAIT.ordinal()][Msg.FAILED_ACK.ordinal()] = new moveOnToReSend();
		transition[State.WAIT.ordinal()][Msg.TIMEOUT.ordinal()] = new moveOnToReSend();
		transition[State.WAIT.ordinal()][Msg.SUCCESSFUL_FINISHED.ordinal()] = new moveOnToEnd();
		
		transition[State.END.ordinal()][Msg.BACK_TO_IDLE.ordinal()] = new moveOnToIdle();
	}
	
	
	private DatagramPacket makePacket(byte alternatingBit){
		
		byte[] alternatingBits = new byte[PACKET_SIZE - HEADER_CHECKSUM];
		alternatingBits[0] = alternatingByte;
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class moveOnToSelect extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Successfully Connected to a Socket!");
			System.out.println("Automatstatus: Switching from IDLE to SELECT");
			return State.SEND;
		}
	}
	
	
	class moveOnToSend extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("File selected!");
			System.out.println("Automatstatus: Switching from Select to Send");
			return State.SEND;
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
