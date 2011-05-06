package cl.niclabs.skandium.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;


class VisualHandler implements EventHandler{
	
	private Map<Long,List<TraceElement>> threadTraces;
	private int maxThreadPoolSize;
	
	VisualHandler() {
		super();
		threadTraces = Collections.synchronizedMap(new HashMap<Long,List<TraceElement>>());
	}
	
	@Override
	public void handler(int maxThreadPoolSize) {
		this.maxThreadPoolSize = maxThreadPoolSize; 
		threadTraces = Collections.synchronizedMap(new HashMap<Long,List<TraceElement>>());
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public Object handler(Object param, Skeleton[] strace, When when,
			Where where, Object... params) {
		long threadId = Thread.currentThread().getId();
		TraceElement e = new TraceElement(param, strace, when, where, params);
		List<TraceElement> threadTrace;
		if (threadTraces.containsKey(threadId)) {
			threadTrace = threadTraces.get(threadId);
		} else {
			threadTrace = Collections.synchronizedList(new ArrayList<TraceElement>());
			threadTraces.put(threadId, threadTrace);
		}
		threadTrace.add(e);
		return param;
	}

	Map<Long, List<TraceElement>> getThreadTraces() {
		return threadTraces;
	}

	int getMaxThreadPoolSize() {
		return maxThreadPoolSize;
	}
	
	
}
