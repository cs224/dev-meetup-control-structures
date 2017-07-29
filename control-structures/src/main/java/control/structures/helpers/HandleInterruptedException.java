package control.structures.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;

public class HandleInterruptedException<R> implements InterruptableCallable<R> {
	
	private static final Logger logger = LoggerFactory.getLogger(HandleInterruptedException.class);
	
	private final InterruptableCallable<R> wrapped;
	
	public HandleInterruptedException(InterruptableCallable<R> wrapped) {
		this.wrapped = wrapped; 
	}

	@Override
	public R call() throws SuspendExecution {
		R result = null;
		try {
			result = wrapped.call();
			return result;
		} catch (InterruptedException e) {
			logger.info("Caught an InterruptedException.", e);
			Thread.currentThread().interrupt();
		}
		return result;
	}
}
