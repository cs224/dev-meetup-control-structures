package control.structures.examples;

import java.io.Serializable;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import control.structures.continuations.MyExecutor;
import control.structures.dataflow.DataflowVariable;
import control.structures.dataflow.QuasarVarDataflowVariableRepository;
import control.structures.helpers.HandleInterruptedException;
import rx.Observable;

public class PythagorasUsingServiceCalls {

	private static final Logger logger = LoggerFactory.getLogger(PythagorasUsingServiceCalls.class);

	private static final Pythagoras pythagoras = new Pythagoras(false);
	
	public interface ISyncRequestResponseService {
		public double request(String someParameter) throws SuspendExecution;
	}
	    
	public static double calculateDistanceFromOriginFromServiceInput(ISyncRequestResponseService service) throws SuspendExecution {
		double result = sqrtFromServiceInput(service);
		logger.debug("result is: " + result);
		return result;
	}
	
	public static double sqrtFromServiceInput(ISyncRequestResponseService service) throws SuspendExecution {
		double result = Math.sqrt(sumFromServiceInput(service));
		return result;
	}
	
	public static double sumFromServiceInput(ISyncRequestResponseService service) throws SuspendExecution {
		double xsquare = xsquareFromServiceInput(service);
		double ysquare = ysquareFromServiceInput(service);
		return xsquare + ysquare;
	}

	public static double xsquareFromServiceInput(ISyncRequestResponseService service) throws SuspendExecution {
		logger.debug("Starting to query service for 'x'.");
		double x = service.request("x");
		logger.debug("Finished to query service for 'x' and got result: " + x);
		return x*x;
	}

	public static double ysquareFromServiceInput(ISyncRequestResponseService service) throws SuspendExecution {
		logger.debug("Starting to query service for 'y'.");
		double y = service.request("y");
		logger.debug("Finished to query service for 'y' and got result: " + y);
		return y*y;
	}
	
	public static double doubleFromUserInput(String q) {
		double result = 0.0;
		System.out.println("Type a double number '" + q + "': ");
		@SuppressWarnings("resource") // I cannot close System.in 
		Scanner scanIn = new Scanner(System.in);
		String answer = scanIn.nextLine();
		boolean success = false;
		while(!success) {
			try {
				result = Double.parseDouble(answer);
				success = true;
			} catch(Exception e) {
				logger.error("A wrong number was entered that could not be parsed to a double:", e);
			}				
		}
	 
		return result;
	}
	
	interface CallBack<T> {
	    void methodToCallBack(T arg);
	}

	public interface Observer extends rx.Observer<Double> {

		/*
		@Override
		public void onNext(Double t) {
			logger.debug("onNext(): '" + t + "'");
		}
		*/
		
		@Override
		default public void onCompleted() {
			logger.debug("onCompleted()");
		}

		@Override
		default public void onError(Throwable e) {
			logger.error("onError()", e);
		}
	}

	public interface ObserverInCoroutine extends rx.Observer<Double> {

		public void onNextInCoroutine(Double t) throws InterruptedException, SuspendExecution;
		
		@Override
	    @Suspendable
		default public void onNext(Double t) {
			logger.debug("onNext(): '" + t + "'");
			try {
				onNextInCoroutine(t);
			} catch (InterruptedException | SuspendExecution e) {
				logger.error("should never happen!", e);
			}
		}
		
		@Override
		default public void onCompleted() {
			logger.debug("onCompleted()");
		}

		@Override
		default public void onError(Throwable e) {
			logger.error("onError()", e);
		}
	}
	
	public static class ObserverLoggingWrapper implements rx.Observer<Double> {
		
		protected final rx.Observer<Double> wrapped;
		
		public ObserverLoggingWrapper(rx.Observer<Double> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void onNext(Double t) {
			logger.debug("onNext(): '" + t + "'");
			wrapped.onNext(t);
		}
		
		@Override
		public void onCompleted() {
			logger.debug("onCompleted()");
			wrapped.onCompleted();
		}

		@Override
		public void onError(Throwable e) {
			logger.error("onError()", e);
			wrapped.onError(e);
		}
	}

	public interface IAsyncRequestResponseService {
		public void request(String someParameter, rx.Observer<Double> callback);		
	}
	
	public static void calculateDistanceFromOriginFromServiceInputAsync1
	(IAsyncRequestResponseService service, CompletableFuture<Double> finalResultHolder) 
	{
		Observer o = (Double result) -> calculateDistanceFromOriginFromServiceInputAsync2(service, finalResultHolder, result);
		service.request("x", o);
	}
	
