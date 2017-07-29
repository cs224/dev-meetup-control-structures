package control.structures.continuations;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;

public class CoIteratorTest {
	
	private static final Logger logger = LoggerFactory.getLogger(CoIteratorTest.class);

	@Test
	public void test() throws ExecutionException, InterruptedException {
		Iterator<String> iter = new TestIterator();
    	try {
    		while(iter.hasNext()) {
    			logger.info(iter.next());
    		}
		} catch (Exception e) {
			String msg = "Unexpected exception thrown.";
			logger.error(msg, e);
			fail(msg);
		}
	}

	public static class TestIterator extends CoIterator<String> implements Serializable {
		private static final long serialVersionUID = 1L;

		@Override
	    protected void run() throws SuspendExecution {
	        produce("A");
	        produce("B");
	        for(int i = 0; i < 4; i++) {
	            produce("C" + i);
	        }
	        produce("D");
	        produce("E");
	    }

	}
}
