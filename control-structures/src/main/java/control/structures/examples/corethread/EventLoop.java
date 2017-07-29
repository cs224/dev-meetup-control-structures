package control.structures.examples.corethread;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import control.structures.continuations.MyExecutor;
import control.structures.dataflow.DataflowVariable;
import control.structures.dataflow.QuasarVarDataflowVariableRepository;
import control.structures.dataflow.ResponseVariable;
import control.structures.utils.InfrastructureUtils;

public class EventLoop implements Runnable, DataflowVariableRepository {
		
	private static final Logger logger = LoggerFactory.getLogger(EventLoop.class);
	
	protected final LinkedBlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("event-loop", new MyExecutor());
    private final String name;
    private final Thread thread;
    
    public EventLoop(String name) {
    	this.name = name;
    	String threadName = "core-thread-" + this.name;
    	logger.debug("creating thread: " + threadName);
    	this.thread = new Thread(this, threadName);
    	logger.debug("starting thread: " + threadName);
    	this.thread.start();
    	logger.debug("started thread: " + threadName);
    }
    
    public Thread getCoreThread() {
    	return thread;
    }

	@Override
	public void run(){
		logger.debug("starting to run");
		while(!Strand.currentStrand().isInterrupted()) {
			try {
				logger.debug("calling commands.take()");
				Command command = commands.poll(1000, TimeUnit.MILLISECONDS);
				logger.debug("commands.take(): " + command);
				if(null == command) continue;

				Fiber<Void> fiber = new Fiber<>(myFiberScheduler, () -> {command.run(EventLoop.this); return null;});
				logger.debug("starting fiber");
				fiber.start();					
				logger.debug("fiber exited in status: " + fiber.getState());
				
			} catch (InterruptedException e) {
				Strand.currentStrand().interrupt();
			}
		}
		logger.debug("exiting due to Strand.currentStrand().isInterrupted()");
	}
	
	public void process(Command c) {
		try {
			this.commands.put(c);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private class BindCommand<T extends Serializable> implements Command {
		
		private static final long serialVersionUID = -396879977978827889L;
		private final DV<T> dv;
		private final T v;
		
		public BindCommand(DV<T> dv, T v) {
			this.dv = dv;
			this.v = v;
		}

		@Override
		public void run(DataflowVariableRepository r) throws SuspendExecution, InterruptedException {
			dv.bind1(v);
		}
		
	}
	
	private class DV<T extends Serializable> implements DataflowVariable<T>, Serializable {

		private static final long serialVersionUID = -7384576069381176916L;
		private final DataflowVariable<T> val;
		private final String variableName;
		
		private DV(DataflowVariable<T> val, String variableName) {
			this.variableName = variableName;
			this.val = val;
		}

		@Override
		public void bind(T value) throws DataflowVariable.AlreadyBoundToIncompatibleValueException {
			logger.debug("DV<T>.bind() creating command and scheduling on core-thread: '" + variableName + "' : '" + InfrastructureUtils.truncate(value) + "'");
			BindCommand<T> bindCommand = new BindCommand<T>(this, value);
			commands.add(bindCommand);
		}
		
		
		public void bind1(T value) throws DataflowVariable.AlreadyBoundToIncompatibleValueException {
			logger.debug("DV<T>.bind(): '" + variableName + "' : '" + InfrastructureUtils.truncate(value) + "'");
			val.bind(value);
		}

		@Override
		public T get() throws SuspendExecution, InterruptedException {
			logger.debug("DV<T>.get(): '" + variableName + "' : before");
			T v = val.get();
			logger.debug("DV<T>.get(): '" + variableName + "' : after");
			return v;
		}
		
		@Override
		public String toString() {
			T value = null;
			if(val.isDone())
				try {
					value = val.get();
				} catch (InterruptedException e) {
					Strand.currentStrand().interrupt();
				} catch(SuspendExecution see) {
					// ignore
				}
			return "DV<T>: '" + variableName + "' val bound?: '" + val.isDone() + "' value: '" + InfrastructureUtils.truncate(value) + "'";
		}

		@Override
		public ResponseVariable<T> response() {
			logger.debug("DV<T>.response(): '" + variableName + "'");
			return this;
		}

		@Override
		public boolean isDone() {
			return val.isDone();
		}
	}
	
	
	public <S extends Serializable> DataflowVariable<S> createReference(Supplier<S> constructor, String name) {
		DataflowVariable<S> dv1 = QuasarVarDataflowVariableRepository.createReference(constructor, name);
		DataflowVariable<S> dv = new DV<>(dv1, name);
		return dv;
	}
	

}
