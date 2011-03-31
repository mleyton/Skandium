package cl.niclabs.skandium.events;

public interface MaxThreadPoolListener extends EventListener {

	public boolean guard(int maxThreadPoolSize);

	public void handler(int maxThreadPoolSize);

}
