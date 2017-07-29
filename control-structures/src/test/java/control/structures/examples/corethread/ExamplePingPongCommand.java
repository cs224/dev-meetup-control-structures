package control.structures.examples.corethread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.paralleluniverse.fibers.SuspendExecution;
import control.structures.dataflow.DataflowVariable;
import control.structures.examples.PythagorasUsingServiceCalls.IAsyncDataflowRequestResponseService1;

public class ExamplePingPongCommand implements Command {

	private static final long serialVersionUID = -849017289536535706L;

	private static final Logger logger = LoggerFactory.getLogger(ExamplePingPongCommand.class);
	
	private final IAsyncDataflowRequestResponseService1<Double> requestResponseService;
	
	public ExamplePingPongCommand(IAsyncDataflowRequestResponseService1<Double> requestResponseService) {
		this.requestResponseService = requestResponseService;
	}
	
	@Override
	public void run(DataflowVariableRepository dvr) throws SuspendExecution, InterruptedException {
		logger.debug("starting ExamplePingPongCommand");
		logger.debug("calling for result of x:");
		DataflowVariable<Double> x = dvr.createReference(() -> 0.0, "x");
		requestResponseService.request("x", x);
		Double d1 = x.get();
		logger.debug("calling for result of y:");
		DataflowVariable<Double> y = dvr.createReference(() -> 0.0, "y");
		requestResponseService.request("y", y);
		Double d2 = y.get();
		logger.debug("result x+y:" + (d1 + d2));
	}

}
