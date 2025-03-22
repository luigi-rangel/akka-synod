package demo.messages;

public class Abort {
	
	public int ballot;
	
	public Abort(int ballot) {
		this.ballot = ballot;
	}
	
	@Override
	public String toString() {
		return Abort.class.getSimpleName() + " (ballot=" + ballot + ")";
	}

}
