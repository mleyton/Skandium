package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.system.events.ComparableEventListener;

class MaxThreadPoolListener implements
		cl.niclabs.skandium.events.MaxThreadPoolListener {

	private EventHandler handler;
	
	public MaxThreadPoolListener(EventHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public int compareTo(ComparableEventListener arg0) {
		return 0;
	}

	@Override
	public boolean guard(int maxThreadPoolSize) {
		return true;
	}

	@Override
	public void handler(int maxThreadPoolSize) {
		handler.handler(maxThreadPoolSize);
	}

}
