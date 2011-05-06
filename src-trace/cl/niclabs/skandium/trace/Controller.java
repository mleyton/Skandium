package cl.niclabs.skandium.trace;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

class Controller {

	private Skandium skandium;
	private AbstractSkeleton<?,?> skeleton;
	private SkeletonListener listener;
	private VisualHandler handler;
	private boolean tracing;
	
	Controller(Skandium skandium, AbstractSkeleton<?, ?> skeleton) {
		super();
		this.skandium = skandium;
		this.skeleton = skeleton;
		this.tracing = false;
		this.handler = new VisualHandler();
		this.listener = new SkeletonListener(handler);
	}
	
	boolean startTrace() {
		if (!tracing) {			
			skandium.addListener(new MaxThreadPoolListener(handler));
			tracing = skeleton.addListener(listener, Skeleton.class, null, null);
		}
		return tracing;
	}
	
	boolean stopTrace() {
		if (tracing) {
			tracing = !skeleton.removeListener(listener, Skeleton.class, null, null);
		}
		return !tracing;
	}
	
	boolean open() {
		// TODO
		return false;
	}
	
	boolean close() {
		// TODO
		return false;
	}
}
