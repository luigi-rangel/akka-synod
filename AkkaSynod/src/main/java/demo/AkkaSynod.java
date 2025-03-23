package demo;

import akka.actor.ActorRef;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import demo.actors.Process;
import demo.messages.Crash;
import demo.messages.Decide;
import demo.messages.Hold;
import demo.messages.Launch;
import demo.messages.References;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class AkkaSynod {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("AkkaSynod");
    	final LoggingAdapter log = Logging.getLogger(system, "MAIN");
    	
        int n = 10;			// N 		= 3, 10, 100
        int f = (n-1) / 2;	// F 		= 1, 4, 49
        
        double alpha = 1;	// alpha 	= 0, 0.1, 1
        int timeout = 2000;	// timeout	= 500, 1000, 1500, 2000
        
        log.info("Run with n=" + n + ", f=" + f + ", alpha=" + alpha + " and timeout=" + timeout + ".");
        
        List<ActorRef> actors = new ArrayList<>(n);
        for(int i = 0; i < n; ++i) {
        	actors.add(system.actorOf(Process.props(i), String.valueOf(i)));
        }
        
        References references = new References(actors);
        for (ActorRef actor : actors) {
            actor.tell(references, ActorRef.noSender());
        }

        List<ActorRef> shuffled_actors = new ArrayList<>(actors);
        Collections.shuffle(shuffled_actors);
        
        Stream<ActorRef> allActors = actors.stream();
        List<ActorRef> actorsToCrash = shuffled_actors.subList(0, f);
        List<ActorRef> actorsToHold = shuffled_actors.subList(0, n-1);

        waitFor(20);
        log.debug("Initialized all servers.");
        waitFor(20);
        
		try {
	        // start timing --------------->
	        long startTime = System.currentTimeMillis();
	        // start processes
	        List<Future<Object>> futures = allActors.map(actor -> Patterns.ask(actor, new Launch(), 10000)).toList();
	        // crash processes
	        for(ActorRef actor : actorsToCrash) {
	        	actor.tell(new Crash(alpha), ActorRef.noSender());
	        }
	        // elect leader after timeout milliseconds
	        for(ActorRef actor : actorsToHold) {
	        	system.scheduler().scheduleOnce(
	        			Duration.create(timeout, TimeUnit.MILLISECONDS),
	        			actor,
	        			new Hold(),
	        			system.dispatcher(),
	        			ActorRef.noSender()
	        	);
	        }
			// await first response
	        scala.collection.immutable.List<Future<Object>> scalaFutures = 
	                scala.jdk.CollectionConverters.CollectionHasAsScala(futures).asScala().toList(); 
	        Future<Object> firstCompleted = Future.firstCompletedOf(scalaFutures, system.dispatcher());
			Object result = Await.result(firstCompleted, Duration.create(5, TimeUnit.SECONDS));
			// stop timing <-----------------
	        long endTime = System.currentTimeMillis();
	        long duration = endTime - startTime;
			if(result instanceof Decide) {
				log.info("Decided on value " + ((Decide) result).v + " after " + duration + " milliseconds.");
			} else {
				throw new RuntimeException("Expected " + Decide.class.getName() + " but got " + result.getClass().getName());
			}
		} catch (TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}

        waitThenTerminate(system, 1000);
        
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
