package control.structures.examples.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import co.paralleluniverse.strands.Strand;
import control.structures.dataflow.DataflowVariable;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService1;

public class AsyncDataflowRequestResponseService1 implements IAsyncDataflowRequestResponseService1<Double> {

	private static final Logger logger = LoggerFactory.getLogger(AsyncDataflowRequestResponseService1.class);
	
	protected static final ExecutorService executor = Executors.newFixedThreadPool( 1, new ThreadFactoryBuilder().setNameFormat("async-dataflow-request-response-service-1-%d").build());
	
	@Override
	public void request(String someParameter, DataflowVariable<Double> result) {
		if("x".equals(someParameter)) {
			executor.execute(() -> {
				try {
					Strand.sleep(500);
				} catch (Exception e) {
					logger.error("Thread.sleep() threw an exception!", e);
				}
				result.bind(3.0);
			});
		} else if("y".equals(someParameter)) {
			result.bind(4.0);
		} else {
			throw new RuntimeException("Don't know parameter: '" + someParameter + "'");
		}
	}

}
