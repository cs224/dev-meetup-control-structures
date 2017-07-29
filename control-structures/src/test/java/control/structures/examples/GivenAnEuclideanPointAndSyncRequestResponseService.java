package control.structures.examples;

import static org.junit.Assert.*;
import static control.structures.examples.PythagorasUsingServiceCalls.calculateDistanceFromOriginFromServiceInput;
import static org.hamcrest.number.IsCloseTo.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;
import control.structures.examples.helper.SyncRequestResponseService;

public class GivenAnEuclideanPointAndSyncRequestResponseService {

	static final Logger logger = LoggerFactory.getLogger(GivenAnEuclideanPointAndSyncRequestResponseService.class);

	protected static final SyncRequestResponseService service = new SyncRequestResponseService();
	protected static final double expected_result = 5.0;
	
	@Test
	public void whenUsingTheServiceTheEuclideanDistanceIsReturned() throws SuspendExecution {
		double result = calculateDistanceFromOriginFromServiceInput(service);		
		assertThat("The distance of the point (3.0, 4.0) from the origin should be 5.0.", result, closeTo(expected_result, 0.01));
	}
	
}
