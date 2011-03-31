package cl.niclabs.skandium.events;

public class SkandiumEventRegistry {
	
	int maxThreadPoolSize;
	
	public SkandiumEventRegistry(int maxThreadPoolSize) {
		this.maxThreadPoolSize = maxThreadPoolSize;
	}
	
	public boolean addListener(MaxThreadPoolListener l) {
		if(l.guard(maxThreadPoolSize)) {
			l.handler(maxThreadPoolSize);
		}
		return true;
	}
	
}
