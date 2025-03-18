package demo.messages;

import java.util.List;

import akka.actor.ActorRef;

public class References {
	public List<ActorRef> processes;
	
	public References(List<ActorRef> members) {
		this.processes = members;
	}
	
	public String toString() {
		return References.class.getName();
	}
}
