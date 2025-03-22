package demo.messages;

public class Ack {
	
	public int ballot;
	
	public Ack(int ballot) {
		this.ballot = ballot;
	}
	
	@Override
	public String toString() {
		return Ack.class.getSimpleName() + " (ballot=" + ballot + ")";
	}

}
