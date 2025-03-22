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
        
        int n = 2;
        List<ActorRef> actors = new ArrayList<>();
        for(int i = 0; i < n; ++i) {
        	actors.add(system.actorOf(Process.props(i), String.valueOf(i)));
        }
        
        References m = new References(actors);
        for (ActorRef actor : actors) {
            actor.tell(m, ActorRef.noSender());
        }
        
        waitFor(50);
        
        for (ActorRef actor : actors) {
        	actor.tell(new Launch(), ActorRef.noSender());
        }

        waitThenTerminate(system, 3000);
    }
    
    private static void waitFor(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    private static void waitThenTerminate(final ActorSystem system, int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}
}
