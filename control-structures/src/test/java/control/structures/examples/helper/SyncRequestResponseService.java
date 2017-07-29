package control.structures.examples.helper;

import control.structures.examples.PythagorasUsingServiceCalls.ISyncRequestResponseService;

public class SyncRequestResponseService implements ISyncRequestResponseService {

	@Override
	public double request(String someParameter) {
		if("x".equals(someParameter)) {
			return 3.0;
		} else if("y".equals(someParameter)) {
			return 4.0;
		} else {
			throw new RuntimeException("Don't know parameter: '" + someParameter + "'");
		}
	}

}
