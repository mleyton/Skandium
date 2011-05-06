package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;

class TraceElement {
	
	Object param; 
	@SuppressWarnings("rawtypes")
	Skeleton[] strace; 
	When when;
	Where where; 
	Object[] params;
	
	@SuppressWarnings("rawtypes")
	TraceElement(Object param, Skeleton[] strace, When when,
			Where where, Object[] params) {
		super();
		this.param = param;
		this.strace = strace;
		this.when = when;
		this.where = where;
		this.params = params;
	}

	Object getParam() {
		return param;
	}

	@SuppressWarnings("rawtypes")
	Skeleton[] getStrace() {
		return strace;
	}

	When getWhen() {
		return when;
	}

	Where getWhere() {
		return where;
	}

	Object[] getParams() {
		return params;
	}

}
