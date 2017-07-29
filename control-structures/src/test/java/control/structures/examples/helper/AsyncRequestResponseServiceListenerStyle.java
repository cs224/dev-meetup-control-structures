package control.structures.examples.helper;

import rx.Observer;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncRequestResponseService;

// https://github.com/ReactiveX/RxJava/wiki/How-To-Use-RxJava
public class AsyncRequestResponseServiceListenerStyle implements IAsyncRequestResponseService {
	
	public void request(String someParameter, Observer<Double> callback) {
		if("x".equals(someParameter)) {
			callback.onNext(3.0);
			callback.onCompleted();
		} else if("y".equals(someParameter)) {
			callback.onNext(4.0);				
			callback.onCompleted();
		} else {
			callback.onError(new RuntimeException("Don't know parameter: '" + someParameter + "'"));
		}
	}
}