package demo.actors.types;

public class Decision {
	public DecisionType type;
	public int value;
	
	public Decision(DecisionType type, int value) {
		this.type = type;
		this.value = value;
	}
}