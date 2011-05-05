package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;

interface EventHandler {
	void handler(int maxThreadPoolSize);
	
	@SuppressWarnings("rawtypes")
	Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params);

}
