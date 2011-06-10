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
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		controller.addTraceElement(strace, where, when);
		return param;
	}
	
}
