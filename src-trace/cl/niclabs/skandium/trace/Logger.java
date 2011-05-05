package cl.niclabs.skandium.trace;


import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Logger {
	
	private Skandium skandium;
	private AbstractSkeleton<?,?> skeleton;
	private PlainHandler handler;
	private boolean running;
	private SkeletonListener listener;
	
	public Logger(Skandium skandium, Skeleton<?, ?> skeleton) {
		super();
		this.skandium = skandium;
		this.skeleton = (AbstractSkeleton<?,?>) skeleton;
		this.handler = new PlainHandler();
		this.running = false;
		this.listener = new SkeletonListener(handler);
	}

	public boolean start() {
		if (!running) {			
			skandium.addListener(new MaxThreadPoolListener(handler));
			running = skeleton.addListener(listener, Skeleton.class, null, null);
		}
		return running;
	}
	
	public boolean stop() {
		if (running) {
			running = !skeleton.removeListener(listener, Skeleton.class, null, null);
		}
		return !running;
	}
}
