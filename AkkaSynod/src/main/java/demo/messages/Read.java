package demo.messages;

public class Read {
	
	public int ballot;
	
	public Read(int ballot) {
		this.ballot = ballot;
	}
	
	@Override
	public String toString() {
		return Read.class.getSimpleName() + " (ballot=" + ballot + ")";
	}

}
