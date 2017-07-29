package control.structures.examples;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.BiFunction;
import java.util.function.Consumer;
//import java.util.function.Function;
import java.util.function.Supplier;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.structures.continuations.MyExecutor;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberExecutorScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.Channels.OverflowPolicy;
import co.paralleluniverse.strands.channels.ReceivePort;
import co.paralleluniverse.strands.dataflow.Val;
import co.paralleluniverse.strands.dataflow.Var;

public class Pythagoras {
	
	private static final Logger logger = LoggerFactory.getLogger(Pythagoras.class);
    private static final FiberScheduler myFiberScheduler = new FiberExecutorScheduler("my-scheduler-pythagoras", new MyExecutor());
	
    private final boolean withStacktraces;
    
    public Pythagoras() {
    	withStacktraces = true;
    }

    public Pythagoras(boolean withStacktraces) {
    	this.withStacktraces = withStacktraces;
    }
    
	public double pythagoras_function_call_with_local_variables(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_function_call_with_local_variables");
		
		logger.debug("xsquared");
		double xsquared = times(x, x);
		logger.debug("ysquared");
		double ysquared = times(y, y);
		logger.debug("squaresum");
		double squaresum = plus(xsquared, ysquared);
		logger.debug("distance");
		double distance = sqrt(squaresum);
		return distance;
	}

	public double pythagoras_function_calls_per_level(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_function_calls_per_level");
		
		return sqrt(plus(times(x,x),times(y,y)));
	}

	public double pythagoras_function_calls_per_level_by_need(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_function_calls_per_level_by_need");
		
		return sqrt_by_need(plus_by_need(() -> times(x,x), () -> times(y,y))).get();
	}

	public Supplier<Double> plus_by_need(Supplier<Double> a, Supplier<Double> b) {
		return () -> {logger.debug("plus_by_need"); return a.get() + b.get();};
	}

	public Supplier<Double> sqrt_by_need(Supplier<Double> a) {
		return () -> {logger.debug("sqrt_by_need"); return Math.sqrt(a.get());};
	}
		
	public double pythagoras_continuation_passing_style(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_continuation_passing_style");
		
		final AtomicReference<Double> result = new AtomicReference<Double>();
		times_cps(x, x, 
			(Double i) -> times_cps(y, y, 
				(Double j) -> plus_cps(i, j,
					(Double k) -> sqrt_cps(k,
							(Double l) -> identity(l, result)
						)
					)
				)
			);
		
		return result.get();
	}	

	public void times_cps(double a, double b, Consumer<Double> k) {
		logger.debug("times_cps");
		logger.debug("stack", new Exception("stack"));
		k.accept(a * b);
	}
	
	public void plus_cps(double a, double b, Consumer<Double> k) {
		logger.debug("plus_cps");
		k.accept(a + b);
	}
	
	public void sqrt_cps(double a, Consumer<Double> k) {
		logger.debug("sqrt_cps");
		k.accept(Math.sqrt(a));
	}
	
	public void identity(double a, AtomicReference<Double> result) {
		logger.debug("identity");
		logger.debug("stack", new Exception("stack"));
		result.set(a);
	}
	
	public double fn(double i1, double i2, double in) {
		return 0.0;
	}

	public void fn_cps(double i1, double i2, double in, Consumer<Double> k) {
		k.accept(0.0);
	}
		
	public double pythagoras_dataflow(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_dataflow");
		
		Val<Double> vx = new Val<>();
		Val<Double> vy = new Val<>();
		
		Var<Double> distance = pythagoras_dataflow_flownetwork_definition(vx, vy);
		
		vx.set(x);
		vy.set(y);
		
		try {
			return distance.get();
		} catch (SuspendExecution | InterruptedException e) {
			return -1.0;
		}
	}

	public Var<Double> pythagoras_dataflow_flownetwork_definition(Val<Double> x, Val<Double> y) {
		Var<Double> xsquared = new Var<>(myFiberScheduler, () -> {logger.debug("dataflow: xsquared"); return times(x.get(), x.get());} );
		Var<Double> ysquared = new Var<>(myFiberScheduler, () -> {logger.debug("dataflow: ysquared"); return times(y.get(), y.get());} );
		Var<Double> squaresum = new Var<>(myFiberScheduler, () -> {logger.debug("dataflow: squaresum"); return xsquared.get() + ysquared.get();});
		return new Var<>(myFiberScheduler, () -> {logger.debug("dataflow: sqrt"); return Math.sqrt(squaresum.get());});		
	}
	
