package tester;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FsmWoman woman = new FsmWoman();
		woman.processMsg(FsmWoman.Msg.MEET_MAN);
		woman.processMsg(FsmWoman.Msg.HI);
		woman.processMsg(FsmWoman.Msg.TIME);	
	}
}
