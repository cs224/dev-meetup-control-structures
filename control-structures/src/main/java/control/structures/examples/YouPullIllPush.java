package control.structures.examples;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.HdrHistogram.Histogram;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import co.paralleluniverse.fibers.SuspendExecution;
import control.structures.dataflow.DataflowVariable;
import control.structures.dataflow.QuasarVarDataflowVariableRepository;


public class YouPullIllPush {

	static final Logger logger = LoggerFactory.getLogger(YouPullIllPush.class);
	static final MetricRegistry metrics = new MetricRegistry();
	
	public static class Buffer<T extends Serializable> implements Serializable {
		
		public static AtomicInteger count = new AtomicInteger();
		
		private static final long serialVersionUID = 3359827726409245481L;
		final protected Pair<DataflowVariable<T>, DataflowVariable<Buffer<T>>> current;
		final private int id;
		
		public String toString() {
			return "Buffer-" + id;
		}
		
		public Buffer() {
			id = count.incrementAndGet();
			DataflowVariable<T> head = QuasarVarDataflowVariableRepository.createReference((Supplier<T>) null  , "head-" + id);
			DataflowVariable<Buffer<T>> tail = QuasarVarDataflowVariableRepository.createReference((Supplier<Buffer<T>>) null  , "tail-" + id);
			current = new Pair<DataflowVariable<T>, DataflowVariable<Buffer<T>>>(head, tail);
		}

		public Buffer(int size) {
			this();
			for(int i = 0; i < size; i++) {
				provideSlot();
			}
		}
		
		protected T head() throws SuspendExecution, InterruptedException {
			logger.debug("provideSlot();");
			provideSlot(); // whenever I consume an element I also provide a new slot for the producer.
			return current.getValue0().get();
		}
		
		protected void provideSlot() {
			logger.debug("provideSlot();");
			Pair<DataflowVariable<T>, DataflowVariable<Buffer<T>>> ptr = current;
			while(ptr.getValue1().isDone()) {
				try {
					logger.debug("ptr = ptr.getValue1().get().current;");
					ptr = ptr.getValue1().get().current;
				} catch (SuspendExecution | InterruptedException e) {
					// you can swallow this exception, because you will never call get() if it would block!
				}
			}
			ptr.getValue1().bind(new Buffer<T>());
		}
		
		protected Buffer<T> tail() throws SuspendExecution, InterruptedException {
			return current.getValue1().get();
		}
		
		protected Buffer<T> push(T v) throws SuspendExecution, InterruptedException {
			Buffer<T> ptr = this;
			logger.debug("ptr: " + ptr);
			logger.debug("ptr.current.getValue0().isDone(): " + ptr.current.getValue0().isDone());
			while(ptr.current.getValue0().isDone()) {
				logger.debug("ptr = ptr.current.getValue1().get();");
				ptr = ptr.current.getValue1().get();
				logger.debug("ptr: " + ptr);
			}
			logger.debug("ptr.current.getValue0().bind(v): " + v);
			logger.debug("ptr: " + ptr);
			ptr.current.getValue0().bind(v);
			return ptr;
		}
		
		public BufferIterator<T> iterator() {
			return new BufferIterator<T>(this);
		}
		
		public BufferProducerIterator<T> producerIterator(Supplier<T> producer) {
			return new BufferProducerIterator<T>(this, producer);
		}

		public static class BufferIterator<T extends Serializable> {
			
			// A Histogram covering the range from 1 nsec to 1 ms with 3 decimal point resolution:
			final Histogram histogram = new Histogram(1_000_000_000L, 3);
			final Meter pull = metrics.meter("pull");
			Buffer<T> buffer = null;
			T result = null;
			long startTime = 0;
			long endTime = 0;
			
			public BufferIterator(Buffer<T> buffer) {
				this.buffer = buffer;
			}

			public boolean hasNext() throws SuspendExecution, InterruptedException {
				logger.debug("pull");
				startTime = endTime;
				endTime = System.nanoTime();
				if(startTime > 0)
					histogram.recordValue(endTime - startTime);
				result = buffer.head();
				pull.mark();
				if(null == result) {
					String perSecond = String.format("%.2f", pull.getMeanRate());
					String waitInMs = String.format("%.2f",1000.0 / pull.getMeanRate());
					logger.info("pull metrics: {}s^⁻1 mean wait time: {}ms", perSecond, waitInMs);
					
					System.err.println("pull metrics: recorded latencies [in msec]:");
			        histogram.outputPercentileDistribution(System.err, 1000000.0);
					return false;
				}
				return true;
			}

			public T next() throws SuspendExecution, InterruptedException {
				logger.debug("pull");
				buffer = buffer.tail();
				return result;
			}
		}

		public static class BufferProducerIterator<T extends Serializable> {
			
			// A Histogram covering the range from 1 nsec to 1 ms with 3 decimal point resolution:
			final Histogram histogram = new Histogram(1_000_000_000L, 3);
			final Meter push = metrics.meter("push");
			final Supplier<T> producer;
			Buffer<T> buffer = null;
			boolean hasNext = true;
			long startTime = 0;
			long endTime = 0;
			
			public BufferProducerIterator(Buffer<T> buffer, Supplier<T> producer) {
				this.producer = producer;
				this.buffer = buffer;
			}

			public boolean hasNext() throws SuspendExecution, InterruptedException {
				startTime = endTime;
				endTime = System.nanoTime();
				if(startTime > 0)
					histogram.recordValue(endTime - startTime);
				if(!hasNext) {
					String perSecond = String.format("%.2f", push.getMeanRate());
					String waitInMs = String.format("%.2f",1000.0 / push.getMeanRate());
					logger.info("push metrics: {}s^⁻1 mean wait time: {}ms", perSecond, waitInMs);

					System.err.println("push metrics: recorded latencies [in msec]:");
			        histogram.outputPercentileDistribution(System.err, 1000000.0);
				}
				return hasNext;
			}

			public void push(T v) throws SuspendExecution, InterruptedException {
				if(null == v) {
					hasNext = false;
				}
				logger.debug("buffer.push(T v): " + v);
				logger.debug("buffer before: " + buffer);
				buffer = buffer.push(v);
				push.mark();
				logger.debug("buffer after: " + buffer);
			}
			
			public void push() throws SuspendExecution, InterruptedException {
				T v = producer.get();
				push(v);
			}
		}
		
	}
}
