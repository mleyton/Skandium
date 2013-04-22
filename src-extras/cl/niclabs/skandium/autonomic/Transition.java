/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2013 NIC Labs, Universidad de Chile.
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

package cl.niclabs.skandium.autonomic;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class represents a transition for the state machines. A transition is 
 * comparable in order to allow the use of a priority queue during the 
 * event/transition analysis. 
 * 
 * It is an abstract class because the final classes should implement on of the
 * three possible execute methods depending of the type and number of 
 * parameters.
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 *
 */
abstract class Transition implements Comparable<Transition> {
	
	/**
	 * Transition label is the (uniquely) identification of the transition. 
	 */
	protected TransitionLabel tl;
	
	/**
	 * Destination state
	 */
	private State dest;
	
	Transition(TransitionLabel tl, State dest) {
		this.tl = tl;
		this.dest = dest;
	}

	protected void execute() {
		throw new NotImplementedException();
	}

	protected void execute(int i) {
		throw new NotImplementedException();
	}
	
	protected void execute(int i, int p) {
		throw new NotImplementedException();
	}

	State getDest() {
		return dest;
	}
	
	boolean isIn(TransitionLabel event) {
		return tl.isIn(event);
	}

	@Override
	public int compareTo(Transition t) {
		return tl.compareTo(t.tl);
	}
	
	/**
	 * Checks if the transition is related to the event
	 * @param event  Event that was raised
	 * @return if "this" transition is related to the event.
	 */
	boolean isTheOne(int eventIndex, int eventParent) {
		return tl.isTheOne(eventIndex, eventParent);
	}
	int getType() {
		return tl.getType();
	}
	void setCurrentState() {
		tl.getTs().setCurrentState(dest);
	}
}