	public double pythagoras_communicating_sequential_processes_style(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_communicating_sequential_processes_style");
		
		final Channel<Double> xc = Channels.newChannel(1, OverflowPolicy.BLOCK);
		final Channel<Double> xc_square = Channels.newChannel(1, OverflowPolicy.BLOCK);
		
		@SuppressWarnings("unused")
		final Fiber<Void> f1 = fiber(() -> {
			while(!xc.isClosed()) {
				Double receive = xc.receive();
				logger.debug("CSP-style: xc.receive(): " + receive);
				xc_square.send(receive * receive);
			}
			xc_square.close();
		});

		final Channel<Double> yc = Channels.newChannel(1, OverflowPolicy.BLOCK);
		final Channel<Double> yc_square = Channels.newChannel(1, OverflowPolicy.BLOCK);
				
		@SuppressWarnings("unused")
		final Fiber<Void> f2 = fiber(() -> {
			while(!yc.isClosed()) {
				Double receive = yc.receive();
				logger.debug("CSP-style: yc.receive(): " + receive);
				yc_square.send(receive * receive);
			}
			yc_square.close();
		});

		final Channel<Double> square_sum = Channels.newChannel(1, OverflowPolicy.BLOCK);

		@SuppressWarnings("unused")
		final Fiber<Void> f3 = fiber(() -> {
			for(;;){
				if(xc_square.isClosed() || yc_square.isClosed())
					break;
				Double p1 = xc_square.receive();
				Double p2 = yc_square.receive();
				logger.debug("CSP-style: p1, p2: " + p1 + ", " + p2);
				square_sum.send(p1 + p2);
			}
			xc_square.close();
			yc_square.close();
			square_sum.close();
		});

		final Channel<Double> result = Channels.newChannel(1, OverflowPolicy.BLOCK);

		@SuppressWarnings("unused")
		final Fiber<Void> f4 = fiber(() -> {
			while(!square_sum.isClosed()) {
				Double p = square_sum.receive();
				logger.debug("CSP-style: square_sum.receive(): " + p);
				result.send(Math.sqrt(p));
			}
			result.close();
		});
		
		final AtomicReference<Double> r = new AtomicReference<>();

		@SuppressWarnings("unused")
		final Fiber<Void> f5 = fiber(() -> {
			yc.send(y);
			xc.send(x);
			r.set(result.receive());
		});
		
		/* we do not needed to wait here, because all the fibers are immediately started 
		try {
			f5.join();
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		*/
		
		return r.get();
	}
	
	public double pythagoras_event_driven_observer_pattern_style(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_event_driven_observer_pattern_style");
		
		DoubleObservable xo = new DoubleObservable();
		DoubleObservable xo_square = new DoubleObservable();
		
		
		xo.addObserver((java.util.Observable o, Object arg) ->  {
			double v = (double) arg;
			logger.debug("xsquared");
			xo_square.setValue(v*v);
		});

		DoubleObservable yo = new DoubleObservable();
		DoubleObservable yo_square = new DoubleObservable();
		
		yo.addObserver((java.util.Observable o, Object arg) ->  {
			double v = (double) arg;
			logger.debug("ysquared");
			yo_square.setValue(v*v);
		});
		
		XSquareYSquare squaresum_observer = new XSquareYSquare();
		xo_square.addObserver((java.util.Observable o, Object arg) ->  {
			squaresum_observer.setXSquare((double) arg);
		});
		yo_square.addObserver((java.util.Observable o, Object arg) ->  {
			squaresum_observer.setYSquare((double) arg);
		});
		
		DoubleObservable result = new DoubleObservable();
		squaresum_observer.addObserver((java.util.Observable o, Object arg) ->  {
			logger.debug("distance");
			logger.debug("stack", new Exception("stack"));
			result.setValue(Math.sqrt((double) arg));
		});
		
		xo.setValue(x);
		yo.setValue(y);
		
		return result.getValue();
	}

	public class DoubleObservable extends java.util.Observable {
		protected double value = -1.0;
		
		public DoubleObservable() {
		}

		public DoubleObservable(double init) {
			this.value = init;
		}
		
		public void setValue(double v) {
			value = v;
			setChanged();
			notifyObservers(v);
		}
		
		public double getValue() {
			return value;
		}
				
	}
	
	public class XSquareYSquare extends java.util.Observable {

		protected double squaresum = -1.0;
		protected double xsquare = -1.0;
		protected double ysquare = -1.0;
		
		public XSquareYSquare() {
		}
		
		public void setXSquare(double v) {
			if(xsquare != -1.0 && ysquare != -1.0) {
				// a new point is arriving
				xsquare = -1.0;
				ysquare = -1.0;				
				squaresum = -1.0;
			}
			xsquare = v;
			update();
		}

		public void setYSquare(double v) {
			if(xsquare != -1.0 && ysquare != -1.0) {
				// a new point is arriving
				xsquare = -1.0;
				ysquare = -1.0;
				squaresum = -1.0;
			}
			ysquare = v;
			update();
		}
		
		protected void update() {
			if(xsquare != -1.0 && ysquare != -1.0) {
				logger.debug("squaresum");
				squaresum = xsquare + ysquare;
				this.setChanged();
				this.notifyObservers(squaresum);
			}
		}
		
		public double getSquareSum() {
			return squaresum;
		}
		
	}
	
	/* Perhaps TODO:
	 * There are other styles than the observer style, typically with an event bus or a reactor:
	 *  https://spring.io/guides/gs/messaging-reactor/
	 *  http://java.dzone.com/articles/event-driven-programming-using
	 *  https://code.google.com/p/jed-java-event-distribution/
	 * 
	 */
	
