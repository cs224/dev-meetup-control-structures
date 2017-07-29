package control.structures.dataflow;

import java.io.Serializable;

import co.paralleluniverse.fibers.SuspendExecution;

public interface DataflowVariable<T extends Serializable> extends ResponseVariable<T> {
	
	public void bind(T value) throws AlreadyBoundToIncompatibleValueException;
	public T get() throws SuspendExecution, InterruptedException;
	
	public ResponseVariable<T> response();
	
	public boolean isDone();
	
	public static class AlreadyBoundToIncompatibleValueException extends RuntimeException {
		private static final long serialVersionUID = 6396210388354647161L;
		public AlreadyBoundToIncompatibleValueException() {
			super();
		}
		public AlreadyBoundToIncompatibleValueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
		public AlreadyBoundToIncompatibleValueException(String message, Throwable cause) {
			super(message, cause);
		}
		public AlreadyBoundToIncompatibleValueException(String message) { 
			super(message);
		}
		public AlreadyBoundToIncompatibleValueException(Throwable cause) {
			super(cause);
		}
	}

}
