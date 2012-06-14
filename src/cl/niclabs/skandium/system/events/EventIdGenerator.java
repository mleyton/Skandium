package cl.niclabs.skandium.system.events;

import java.util.concurrent.atomic.AtomicInteger;

public class EventIdGenerator {
	private static final EventIdGenerator singleton = new EventIdGenerator();
	
	private AtomicInteger counter;
	
	private EventIdGenerator() {
		counter = new AtomicInteger();
	}
	
	public static EventIdGenerator getSingleton() {
		return singleton;
	}
	
	public int increment() {
		return counter.incrementAndGet();
	}
}