	// http://reactivex.io/
	// https://github.com/ReactiveX/RxJava
	public double pythagoras_reactive_extensions_style(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_reactive_extensions_style");

		final AtomicReference<Double> result = new AtomicReference<Double>();		
		
		rx.Observable<Double> ox = rx.Observable.just(x);
		rx.Observable<Double> ox_square = ox.map((Double i) -> {logger.debug("rx: xsquared"); return i*i;});
		rx.Observable<Double> oy = rx.Observable.just(y);
		rx.Observable<Double> oy_square = oy.map((Double i) -> {logger.debug("rx: ysquared"); return i*i;}); 
		
		rx.Observable<Pair<Double,Double>> pairs = rx.Observable.zip(ox_square, oy_square, (Double d1, Double d2) -> new Pair<Double,Double>(d1, d2));
		pairs
			.map(p -> {logger.debug("rx: squaresum"); return (p.getValue0() + p.getValue1());})
			.map(p -> {logger.debug("rx: distance"); logger.debug("stack", new Exception("stack")); return Math.sqrt(p);})
			.subscribe(p -> result.set(p));
		
		return result.get();
	}

	public double pythagoras_reactive_extensions_blocking_observable_style_1(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_reactive_extensions_blocking_observable_style_1");
		
		rx.Observable<Double> ox = rx.Observable.just(x);
		rx.Observable<Double> ox_square = ox.map((Double i) -> i*i);
		rx.Observable<Double> oy = rx.Observable.just(y);
		rx.Observable<Double> oy_square = oy.map((Double i) -> i*i); 
		
		rx.Observable<Pair<Double,Double>> pairs = rx.Observable.zip(ox_square, oy_square, (Double d1, Double d2) -> new Pair<Double,Double>(d1, d2));
		Double single = pairs
			.map(p -> (p.getValue0() + p.getValue1()))
			.map(p -> {logger.debug("blocking rx 1: distance"); return Math.sqrt(p);})
			.toBlocking()
			.single();
		
		return single;
	}

	public double pythagoras_reactive_extensions_blocking_observable_style_2(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_reactive_extensions_blocking_observable_style_2");
		
		rx.Observable<Double> ox = rx.Observable.just(x);
		rx.Observable<Double> ox_square = ox.map((Double i) -> i*i);
		rx.Observable<Double> oy = rx.Observable.just(y);
		rx.Observable<Double> oy_square = oy.map((Double i) -> i*i); 
		
		rx.Observable<Pair<Double,Double>> pairs = rx.Observable.zip(ox_square, oy_square, (Double d1, Double d2) -> new Pair<Double,Double>(d1, d2));
		Iterator<Double> iterator = pairs
			.map(p -> (p.getValue0() + p.getValue1()))
			.map(p -> {logger.debug("blocking rx 2: distance"); return Math.sqrt(p);})
			.toBlocking()
			.getIterator();
		
		return iterator.next();
	}
	
	public double pythagoras_reactive_quasar_style(double x, double y) {
		logger.info("-----------------------------------------------------------------------------");
		logger.info("pythagoras_reactive_quasar_style");
		
		Channel<Double> xc = Channels.newChannel(1, OverflowPolicy.BLOCK);
		ReceivePort<Double> xc_square = Channels.map(xc, (Double i) -> i*i);
		Channel<Double> yc = Channels.newChannel(1, OverflowPolicy.BLOCK);
		ReceivePort<Double> yc_square = Channels.map(yc, (Double i) -> i*i);
		
		ReceivePort<Pair<Double,Double>> pairs = Channels.zip(xc_square, yc_square, (Double d1, Double d2) -> new Pair<Double,Double>(d1, d2));
		
		ReceivePort<Double> r1 = Channels.map(pairs, p -> (p.getValue0() + p.getValue1()));
		ReceivePort<Double> r2 = Channels.map(r1, p -> {logger.debug("rx quasar: distance"); return Math.sqrt(p);});
		
		double result = -1.0;
		try {
			xc.send(x);
			yc.send(y);
			result = r2.receive();
		} catch (SuspendExecution | InterruptedException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	// ------------------------
	
	public double squaresum(double x, double y) {
		double xsquared = times(x, x);
		double ysquared = times(y, y);
		return plus(xsquared, ysquared);		
	}
	
	public double times(double a, double b) {
		if(withStacktraces)
			logger.debug("stack", new Exception("stack"));
		return a * b;
	}

	public double plus(double a, double b) {
		return a + b;
	}

	public double sqrt(double a) {
		return Math.sqrt(a);
	}

	// ------------- fiber on same thread --------------------
	
	
	public static Fiber<Void> fiber(SuspendableRunnable runnable) {
		Fiber<Void> fiber = new Fiber<>(myFiberScheduler, 
				() -> {runnable.run(); return null;});
		fiber.start();
		return fiber;
	}
	
}
