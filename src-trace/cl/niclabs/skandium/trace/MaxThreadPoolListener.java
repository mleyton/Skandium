package cl.niclabs.skandium.trace;

class MaxThreadPoolListener extends
		cl.niclabs.skandium.events.MaxThreadPoolListener {

	private EventHandler handler;
	
	public MaxThreadPoolListener(EventHandler handler) {
		super();
		this.handler = handler;
	}

	@Override
	public void handler(int maxThreadPoolSize) {
		handler.handler(maxThreadPoolSize);
	}

}
