package cl.niclabs.skandium.trace;


import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Visualizer {
	
	private Skandium skandium;
	private AbstractSkeleton<?,?> skeleton;
	private boolean running;
	
	public Visualizer(Skandium skandium, Skeleton<?, ?> skeleton) {
		super();
		this.skandium = skandium;
		this.skeleton = (AbstractSkeleton<?,?>) skeleton;
		this.running = false;
	}

	public boolean open() {
		if (!running) {			
			running = true;
		}
		return running;
	}
	
	public boolean stop() {
		if (running) {
			running = false;
		}
		return !running;
	}
}
