/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2009 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */

package cl.niclabs.skandium.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

import com.mxgraph.model.mxCell;

class Controller {

	private AbstractSkeleton<?,?> skeleton;
	private SkeletonListener listener;
	private VisualHandler handler;
	private boolean open;
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
			open = skeleton.addGeneric(listener, Skeleton.class, null, null);
			frame = new SkelFrame(this);
			frame.initSkelFrame(skeleton);
		}
		return open;
	}
	
	boolean close() {
		if (open) {
			open = !skeleton.removeGeneric(listener, Skeleton.class, null, null);
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
				mxCell traceVert = traceVertMap.get(where);
				String hashKey = skelHashKey + where.hashCode();
				TraceElement te = new TraceElement(traceVert);
				traces.put(hashKey, te);
				frame.updateTrace(traceVert, te.getInvokes(), te.getExecTime());
			}
		}
	}
	
	void addTraceElement(@SuppressWarnings("rawtypes") Skeleton[] strace, Where where, When when) {
		String skelHashKey = new String();
		for (Skeleton<?,?> s : strace) {
			skelHashKey += s.hashCode() + ":";
		}
		String hashKey = skelHashKey + where.hashCode();
		if (!traces.containsKey(hashKey))
			return;
		TraceElement e = traces.get(hashKey);			
		if (when.equals(When.BEFORE)) {	
			e.setStartTime();
		} else {
			e.setEndTime();
		}
		frame.updateTrace(e.getTraceVert(), e.getInvokes(), e.getExecTime());
		if (strace.length == 1 && where.equals(Where.SKELETON) && when.equals(When.AFTER))
			frame.refresh();
	}
}
