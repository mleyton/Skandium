package cl.niclabs.skandium.visualizer;


import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Visualizer {
	
	private Skandium skandium;
	private AbstractSkeleton<?,?> skeleton;
	private EventHandler handler;
	private boolean running;
	private SkeletonListener listener;
	
	public Visualizer(Skandium skandium, AbstractSkeleton<?, ?> skeleton) {
		super();
		this.skandium = skandium;
		this.skeleton = skeleton;
		this.handler = new EventHandler();
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
	
	public void open() {
		// TODO open
	}

	public void close() {
		// TODO close
	}
}
