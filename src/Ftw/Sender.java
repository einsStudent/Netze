package Ftw;

import java.io.IOException;

public class Sender{


	private State currentState;
	private Transition[][] transition;

	public Sender() {
		
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
		System.out.println("INFO State: "+currentState);
	}
	


	abstract class Transition {
		abstract public State execute(Msg input);
	}
	
	
	//									THIS IS THE IDELE MODE
	//
	//				The idle mode builds up a DatagrammSocket and Connection
	//
	//
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
			try {
				SendandReceiv.sendPacket(UDPSender.getSendData(), UDPSender.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
