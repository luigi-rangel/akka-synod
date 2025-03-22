package demo.messages;

public class Impose {
	
	public int ballot;
	public int v;
	
	public Impose(int ballot, int proposal) {
		this.ballot = ballot;
		this.v = proposal;
	}
	
	@Override
	public String toString() {
		return Impose.class.getSimpleName() + " (ballot=" + ballot + ", proposal=" + v + ")";
	}

}
