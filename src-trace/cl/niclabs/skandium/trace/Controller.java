package cl.niclabs.skandium.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mxgraph.model.mxCell;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

class Controller {

	private AbstractSkeleton<?,?> skeleton;
	private SkeletonListener listener;
	private VisualHandler handler;
	private boolean open;
	/*
	 * muscleTraces is the data structure to store the performance information
	 * It allocates an Skeleton trace and for each Where event stores tree Long values:
	 * The first one is the invoke counter, and the second one is the accumulated wallclock
	 * time execution. The third long value corresponds to the timestamp of the last invocation
	 * used as base for the accumulated time.
	 */
	private Map<String,TraceElement> traces;
	private SkelFrame frame;
	
	Controller(AbstractSkeleton<?, ?> skeleton) {
		super();
		this.skeleton = skeleton;
		this.handler = new VisualHandler(this);
		this.listener = new SkeletonListener(handler);
		this.open = false;
	}
	
	boolean open() {
		if (!open) {
			traces = Collections.synchronizedMap(new HashMap<String,TraceElement>());
			open = skeleton.addListener(listener, Skeleton.class, null, null);
			frame = new SkelFrame(this);
			frame.initSkelFrame(skeleton);
		}
		return open;
	}
	
	boolean close() {
		if (open) {
			open = !skeleton.removeListener(listener, Skeleton.class, null, null);
			frame.close();
		}
		return !open;
	}

	void initTrace(Skeleton<?,?>[] strace, Map<Where,mxCell> traceVertMap) {
		String skelHashKey = new String();
		for (Skeleton<?,?> s : strace) {
			skelHashKey += s.hashCode() + ":";
		}
		for (Where where :Where.values()) {
			if (traceVertMap.containsKey(where)) {
				long invokes = 0;
				long execTime = 0;
				mxCell traceVert = traceVertMap.get(where);
				String hashKey = skelHashKey + where.hashCode();
				System.out.println("I:" + hashKey);
				traces.put(hashKey, 
						new TraceElement(traceVert,invokes,execTime,0));
				frame.updateTrace(traceVert, invokes, execTime);
			}
		}
	}
	
	void addTraceElement(Skeleton<?,?>[] strace, Where where, When when) {
		String skelHashKey = new String();
		for (Skeleton<?,?> s : strace) {
			skelHashKey += s.hashCode() + ":";
		}
		String hashKey = skelHashKey + where.hashCode();
		System.out.println("R:" + hashKey);
		if (!traces.containsKey(hashKey))
			return;
		TraceElement e = traces.get(hashKey);			
		if (when.equals(When.BEFORE)) {	
			e.setInvokes(e.getInvokes()+1);
			e.setStartTime(System.currentTimeMillis());
		} else {
			long currTime = System.currentTimeMillis();
			e.setExecTime(e.getExecTime() + (currTime - e.getStartTime()));
			e.setStartTime(0);
		}
		frame.updateTrace(e.getTraceVert(), e.getInvokes(), e.getExecTime());
	}
	
}
