package demo.actors;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.actors.types.State;
import demo.messages.Abort;
import demo.messages.Ack;
import demo.messages.Crash;
import demo.messages.Decide;
import demo.messages.Gather;
import demo.messages.Impose;
import demo.messages.Launch;
import demo.messages.Read;
import demo.messages.References;

public class Process extends UntypedAbstractActor {
	
	private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	private final int i;
	
	// initialized
	private List<ActorRef> processes = null;
	private int n = -1;
	
	// launched
	private Integer input = null;
	private int ballot = -1;
	private int proposal = -1;
	private State state = new State();
	private int acksCount = 0;
	
	// use to respond
	private int readBallot = -1;
	private int imposeBallot = -1;
	private int estimate = -1;
	
	// state
	private boolean faultProne = false;
	private double alpha = 0;
	private boolean silentMode = false;
	private Integer decided = null;
	
	public Process(int i) {
		if(i < 0) {
			throw new IllegalArgumentException("Expected non-negative process id, but received id="+ i + ".");
		}
		this.i = i;
	}

    public static Props props(int i) {
        return Props.create(Process.class, () -> {
        	return new Process(i);
        });
    }

    @Override
    public void onReceive(Object msg) {
        log.info("Process " + getSender().path().name() + " to " + getName() + ": [" + msg.toString() + "].");
        
        if(silentMode) return;
        if(faultProne && Math.random() > this.alpha) {
        	silentMode = true;
        	return;
        }
        
        if(msg instanceof References) {
        	setReferences((References) msg);
        } else if(msg instanceof Launch) {
        	launch();
        } else if(msg instanceof Crash) {
        	crash((Crash) msg);
        } else if(msg instanceof Read) {
        	read((Read) msg);
        } else if(msg instanceof Abort) {
        	abort((Abort) msg);
        } else if(msg instanceof Gather) {
        	gather((Gather) msg);
        } else if(msg instanceof Impose) {
        	impose((Impose) msg);
        } else if(msg instanceof Ack) {
        	ack((Ack) msg);
        } else if(msg instanceof Decide) {
        	decide((Decide) msg);
        }
    }

	private void setReferences(References msg) {
    	if(isInitialized()) {
    		throw new RuntimeException("References already initialized.");
    	}
		// validate references -->
		boolean foundSelf = false;
		for(ActorRef actor : msg.processes) {
			if(getSelf().equals(actor)) {
				foundSelf = true;
				break;
			}
		}
		if(!foundSelf) {
			throw new IllegalArgumentException("Own actor not in references.");
		}
		// <--
		log.info("Process " + getName() + " sets references.");
		
    	processes = msg.processes;
    	n = processes.size();
    }
    
    private void launch() {
    	if(!isInitialized()) {
    		throw new RuntimeException("References have not been initialized.");
    	}
    	if(isLaunched()) {
    		throw new RuntimeException("Process already launched.");
    	}
		log.info("Launch process " + getName() + ".");
    	input = Math.random() > 0.5 ? 1 : 0;
    	ballot = i - n;
    	propose();
    }
    
	private void propose() {
		assertLaunched();
		if(decided != null) {
			return;
		}
		log.info("Process " + getName() + " proposes " + input);
    	proposal = input;
    	ballot += n;
    	state = new State();
    	acksCount = 0;
    	sendToAll(new Read(ballot));
    }
    
    private void read(Read msg) {
		if(readBallot > msg.ballot || imposeBallot > msg.ballot) {
			sendToSender(new Abort(msg.ballot));
		} else {
			readBallot = msg.ballot;
			sendToSender(new Gather(msg.ballot, imposeBallot, estimate));
		}
	}
    
    private void abort(Abort msg) {
    	if(ballot == msg.ballot) { // implies launched
        	log.info("Process " + getName() + " aborted!");
        	// abortion is implicit by restarting the proposal
        	propose();
    	}
    }
    
    private void gather(Gather msg) {
    	if(decided != null) {
    		return;
    	}
    	if(ballot == msg.ballot) { // implies launched
    		int gatheredResponses = state.gather(msg.imposeBallot, msg.estimate);
	    	if(gatheredResponses > n / 2) {
	    		proposal = state.estimate != null ? state.estimate : proposal;
	    		state = new State();
	    		sendToAll(new Impose(ballot, proposal));
	    	}
    	}
    }
    
    private void impose(Impose msg) {
    	if(readBallot > msg.ballot || imposeBallot > msg.ballot) {
    		sendToSender(new Abort(msg.ballot));
		} else {
			estimate = msg.v;
			imposeBallot = msg.ballot;
			sendToSender(new Ack(msg.ballot));
		}
    }
    
    private void ack(Ack msg) {
    	if(decided != null) {
    		return;
    	}
    	if(ballot == msg.ballot) { // implies launched
        	acksCount++;
        	if(acksCount > n / 2) {
        		sendToAll(new Decide(proposal));
        	}
    	}
    }

	private void decide(Decide msg) {
		if(decided != null) {
			if(decided.intValue() == msg.v) {
				return;
			} else {
				throw new RuntimeException("Cannot decide on value " + msg.v + ". Already decieded on " + decided);
			}
		}
		decided = msg.v;
		log.info("Process " + getName() + " decided on " + decided + ".");
		sendToAll(new Decide(decided));
	}
	
	private void crash(Crash msg) {
		/*
		this.faultProne = true;
		this.alpha = msg.alpha;
		
		sendToSelf(pendingOperations.poll());
    	this.operating = false;
    	*/
	}
	
	
	
	// ----- HELPER FUNCTIONS -----
	
	private void sendToSender(Object msg) {
		getSender().tell(msg, getSelf());
	}
	
	private void sendToAll(Object msg) {
		for(ActorRef actor : processes) {
			actor.tell(msg, getSelf());
		}
	}
	
	private String getName() {
		return getSelf().path().name();
	}
	
	private boolean isInitialized() {
		return processes != null;
	}
	
	private boolean isLaunched() {
		return input != null;
	}
	
	private void assertLaunched() {
		if(!isLaunched()) {
			throw new RuntimeException("Process has not been launched yet.");
		}
	}
	
}
