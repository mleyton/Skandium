package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;


class VisualHandler implements EventHandler{

	Controller controller;
	
	VisualHandler(Controller controller) {
		super();
		this.controller = controller;
	}
	
	@Override
	public void handler(int maxThreadPoolSize) {
		controller.setMaxThreadPoolSize(maxThreadPoolSize); 
		
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		long threadId = Thread.currentThread().getId();
		TraceElement e = new TraceElement(param, strace, when, where, params);
		controller.addTraceElement(threadId, e);
		return param;
	}
	
}
