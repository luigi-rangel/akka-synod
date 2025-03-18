package demo;

import akka.actor.ActorRef;

import akka.actor.ActorSystem;
import demo.actors.Process;
import demo.messages.Launch;
import demo.messages.References;

import java.util.ArrayList;
import java.util.List;

public class AkkaSynod {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("AkkaSynod");
        
        int n = 1;
        List<ActorRef> actors = new ArrayList<>();
        for(int i = 0; i < n; ++i) {
        	actors.add(system.actorOf(Process.props(i), String.valueOf(i)));
        }
        
        References m = new References(actors);
        for (ActorRef actor : actors) {
            actor.tell(m, ActorRef.noSender());
        }
        
        for (ActorRef actor : actors) {
        	actor.tell(new Launch(), ActorRef.noSender());
        }

        waitThenTerminate(system);
    }
    
    private static void waitThenTerminate(final ActorSystem system) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}
}