	public static void calculateDistanceFromOriginFromServiceInputAsync2
	(IAsyncRequestResponseService service, CompletableFuture<Double> finalResultHolder, double x) 
	{
		Observer o = (Double result) -> calculateDistanceFromOriginFromServiceInputAsync3(service, finalResultHolder, x, result);
		service.request("y", o);
	}

	public static void calculateDistanceFromOriginFromServiceInputAsync3
	(IAsyncRequestResponseService service, CompletableFuture<Double> finalResultHolder, double x, double y) 
	{
		double result = pythagoras.pythagoras_function_call_with_local_variables(x, y);
		logger.debug("providing result in the finalResultHolder");
		finalResultHolder.complete(result);
	}
	
	public interface IAsyncDataflowRequestResponseService1<T extends Serializable> {
		public void request(String someParameter,  DataflowVariable<T> result);		
	}
	
	public interface IAsyncDataflowRequestResponseService2<T extends Serializable> {
		public DataflowVariable<T> request(String someParameter);		
	}
	
	public static double calculateDistanceFromOriginFromServiceInputDataflowAsync1
	(IAsyncDataflowRequestResponseService1<Double> service) throws SuspendExecution, InterruptedException 
	{
		logger.info("-----------------------------------------------------------------------------");
		logger.info("calculateDistanceFromOriginFromServiceInputDataflowAsync1");
		
		DataflowVariable<Double> x = QuasarVarDataflowVariableRepository.createReference(() -> new Double(0.0), "x");
		service.request("x", x);
		DataflowVariable<Double> y = QuasarVarDataflowVariableRepository.createReference(() -> new Double(0.0), "y");
		service.request("y", y);
		
		logger.debug("xsquared");
		double xsquared = pythagoras.times(x.get(), x.get());
		logger.debug("ysquared");
		double ysquared = pythagoras.times(y.get(), y.get());
		logger.debug("squaresum");
		double squaresum = pythagoras.plus(xsquared, ysquared);
		logger.debug("distance");
		double distance = pythagoras.sqrt(squaresum);
		return distance;
	}
	
	public static double calculateDistanceFromOriginFromServiceInputDataflowAsync2
	(IAsyncDataflowRequestResponseService2<Double> service) throws SuspendExecution, InterruptedException 
	{
		logger.info("-----------------------------------------------------------------------------");
		logger.info("calculateDistanceFromOriginFromServiceInputDataflowAsync2");
		
		DataflowVariable<Double> x = service.request("x");
		DataflowVariable<Double> y = service.request("y");
		
		logger.debug("xsquared");
		double xsquared = pythagoras.times(x.get(), x.get());
		logger.debug("ysquared");
		double ysquared = pythagoras.times(y.get(), y.get());
		logger.debug("squaresum");
		double squaresum = pythagoras.plus(xsquared, ysquared);
		logger.debug("distance");
		double distance = pythagoras.sqrt(squaresum);
		return distance;
	}
	
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("my-scheduler-pythagoras-using-service-calls", new MyExecutor());
	
	public static class SyncRequestResponseServiceAdapter1 implements ISyncRequestResponseService {
		
		private final IAsyncRequestResponseService service;
		
		public SyncRequestResponseServiceAdapter1(IAsyncRequestResponseService service) {
			this.service = service;
		}

		@Override
		public double request(String someParameter) throws SuspendExecution {
			DataflowVariable<Double> dv = QuasarVarDataflowVariableRepository.createReference(() -> new Double(0.0), someParameter);
			ObserverInCoroutine o = (Double r) -> dv.bind(r);
			service.request(someParameter, o);
			HandleInterruptedException<Double> h = new HandleInterruptedException<>(() -> 
			{
				logger.debug("dv.get();");
				return dv.get();
			});
			return h.call();
		}
		
	}
	
	public static double calculateDistanceFromOriginFromServiceInputAsync1(IAsyncRequestResponseService service) 
			throws ExecutionException, InterruptedException 
	{
		logger.info("-----------------------------------------------------------------------------");
		logger.info("calculateDistanceFromOriginFromServiceInputAsync1");
		
		final SyncRequestResponseServiceAdapter1 adapter = new SyncRequestResponseServiceAdapter1(service); 
		
		Fiber<Double> fiber = new Fiber<>(myFiberScheduler, (SuspendableCallable<Double>)() -> {
			logger.debug("Starting fiber");
			double result = calculateDistanceFromOriginFromServiceInput(adapter);
			logger.debug("Exiting fiber");
			return result;
		});
		fiber.start();
		double r = fiber.get();
		logger.debug("Returning result: '" + r + "'");
		return r;
	}

