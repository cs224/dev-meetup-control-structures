package control.structures.examples;

import static org.junit.Assert.*;
import static org.hamcrest.number.IsCloseTo.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableCallable;
import control.structures.continuations.MyExecutor;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService1;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService2;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncRequestResponseService;
import control.structures.examples.helper.AsyncDataflowRequestResponseService1;
import control.structures.examples.helper.AsyncDataflowRequestResponseService2;
import control.structures.examples.helper.AsyncRequestResponseService;
import control.structures.examples.helper.AsyncRequestResponseServiceCallbackOnOtherThread;
import control.structures.examples.helper.AsyncRequestResponseServiceListenerStyle;
import control.structures.examples.helper.ObservableAsyncRequestResponseService;


public class GivenAnEuclideanPointAsyncRequestResponseService {

	static final Logger logger = LoggerFactory.getLogger(GivenAnEuclideanPointAsyncRequestResponseService.class);

	protected static final IAsyncRequestResponseService service = new AsyncRequestResponseService();
	protected static final IAsyncRequestResponseService serviceListenerStyle = new AsyncRequestResponseServiceListenerStyle();
	protected static final IAsyncRequestResponseService serviceCallbackOnOtherThread = new AsyncRequestResponseServiceCallbackOnOtherThread();
	
	protected static final IAsyncDataflowRequestResponseService1<Double> asyncDataflowService1 = new AsyncDataflowRequestResponseService1();
	protected static final IAsyncDataflowRequestResponseService2<Double> asyncDataflowService2 = new AsyncDataflowRequestResponseService2();
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("my-scheduler-pythagoras", new MyExecutor());

	protected final ObservableAsyncRequestResponseService asyncRxService = new ObservableAsyncRequestResponseService(new AsyncRequestResponseService(500));
    
	protected static final double expected_result = 5.0;
	
	@Test
	public void test_pythagoras_function_call_async() throws InterruptedException, ExecutionException {
		logger.debug("starting");
		
		final CompletableFuture<Double> result = new CompletableFuture<Double>();
		
		PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputAsync1(service, result);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result.get(), closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_with_fibers_1() throws InterruptedException, ExecutionException {
		logger.debug("starting style 1 using an adapter using dataflow variables");
		double result = PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputAsync1(service);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_with_fibers_2() throws InterruptedException, ExecutionException {
		logger.debug("starting style 2 using an adapter using a channel");
		double result = PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputAsync2(service);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_with_fibers_listener_style() throws InterruptedException, ExecutionException {
		logger.debug("starting");
		double result = PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputAsync1(serviceListenerStyle);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_with_fibers_callback_on_other_thread() throws InterruptedException, ExecutionException {
		logger.debug("starting");
		double result = PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputAsync1(serviceCallbackOnOtherThread);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_dataflow_1() throws InterruptedException, ExecutionException, SuspendExecution {
		logger.debug("starting");
		Fiber<Double> fiber = new Fiber<>(myFiberScheduler, (SuspendableCallable<Double>)() -> {
			return PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputDataflowAsync1(asyncDataflowService1);
		});
		fiber.start();
		double result = fiber.get();
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_async_dataflow_2() throws InterruptedException, ExecutionException, SuspendExecution {
		logger.debug("starting");
		Fiber<Double> fiber = new Fiber<>(myFiberScheduler, (SuspendableCallable<Double>)() -> {
			return PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInputDataflowAsync2(asyncDataflowService2);
		});
		fiber.start();
		double result = fiber.get();
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_with_local_variables_async_rx_style_1() {
		logger.debug("starting");
		double result = PythagorasUsingServiceCalls.pythagoras_function_call_with_local_variables_async_rx_style_1(asyncRxService);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_function_call_with_local_variables_async_rx_style_2() {
		logger.debug("starting");
		double result = PythagorasUsingServiceCalls.pythagoras_function_call_with_local_variables_async_rx_style_2(asyncRxService);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}

	@Test
	public void test_pythagoras_reactive_extensions_style_async() {
		logger.debug("starting");
		double result = PythagorasUsingServiceCalls.pythagoras_reactive_extensions_style_async(asyncRxService);
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}
}
