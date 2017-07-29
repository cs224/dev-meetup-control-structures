package control.structures.dataflow;

import java.io.Serializable;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.dataflow.Val;
import control.structures.utils.InfrastructureUtils;


public class QuasarVarDataflowVariableRepository {

	private static final Logger logger = LoggerFactory.getLogger(QuasarVarDataflowVariableRepository.class);

	private final static class DV<T extends Serializable> implements DataflowVariable<T>, Serializable {

		private static final long serialVersionUID = -7384576069381176916L;
		private final Val<T> val = new Val<T>();
		private final String variableName;
		
		private DV(String variableName) {
			this.variableName = variableName;
		}

		@Override
		public void bind(T value) throws DataflowVariable.AlreadyBoundToIncompatibleValueException {
			logger.debug("DV<T>.bind(): '" + variableName + "' : '" + InfrastructureUtils.truncate(value) + "'");
			val.set(value);
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

	public static <S extends Serializable> DataflowVariable<S> createReference(Supplier<S> constructor, String name) {
		DV<S> dv = new DV<S>(name);
		logger.debug("createReference(): " + dv);
		return dv;
	}
	
}
