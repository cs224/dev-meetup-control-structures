package control.structures.examples.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import co.paralleluniverse.strands.Strand;
import control.structures.dataflow.DataflowVariable;
import control.structures.dataflow.QuasarVarDataflowVariableRepository;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService2;

public class AsyncDataflowRequestResponseService2  implements IAsyncDataflowRequestResponseService2<Double> {

	private static final Logger logger = LoggerFactory.getLogger(AsyncDataflowRequestResponseService2.class);
	
	protected static final ExecutorService executor = Executors.newFixedThreadPool( 1, new ThreadFactoryBuilder().setNameFormat("async-dataflow-request-response-service-2-%d").build());
	
	@Override
	public DataflowVariable<Double> request(String someParameter) {
		DataflowVariable<Double> result = QuasarVarDataflowVariableRepository.createReference(() -> new Double(0.0), someParameter);
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
		return result;
	}

}
