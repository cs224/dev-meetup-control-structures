package control.structures.examples;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.SuspendableRunnable;
import control.structures.continuations.MyExecutor;
import control.structures.examples.YouPullIllPush.Buffer;
import control.structures.examples.YouPullIllPush.Buffer.BufferIterator;
import control.structures.examples.YouPullIllPush.Buffer.BufferProducerIterator;


public class YouPullIllPushTest {

	static final Logger logger = LoggerFactory.getLogger(YouPullIllPushTest.class);
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("you-pull-ill-push", new MyExecutor());

    private static final FiberScheduler myFiberProduerSchedulerThread = new FiberExecutorScheduler("producer-threads",
    		Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("+producer-threads-%d").build())
    	);
    private static final FiberScheduler myFiberConsumerSchedulerThread = new FiberExecutorScheduler("consumer-threads",
    		Executors.newFixedThreadPool(1, new ThreadFactoryBuilder().setNameFormat("-consumer-threads-%d").build())
    	);
    
    
    public void testYouPullIllPush(int bufferSize, FiberScheduler producerScheduler, int producerSleepMillis, FiberScheduler consumerScheduler, int consumerSleepMillis) throws ExecutionException, InterruptedException {
		logger.debug("starting testYouPullIllPush()");		
		final AtomicInteger lastIntegerProduced = new AtomicInteger(-1);
		final AtomicInteger lastIntegerConsumed = new AtomicInteger(-1);
		final int MAX = 10;

		Supplier<Integer> producer = new Supplier<Integer>() {
			final int max = MAX;
			int i = 0;
			
			@Override
			public Integer get() {
				if(i < max) {
					lastIntegerProduced.set(i);
					return i++;
				}
				return null;
			}
		};

		Consumer<Integer> consumer = new Consumer<Integer>() {
			@Override
			public void accept(Integer t) {
				lastIntegerConsumed.set(t);
				logger.debug("value: " + t);
			}
		};
		
		BufferIterator<Integer> iterator1 = null;
		BufferProducerIterator<Integer> producerIterator1 = null;
		{
			/*
			 * "Losing your head": the curly braces make sure that we "lose the head(s)" of the buffer and do not accumulate large amounts of memory
			 * We only keep the iterator and the producerIterator!
			 * 
			 * Have a look at the following links for more explanations:
			 * http://stackoverflow.com/questions/5698122/explanation-of-lose-your-head-in-lazy-sequences
			 * http://stackoverflow.com/questions/20100830/avoiding-holding-onto-head
			 * http://clojure.org/reference/lazy#_don_t_hang_onto_your_head
			 */
			Buffer<Integer> buffer = null;
			if(bufferSize > 1)
				buffer = new Buffer<>(bufferSize);
			else
				buffer = new Buffer<>();
			
			iterator1 = buffer.iterator();
			producerIterator1 = buffer.producerIterator(producer);
		}
		final BufferIterator<Integer> iterator = iterator1;
		final BufferProducerIterator<Integer> producerIterator = producerIterator1;
				
		
		Fiber<Void> f1 = new Fiber<>(consumerScheduler, (SuspendableRunnable)() -> {
			while(iterator.hasNext()) {
				Integer next = iterator.next();
				logger.debug("consumer consume");
				consumer.accept(next);
				if(consumerSleepMillis > 0)
					Strand.sleep(consumerSleepMillis);
			}
		});
		f1.start();

		Fiber<Void> f2 = new Fiber<>(producerScheduler, (SuspendableRunnable)() -> {
			while(producerIterator.hasNext()) {
				logger.debug("producer produce");
				producerIterator.push();
				if(producerSleepMillis > 0)
					Strand.sleep(producerSleepMillis);
			}
		});
		f2.start();
		
		f1.join();
		f2.join();
		assertThat("The final produced value should be the MAX value minus 1", lastIntegerProduced.get(), equalTo(MAX - 1));
		assertThat("The final consumed value should be the MAX value minus 1", lastIntegerProduced.get(), equalTo(MAX - 1));    	
    }
    
	@Test
	public void testYouPullIllPush() throws InterruptedException, ExecutionException {
		testYouPullIllPush(0, myFiberScheduler, 0, myFiberScheduler, 0);
	}

	@Test
	public void testYouPullIllPushWithThreadsAndSlowConsumerPush() throws InterruptedException, ExecutionException {
		testYouPullIllPush(20, myFiberProduerSchedulerThread, 5, myFiberConsumerSchedulerThread, 100);
		// the following should basically cause all productions to run before the consumptions: 
		//testYouPullIllPush(20, myFiberScheduler, 0, myFiberScheduler, 100);
		// the following should show that a delay in the producer will also cause a delay in the consumer 
		//testYouPullIllPush(20, myFiberScheduler, 5, myFiberScheduler, 0);
	}

	@Test
	public void testYouPullIllPushWithThreadsAndSlowConsumerPull() throws InterruptedException, ExecutionException {
		testYouPullIllPush(1, myFiberProduerSchedulerThread, 5, myFiberConsumerSchedulerThread, 100);
	}
	
}
