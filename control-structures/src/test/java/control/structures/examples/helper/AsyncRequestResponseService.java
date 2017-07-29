package control.structures.examples.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import co.paralleluniverse.strands.Strand;
import rx.Observer;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncRequestResponseService;

// https://github.com/ReactiveX/RxJava/wiki/How-To-Use-RxJava
public class AsyncRequestResponseService implements IAsyncRequestResponseService {
	
	private static final Logger logger = LoggerFactory.getLogger(AsyncRequestResponseService.class);
	
	protected static final ExecutorService executor = Executors.newFixedThreadPool( 1, new ThreadFactoryBuilder().setNameFormat("async-request-response-service-%d").build());
	
	private int sleep_millis = -1;
	
	public AsyncRequestResponseService() {}
	public AsyncRequestResponseService(int sleep_millis) {
		this.sleep_millis = sleep_millis;
	}
	
	public void request(String someParameter, Observer<Double> callback) {
		if("x".equals(someParameter)) {
			executor.execute(() -> {
				if(sleep_millis > 0) {
					try {
						Strand.sleep(sleep_millis);
					} catch (Exception e) {
						logger.error("Thread.sleep() threw an exception!", e);
					}
				}
				callback.onNext(3.0);
				callback.onCompleted();
			});
		} else if("y".equals(someParameter)) {
			callback.onNext(4.0);				
			callback.onCompleted();
		} else {
			callback.onError(new RuntimeException("Don't know parameter: '" + someParameter + "'"));
		}
	}
}