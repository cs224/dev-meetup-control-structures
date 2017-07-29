package control.structures.examples;

import static org.junit.Assert.*;
import static org.hamcrest.number.IsCloseTo.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class GivenAnEuclideanPoint {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GivenAnEuclideanPoint.class);
	
	public static interface IPythagoras {
		public double pythagoras(double x, double y);
	}

	static Pythagoras pythagoras = new Pythagoras();
	static double x = 3.0;
	static double y = 4.0;
	static double expected_result = 5.0;
	
	protected final IPythagoras ipythagoras;
	
	public GivenAnEuclideanPoint(IPythagoras ipythagoras) {
		this.ipythagoras = ipythagoras;
	}

	@Test
	public void whenCalculatingThePythagorasThenTheResultIsTheDistanceFromTheOrigin() {
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", this.ipythagoras.pythagoras(x, y), closeTo(expected_result, 0.01));
	}

    @Parameterized.Parameters
    public static Collection<Object[]> instancesToTest() {
        return Arrays.asList(
        		new Object[][]{
        				{   (IPythagoras) (double x, double y) -> pythagoras.pythagoras_function_call_with_local_variables(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_function_calls_per_level(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_function_calls_per_level_by_need(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_continuation_passing_style(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_dataflow(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_communicating_sequential_processes_style(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_event_driven_observer_pattern_style(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_reactive_extensions_style(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_reactive_extensions_blocking_observable_style_1(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_reactive_extensions_blocking_observable_style_2(x,y) }
        				, { (IPythagoras) (double x, double y) -> pythagoras.pythagoras_reactive_quasar_style(x,y) }
        			}
        		);
    }
	
}
