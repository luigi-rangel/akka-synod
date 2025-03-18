package demo.actors.types;

public class State {
	private Integer gatheredStates;
	private Integer estBallot;
	public Integer estimate;
	
	public State() {
		this.estBallot = -1;
		this.estimate = null;
		this.gatheredStates = 0;
	}
	
	public Integer gather(Integer newEstBallot, Integer newEstimate) {
		gatheredStates++;
		if(newEstBallot.intValue() > this.estBallot.intValue()) {
			this.estBallot = newEstBallot;
			this.estimate = newEstimate;
		}
		return gatheredStates;
	}
}
