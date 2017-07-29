package control.structures.helpers;

import co.paralleluniverse.fibers.SuspendExecution;

@FunctionalInterface
public interface InterruptableCallable<W> {
    W call() throws InterruptedException, SuspendExecution;
}
