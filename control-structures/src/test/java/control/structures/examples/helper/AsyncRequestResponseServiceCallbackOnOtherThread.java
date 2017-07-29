package control.structures.examples.helper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import control.structures.examples.PythagorasUsingServiceCalls.IAsyncRequestResponseService;
import rx.Observer;

public class AsyncRequestResponseServiceCallbackOnOtherThread implements IAsyncRequestResponseService {
	
	protected static final ExecutorService executor = Executors.newFixedThreadPool(100, new ThreadFactoryBuilder().setNameFormat("async-request-response-service-callback-on-other-thread-%d").build());
	
	public void request(String someParameter, Observer<Double> callback) {
		if("x".equals(someParameter)) {
			executor.execute(() -> {
				callback.onNext(3.0);
				callback.onCompleted();
			});
		} else if("y".equals(someParameter)) {
			executor.execute(() -> {
				callback.onNext(4.0);				
				callback.onCompleted();
			});
		} else {
			callback.onError(new RuntimeException("Don't know parameter: '" + someParameter + "'"));
		}
	}
}