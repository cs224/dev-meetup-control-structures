package control.structures.examples.helper;

import rx.Observer;
import rx.Subscriber;
import rx.Observable.OnSubscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.structures.examples.PythagorasUsingServiceCalls.IAsyncRequestResponseService;
import control.structures.examples.PythagorasUsingServiceCalls.IObservableAsyncRequestResponseService;
import control.structures.examples.PythagorasUsingServiceCalls.ObserverLoggingWrapper;

public class ObservableAsyncRequestResponseService implements IObservableAsyncRequestResponseService {
	
	static final Logger logger = LoggerFactory.getLogger(ObservableAsyncRequestResponseService.class);
	
	protected final IAsyncRequestResponseService service;
	
	public ObservableAsyncRequestResponseService(IAsyncRequestResponseService service) {
		this.service = service;
	}

	@Override
	public rx.Observable<Double> request(String someParameter) {
		OnSubscribe<Double> onSubscribe = new OnSubscribe<Double>(){
			@Override
			public void call(Subscriber<? super Double> t1) {
				logger.debug("OnSubscribe(): someParameter: '" + someParameter + "'");
				
				@SuppressWarnings("unchecked")
				Subscriber<Double> t2 = (Subscriber<Double>)t1;
				Observer<Double> callback = t2;
				ObserverLoggingWrapper wrapper = new ObserverLoggingWrapper(callback); 
				service.request(someParameter, wrapper);
			}
		};

		return rx.Observable.create(onSubscribe);
	}
	
}