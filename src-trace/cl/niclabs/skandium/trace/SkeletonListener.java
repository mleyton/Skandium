package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.events.ComparableEventListener;
import cl.niclabs.skandium.system.events.GenericListener;

class SkeletonListener implements GenericListener {

	private EventHandler handler;
	
	public SkeletonListener(EventHandler handler) {
		super();
		this.handler = handler;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean guard(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		return handler.handler(param, strace, when, where, params);
	}

	@Override
	public int compareTo(ComparableEventListener o) {
		return 0;
	}

}