	public static class SyncRequestResponseServiceAdapter2 implements ISyncRequestResponseService {
		
		private final IAsyncRequestResponseService service;
		
		public SyncRequestResponseServiceAdapter2(IAsyncRequestResponseService service) {
			this.service = service;
		}

		@Override
		public double request(String someParameter) throws SuspendExecution {
			final Channel<Double> channel = Channels.newChannel(1);
			ObserverInCoroutine o = (Double r) -> channel.send(r);
			service.request(someParameter, o);
			HandleInterruptedException<Double> h = new HandleInterruptedException<>(() -> 
			{
				logger.debug("channel.receive();");
				return channel.receive();
			});
			return h.call();
		}
		
	}
	
	public static double calculateDistanceFromOriginFromServiceInputAsync2(IAsyncRequestResponseService service) 
			throws ExecutionException, InterruptedException 
	{
		logger.info("-----------------------------------------------------------------------------");
		logger.info("calculateDistanceFromOriginFromServiceInputAsync2");
		
		final SyncRequestResponseServiceAdapter2 adapter = new SyncRequestResponseServiceAdapter2(service); 
		
		Fiber<Double> fiber = new Fiber<>(myFiberScheduler, (SuspendableCallable<Double>)() -> {
			logger.debug("Starting fiber");
			double result = calculateDistanceFromOriginFromServiceInput(adapter);
			logger.debug("Exiting fiber");
			return result;
		});
		fiber.start();
		double r = fiber.get();
		logger.debug("Returning result: '" + r + "'");
		return r;
	}
	
	public static double pythagoras_function_call_with_local_variables_async_rx_style_1(IObservableAsyncRequestResponseService service) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_function_call_with_local_variables_async_rx_style_1");
		
		logger.debug("service.request('x');");
		Observable<Double> x_observable = service.request("x");
		logger.debug("service.request('y');");
		Observable<Double> y_observable = service.request("y");
		logger.debug("x_observable.toBlocking().single()");
		double x = x_observable.toBlocking().single();
		logger.debug("y_observable.toBlocking().single()");
		double y = y_observable.toBlocking().single();
		
		logger.debug("rest of the calculation");
		double xsquared = pythagoras.times(x, x);
		double ysquared = pythagoras.times(y, y);
		double squaresum = pythagoras.plus(xsquared, ysquared);
		double distance = pythagoras.sqrt(squaresum);
		return distance;
	}
	
	public static double pythagoras_function_call_with_local_variables_async_rx_style_2(IObservableAsyncRequestResponseService service) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_function_call_with_local_variables_async_rx_style_2");
		
		logger.debug("service.request('x');");
		Observable<Double> x_observable = service.request("x").cache();
		x_observable.subscribe();
		logger.debug("service.request('y');");
		Observable<Double> y_observable = service.request("y").cache();
		y_observable.subscribe();
		logger.debug("x_observable.toBlocking().single()");
		double x = x_observable.toBlocking().single();
		logger.debug("y_observable.toBlocking().single()");
		double y = y_observable.toBlocking().single();
		
		logger.debug("rest of the calculation");
		double xsquared = pythagoras.times(x, x);
		double ysquared = pythagoras.times(y, y);
		double squaresum = pythagoras.plus(xsquared, ysquared);
		double distance = pythagoras.sqrt(squaresum);
		return distance;
	}

	public static double pythagoras_reactive_extensions_style_async(IObservableAsyncRequestResponseService service) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_reactive_extensions_style_async");
		
		final CompletableFuture<Double> result = new CompletableFuture<Double>();		

		logger.debug("About to create observables.");
		
		Observable<Double> ox = service.request("x");
		rx.Observable<Double> ox_square = ox.map((Double i) -> i*i);
		Observable<Double> oy = service.request("y");
		rx.Observable<Double> oy_square = oy.map((Double i) -> i*i); 
		
		logger.debug("Observables created now about to start.");
		
		rx.Observable<Pair<Double,Double>> pairs = rx.Observable.zip(ox_square, oy_square, (Double d1, Double d2) -> new Pair<Double,Double>(d1, d2));
		pairs
			.map(p -> (p.getValue0() + p.getValue1()))
			.map(p -> Math.sqrt(p))
			.subscribe(p -> result.complete(p));
		
		double r = -1.0;
		try {
			r = result.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("pythagoras_reactive_extensions_style_async()", e);
		}

		logger.debug("pythagoras_reactive_extensions_style_async() result: '" + r + "'");
		
		return r;
	}

	public interface IObservableAsyncRequestResponseService {
		public rx.Observable<Double> request(String someParameter);		
	}
	
}
