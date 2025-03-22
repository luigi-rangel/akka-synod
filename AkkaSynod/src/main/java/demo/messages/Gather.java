package demo.messages;

public class Gather {
	
	public int ballot;
	public int imposeBallot;
	public int estimate;
	
	public Gather(int ballot, int imposeBallot, int estimate) {
		this.ballot = ballot;
		this.imposeBallot = imposeBallot;
		this.estimate = estimate;
	}
	
	@Override
	public String toString() {
		return Gather.class.getSimpleName() + " (ballot=" + ballot + ", imposeBallot=" + imposeBallot + ", estimate=" + estimate + ")";
	}

}
