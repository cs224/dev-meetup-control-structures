package control.structures.examples.corethread;

import static org.junit.Assert.*;
//import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.strands.Strand;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService1;

public class CoreThreadTest {

	static final Logger logger = LoggerFactory.getLogger(CoreThreadTest.class);
	protected static final IAsyncDataflowRequestResponseService1<Double> asyncDataflowService1 = new AsyncDataflowRequestResponseService1();
	
	@Test
	public void t1() throws InterruptedException {
		EventLoop eventLoop = new EventLoop("CoreThreadTest.EventLoop");
		ExamplePingPongCommand examplePingPongCommand = new ExamplePingPongCommand(asyncDataflowService1);
		eventLoop.process(examplePingPongCommand);
		
		eventLoop.process((i) -> {logger.info("doing something else in between 1");});
		eventLoop.process((i) -> {logger.info("doing something else in between 2");});
		
		new Thread(() -> {
			try {
				logger.debug("going to sleep for 1000ms");
				Strand.sleep(1000);
			} catch (Exception e) {
				logger.error("Should never happen");
			}
			logger.debug("triggering to kill the event loop by setting its interrupted flag.");
			eventLoop.process((i) -> {logger.debug("kill-event-loop: interrupting.");Thread.currentThread().interrupt();});
		}, "kill-event-loop").start();
		
		logger.debug("calling: eventLoop.getCoreThread().join();");
		eventLoop.getCoreThread().join();
		
		assertTrue("nop", true);
	}
	
}
