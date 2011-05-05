package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;


class PlainHandler implements EventHandler{
	
	PlainHandler() {
		super();
	}
	
	@Override
	public void handler(int maxThreadPoolSize) {
		System.out.println(System.currentTimeMillis() + " maxThreadPoolSize:" + maxThreadPoolSize);
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		String out = System.currentTimeMillis() + " skeletonTrace:";
		for (int i=0; i<strace.length; i++) {
			out += "/" + strace[i].getClass().getSimpleName();
		}
		out += " when:" + when + " where:" + where + " params:" + params; 
		System.out.println(out);
		return param;
	}
}
