package control.structures.dataflow;

import java.io.Serializable;

public interface ResponseVariable<T extends Serializable> extends Serializable {

	public void bind(T value);
	
}
