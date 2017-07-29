package control.structures.examples.corethread;

import co.paralleluniverse.fibers.SuspendExecution;

public interface Command extends java.io.Serializable  {
    void run(DataflowVariableRepository repository) throws SuspendExecution, InterruptedException;
}
