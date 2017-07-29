package control.structures.continuations;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand.State;
import co.paralleluniverse.strands.SuspendableCallable;

public abstract class CoIterator<E> implements Iterator<E>, Serializable {

    private static final long serialVersionUID = 351278561539L;
    
    private final Coroutine co;
    
    private E element;
    private boolean hasElement;
    
    protected CoIterator() {
        co = new Coroutine((SuspendableCallable<Void>)() -> {run(); return null;});
    }

    public boolean hasNext() {
        while(!hasElement && co.getState() != State.TERMINATED) {
			co.run();
        }
        return hasElement;
    }

    public E next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        E result = element;
        hasElement = false;
        element = null;
        return result;
    }

    /**
     * Always throws UnsupportedOperationException.
     * @throws java.lang.UnsupportedOperationException always
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Produces the next value to be returned by the {@link #next} method.
     * 
     * @param element The value that should be returned by {@link #next}
     * @throws de.matthiasmann.continuations.SuspendExecution This method will suspend the execution
     */
    protected void produce(E element) throws SuspendExecution {
        if(hasElement) {
            throw new IllegalStateException("hasElement = true");
        }
        this.element = element;
        hasElement = true;
        co.yield();
    }
    
    /**
     * <p>This is the body of the Iterator. This method is executed as a
     * {@link Coroutine} to {@link #produce} the values of the Iterator.</p>
     * 
     * <p>Note that this method is suspended each time it calls produce. And if
     * the consumer does not consume all values of the Iterator then this 
     * method does not get the change to finish it's execution. This also
     * includes the finally blocks.</p>
     * 
     * <p>This method must only suspend by calling produce. Any other reason
     * for suspension will cause a busy loop in the Iterator.</p>
     * 
     * @throws de.matthiasmann.continuations.SuspendExecution
     */
    protected abstract void run() throws SuspendExecution;

}
