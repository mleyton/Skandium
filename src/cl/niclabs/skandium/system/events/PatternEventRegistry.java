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


/**
 * This class is used by each pattern(Skeleton) class in order to register and remove event
 * listeners
 */
public class PatternEventRegistry {
	private Hashtable<Integer,PriorityBlockingQueue<SkandiumEventListener>> listeners;
	private boolean hasListeners;

	/**
	 * The constructor
	 */
	public PatternEventRegistry() {
		listeners = new Hashtable<Integer,PriorityBlockingQueue<SkandiumEventListener>>(Where.values().length * When.values().length);
		hasListeners = false;
	}

	/**
	 * Adds an event listener
	 * @param when Defines the event {@link When} dimension, it could be {@link When#BEFORE} or {@link When#AFTER}
	 * @param where Defines the event {@link Where} dimension, it could be {@link Where#SKELETON}, {@link Where#CONDITION}, {@link Where#SPLIT}, {@link Where#NESTED_SKELETON} or {@link Where#MERGE}
	 * @param e event listener to register
	 * @return true if the listener was registered successfully, and false otherwise
	 */
	public boolean addListener(When when, Where where, SkandiumEventListener e) {
		hasListeners = true;
		int hashCode = getHashCode(when, where);
		PriorityBlockingQueue<SkandiumEventListener> q;
		if (!listeners.containsKey(hashCode)) {
			q = new PriorityBlockingQueue<SkandiumEventListener>();
			listeners.put(hashCode, q);
		} else {
			q = listeners.get(hashCode);
		}
		if (!q.contains(e)) return q.add(e);
		return true;
	}
	
	/**
	 * Removes an event listener
	 * @param when Defines the event {@link When} dimension, it could be {@link When#BEFORE} or {@link When#AFTER}
	 * @param where Defines the event {@link Where} dimension, it could be {@link Where#SKELETON}, {@link Where#CONDITION}, {@link Where#SPLIT}, {@link Where#NESTED_SKELETON} or {@link Where#MERGE}
	 * @param e event listener to remove
	 * @return true if the listener was removed successfully, and false otherwise
	 */
	public boolean removeListener(When when, Where where, SkandiumEventListener e) {
		boolean ret = listeners.get(getHashCode(when, where)).remove(e);
		if (listeners.size()==0) hasListeners = false;
		return ret;
	}

	/**
	 * Given a when and where parameters, returns the list of listeners registered.
	 * @param when Defines the event {@link When} dimension, it could be {@link When#BEFORE} or {@link When#AFTER}
	 * @param where Defines the event {@link Where} dimension, it could be {@link Where#SKELETON}, {@link Where#CONDITION}, {@link Where#SPLIT}, {@link Where#NESTED_SKELETON} or {@link Where#MERGE}
	 * @return list of listeners registered.
	 */
	public SkandiumEventListener[] getListeners(When when, Where where) {
		if (!hasListeners) {
			return new SkandiumEventListener[0];
		}
		int hashCode = getHashCode(when, where);
		if (!listeners.containsKey(hashCode)) {
			return new SkandiumEventListener[0];
		} 
		PriorityBlockingQueue<SkandiumEventListener> q = listeners.get(hashCode);
		return q.toArray(new SkandiumEventListener[q.size()]);
	}

	private int getHashCode(When when, Where where) {
		return where.ordinal() + Where.values().length * when.ordinal();
	}
		
}
