package demo.messages;

public class Propose implements Operation {
	public int input;
	
	public Propose(int input) {
		this.input = input;
	}
	
	public String toString() {
		return "Propose '" + this.input + "'";
	}
}
