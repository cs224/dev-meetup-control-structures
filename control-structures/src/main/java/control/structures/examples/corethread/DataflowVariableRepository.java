package control.structures.examples.corethread;

import java.io.Serializable;
import java.util.function.Supplier;

import control.structures.dataflow.DataflowVariable;

public interface DataflowVariableRepository {
	public <S extends Serializable> DataflowVariable<S> createReference(Supplier<S> constructor, String name);
}
