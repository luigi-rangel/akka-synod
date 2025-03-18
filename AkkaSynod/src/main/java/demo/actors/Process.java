package demo.actors;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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
import demo.messages.Operation;
import demo.messages.Propose;
import demo.messages.Read;
import demo.messages.References;

public class Process extends UntypedAbstractActor {
	final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	
	private boolean operating = false;
	private boolean faultProne = false;
	private double alpha;
	private boolean silentMode = false;
	private Queue<Object> operationsList = new LinkedBlockingQueue<>(); // needs better name ig
	
	private List<ActorRef> refs;
	private int i;
	private int n;
	private int input;
	
	private Integer proposal;
	private int ballot;
	private State state = new State();
	private int readBallot = 0;
	private int imposeBallot = 0;
	private int estimate;
	private int totalAcks;
	
	public Process(int i) {
		this.i = i;
	}

    public static Props props(int i) {
        return Props.create(Process.class, () -> {
        	return new Process(i);
        });
    }

    @Override
    public void onReceive(Object msg) throws Throwable {
        log.info(this.getSender().toString() + " sent '" + msg.toString() + "'");
        
        if(silentMode) return;
        if(faultProne && Math.random() > this.alpha) {
        	silentMode = true;
        	return;
        }
        
        if(msg instanceof References) {
        	this.setReferences((References) msg);
        } else if(msg instanceof Launch) {
        	this.launch();
        } else if(msg instanceof Operation) {
        	operationsList.add(msg);
        	if(operating) return;
        	this.operate(operationsList.poll());
        } else if(msg instanceof Read) {
        	this.read((Read) msg);
        } else if(msg instanceof Abort) {
        	this.abort((Abort) msg);
        } else if(msg instanceof Gather) {
        	this.gather((Gather) msg);
        } else if(msg instanceof Impose) {
        	this.impose((Impose) msg);
        } else if(msg instanceof Ack) {
        	this.ack((Ack) msg);
        } else if(msg instanceof Decide) {
        	this.decide((Decide) msg);
        }
    }

	private void setReferences(References msg) {
    	refs = msg.processes;
    	n = refs.size();
    }
    
    private void launch() {
    	input = Math.random() > 0.5 ? 1 : 0;
    	ballot = i - n;
    	
    	this.self().tell(new Propose(input), this.getSelf());
    }
    
    private void operate(Object msg) {
    	this.operating = true;
    	
    	if(msg instanceof Propose) {
    		this.propose((Propose) msg);
    	} else if (msg instanceof Crash){
    		this.crash((Crash) msg);
    	}
    }

	private void propose(Propose msg) {
    	proposal = msg.input;
    	ballot += n;
    	state = new State();
    	totalAcks = 0;
    	
    	for(int i = 0; i < n; ++i) {
    		refs.get(i).tell(new Read(ballot), getSelf());
    	}
    }
	
	private void crash(Crash msg) {
		this.faultProne = true;
		this.alpha = msg.alpha;
		
		getSelf().tell(operationsList.poll(), getSelf());
    	this.operating = false;
	}
    
    private void read(Read msg) {
		if(readBallot > msg.ballot || imposeBallot > msg.ballot) {
			this.getSender().tell(new Abort(msg.ballot), getSelf());
		} else {
			readBallot = msg.ballot;
			this.getSender().tell(new Gather(msg.ballot, imposeBallot, estimate), getSender());
		}
	}
    
    private void abort(Abort msg) {
    	log.info(this.toString() + " aborted!");
    	
    	this.operating = false;
    	this.self().tell(new Propose(input), this.getSelf());
    }
    
    private void gather(Gather msg) {
    	if(state.gather(msg.imposeBallot, msg.estimate) > n / 2) {
    		proposal = state.estimate != null ? state.estimate : proposal;
    		
    		for(int i = 0; i < n; ++i) {
    			this.refs.get(i).tell(new Impose(ballot, proposal), getSelf());
    		}
    	}
    }
    
    private void impose(Impose msg) {
    	if(readBallot > msg.ballot || imposeBallot > msg.ballot) {
			this.getSender().tell(new Abort(msg.ballot), getSelf());
		} else {
			this.estimate = msg.v;
			this.imposeBallot = msg.ballot;
			this.getSender().tell(new Ack(msg.ballot), getSender());
		}
    }
    
    private void ack(Ack msg) {
    	totalAcks++;
    	
    	if(totalAcks > n / 2) {
    		for(int i = 0; i < n; ++i) {
    			this.refs.get(i).tell(new Decide(proposal), getSelf());
    		}
    	}
    }

	private void decide(Decide msg) {
		// ??????????? implemented according to the reference. don't think it works...
		for(int i = 0; i < n; ++i) {
			this.refs.get(i).tell(new Decide(proposal), getSelf());
		}
		
		log.info(this.toString() + " decided on " + msg.v);
		
		this.operating = false;
		getSelf().tell(operationsList.poll(), getSelf());
	}
}
