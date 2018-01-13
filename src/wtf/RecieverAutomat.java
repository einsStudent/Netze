package wtf;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class RecieverAutomat {

	private State currentState;
	private Transition[][] transition;
	

	public RecieverAutomat(DatagramSocket recieverSocket, InetAddress ip){
		currentState = State.STANDBY;
		transition = new Transition[State.values().length] [Msg.values().length];
		
		
		transition[State.STANDBY.ordinal()] [Msg.GO_TO_WAIT.ordinal()] = new Standby();
		
		transition[State.WAIT.ordinal()] [Msg.SUCESSFUL_RECV.ordinal()] = new CheckingPacketAndPerhapsAnswereIt();
		transition[State.ANSWERE.ordinal()] [Msg.SUCCESSFUL_ANSWERED.ordinal()] = new GoBackAndWait();
		transition[State.WAIT.ordinal()] [Msg.LAST_PACKET.ordinal()] = new AnswereingWithLastAck();
		
		transition[State.END.ordinal()] [Msg.END_BY_TIMEOUT.ordinal()] = new Finish();
		System.out.println("INFO FSM constructed, current state: "+currentState);
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
	
	class Standby extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Going from Standby to Wait");
			return State.WAIT;
		}
	}
	
	class CheckingPacketAndPerhapsAnswereIt extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Packet recvied.");
			// Look at the Packet and see if its oke if its okey set a boolean to true and send it!!
			boolean isDamaged = false;
			
			if(isDamaged){
				return State.WAIT;
			}
			else{
				return State.ANSWERE;
			}
		}
	}
	
	class GoBackAndWait extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Answere Send. Going back and waiting for more");
			return State.WAIT;
		}
	}
	
	class AnswereingWithLastAck extends Transition{
		@Override
		public State execute(Msg input) {
			System.out.println("I got the Last Packet. I am now Sending my last Ack and go to Standby");
			
			//Sending my last Ack
			//Going to wait for an answere if times up go to Standby
			long MarkerSystemtime = System.currentTimeMillis();
			
	        Timer timer = new Timer();
	        
	        timer.schedule(new TimerTask() {
	        	
	            @Override
	            public void run() {
	                
	            	//Recieve data from socket
	            	
	            	//if(Recieve){timer.cacel; return State.Wait}
	            }
	        }, 2*1000);
	        return State.END;
	    }

		
		
	}
	
	class Finish extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("Fully Recieved Data and made it into a File!!");
			return State.STANDBY;
		}
	}
	
}