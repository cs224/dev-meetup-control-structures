package control.structures.continuations;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.Strand.State;

public class Coroutine implements Serializable {
	
	private static final long serialVersionUID = 357144399321567101L;
	
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("my-scheduler", new MyExecutor());
	private final Fiber<Void> fiber;
	
    public Coroutine(SuspendableCallable<Void> proto) {
        this(proto, Fiber.DEFAULT_STACK_SIZE);
    }
    
    public Coroutine(SuspendableCallable<Void> proto, int stackSize) {
        fiber = new Fiber<>(myFiberScheduler, () -> { Fiber.park(); proto.run();});
        fiber.start();
    }
	
	
	public void yield() throws SuspendExecution {
		Fiber.park();
	}
	
	// as long as I am single threaded and as long as I am sticking to the protocol of alternating calls to yield() and run()
	// I don't need to throw the SuspendExecution exception here, because this channel should never suspend.
	public void run() {
		Fiber.unpark(fiber);
	}

    public State getState() {
        return fiber.getState();
    }
    
    public void join() throws ExecutionException, InterruptedException {
    	fiber.join();
    }
	
}
