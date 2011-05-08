package cl.niclabs.skandium.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

class Controller {

	private Skandium skandium;
	private AbstractSkeleton<?,?> skeleton;
	private SkeletonListener listener;
	private VisualHandler handler;
	private boolean tracing;
	private boolean open;
	private Map<Long,List<TraceElement>> threadTraces;
	private int maxThreadPoolSize;
	
	Controller(Skandium skandium, AbstractSkeleton<?, ?> skeleton) {
		super();
		this.skandium = skandium;
		this.skeleton = skeleton;
		this.tracing = false;
		this.handler = new VisualHandler(this);
		this.listener = new SkeletonListener(handler);
		this.open = false;
		threadTraces = Collections.synchronizedMap(new HashMap<Long,List<TraceElement>>());
	}
	
	boolean startTrace() {
		if (!tracing) {
			threadTraces = Collections.synchronizedMap(new HashMap<Long,List<TraceElement>>());
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
		if (!open) {
			open = true;
			SkelFrame frame = new SkelFrame(skeleton);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 320);
			frame.setVisible(true);
		}
		return open;
	}
	
	boolean close() {
		if (open) {
			open = false;
			// TODO
		}
		return !open;
	}

	void setMaxThreadPoolSize(int maxThreadPoolSize) {
		this.maxThreadPoolSize = maxThreadPoolSize;
	}
	
	void addTraceElement(long threadId, TraceElement e) {
		List<TraceElement> threadTrace;
		if (threadTraces.containsKey(threadId)) {
			threadTrace = threadTraces.get(threadId);
		} else {
			threadTrace = Collections.synchronizedList(new ArrayList<TraceElement>());
			threadTraces.put(threadId, threadTrace);
		}
		threadTrace.add(e);		
	}
	
}
