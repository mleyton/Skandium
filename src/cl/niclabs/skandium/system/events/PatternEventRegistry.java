/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2011 NIC Labs, Universidad de Chile.
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
package cl.niclabs.skandium.system.events;

import java.util.Hashtable;
import java.util.concurrent.PriorityBlockingQueue;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;


public class PatternEventRegistry {
	private Hashtable<Integer,PriorityBlockingQueue<ComparableEventListener>> listeners;

	
	public PatternEventRegistry() {
		listeners = new Hashtable<Integer,PriorityBlockingQueue<ComparableEventListener>>(Where.values().length * When.values().length);
	}

	public boolean addListener(NonGenericListener e) throws BadListenerException {
		return addListener(getWhen(e), getWhere(e), e);
	}
	
	public boolean addListener(When when, Where where, ComparableEventListener e) {
		int hashCode = getHashCode(when, where);
		PriorityBlockingQueue<ComparableEventListener> q;
		if (!listeners.containsKey(hashCode)) {
			q = new PriorityBlockingQueue<ComparableEventListener>();
			listeners.put(hashCode, q);
		} else {
			q = listeners.get(hashCode);
		}
		return q.add(e);
	}
	
	public boolean removeListener(NonGenericListener e) throws BadListenerException {
		return removeListener(getWhen(e), getWhere(e),e);
	}

	public boolean removeListener(When when, Where where, ComparableEventListener e) {
		return listeners.get(getHashCode(when, where)).remove(e);
	}

	public ComparableEventListener[] getListeners(When when, Where where) {
		int hashCode = getHashCode(when, where);
		if (!listeners.containsKey(hashCode)) {
			return new ComparableEventListener[0];
		} 
		PriorityBlockingQueue<ComparableEventListener> q = listeners.get(hashCode);
		return q.toArray(new ComparableEventListener[q.size()]);
	}

	private int getHashCode(When when, Where where) {
		return where.ordinal() + Where.values().length * when.ordinal();
	}
	
	private Where getWhere(ComparableEventListener e) throws BadListenerException {
		if (e instanceof SkeletonListener) return Where.SKELETON;
		if (e instanceof NestedSkelListener) return Where.NESTED_SKELETON;
		if (e instanceof ConditionListener) return Where.CONDITION;
		if (e instanceof SplitListener) return Where.SPLIT;
		if (e instanceof MergeListener) return Where.MERGE;
		throw new BadListenerException();
	}

	private When getWhen(ComparableEventListener e) throws BadListenerException {
		if (e instanceof BeforeListener) return When.BEFORE;
		if (e instanceof AfterListener) return When.AFTER;
		throw new BadListenerException();
	}
	
}
