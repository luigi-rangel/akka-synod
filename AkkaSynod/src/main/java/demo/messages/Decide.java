package demo.messages;

public class Decide {
	
	public int v;
	
	public Decide(int v) {
		this.v = v;
	}
	
	@Override
	public String toString() {
		return Decide.class.getSimpleName() + " (value=" + v + ")";
	}

}
