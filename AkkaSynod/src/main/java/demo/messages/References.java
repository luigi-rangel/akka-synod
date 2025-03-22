package demo.messages;

import java.util.List;

import akka.actor.ActorRef;

public class References {
	
	public List<ActorRef> processes;
	
	public References(List<ActorRef> members) {
		this.processes = members;
	}

	@Override
	public String toString() {
		return References.class.getSimpleName() + " (N=" + processes.size() + ")";
	}
	
}
