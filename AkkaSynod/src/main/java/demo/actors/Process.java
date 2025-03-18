package demo.actors;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.messages.Abort;
import demo.messages.Gather;
import demo.messages.Launch;
import demo.messages.Propose;
import demo.messages.Read;
import demo.messages.References;

public class Process extends UntypedAbstractActor {
	final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	
	private List<ActorRef> refs;
	private int i;
	private int n;
	private int input;
	
	private Integer proposal;
	private int ballot;
	private int[][] states;
	private int readBallot = 0;
	private int imposeBallot = 0;
	private int estimate;
	
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
        log.debug(this.getSender().toString() + " sent '" + msg.toString() + "'");
        
        if(msg instanceof References) {
        	this.setReferences((References) msg);
        } else if(msg instanceof Launch) {
        	this.launch();
        } else if(msg instanceof Propose) {
        	this.propose((Propose) msg);
        } else if(msg instanceof Read) {
        	this.read((Read) msg);
        } else if(msg instanceof Abort) {
        	this.abort((Abort) msg);
        } else if(msg instanceof Gather) {
        	this.gather((Gather) msg);
        }
    }

	public void setReferences(References msg) {
    	refs = msg.processes;
    	n = refs.size();
    }
    
    public void launch() {
    	input = Math.random() > 0.5 ? 1 : 0;
    	ballot = i - n;
    	
    	this.self().tell(new Propose(input), this.getSelf());
    }
    
    public void propose(Propose msg) {
    	proposal = msg.input;
    	ballot += n;
    	initializeStates();
    	
    	for(int i = 0; i < n; ++i) {
    		refs.get(i).tell(new Read(ballot), getSelf());
    	}
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
    	log.debug(this.toString() + " aborted!");
    	
    	this.self().tell(new Propose(input), this.getSelf());
    }
    
    private void gather(Gather msg) {
    	// TODO Auto-generated method stub
    	
    }

	private void initializeStates() {
		states = new int[n][2];
    	for(int i = 0; i < n; ++i) {
    		states[i][1] = 0;
    	}
	}
}
