package control.structures.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;

import static control.structures.examples.PythagorasUsingServiceCalls.*;

public class PythagorasUsingConsoleInputApp {

	private static final Logger logger = LoggerFactory.getLogger(PythagorasUsingConsoleInputApp.class);
	
	public static void main(String[] args) throws SuspendExecution {
		ISyncRequestResponseService service = PythagorasUsingServiceCalls::doubleFromUserInput;
		logger.info("Result: " + calculateDistanceFromOriginFromServiceInput(service));
	}

}
