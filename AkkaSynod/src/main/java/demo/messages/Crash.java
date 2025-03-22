package demo.messages;

public class Crash {
	
	public double alpha;
	
	public Crash(double alpha) {
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		return Crash.class.getSimpleName() + " (alpha=" + alpha + ")";
	}

}
