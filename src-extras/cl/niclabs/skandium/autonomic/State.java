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

import java.util.ArrayList;
import java.util.List;


class State {
	
 	private List<Transition> transitions;
	private boolean isPersistent;
	private StateType type;
	
	State(StateType type) {
		transitions = new ArrayList<Transition>();
		this.isPersistent = false;
		this.type = type;
	}
	State(StateType type, boolean isPersistent) {
		this(type);
		this.isPersistent = isPersistent;
	}
	void addTransition(Transition t) {
		transitions.add(t);
	}
	List<Transition> getTransitions(TransitionLabel event) {
		List<Transition> areIn = new ArrayList<Transition>();
		for (Transition t : transitions) 
			if (t.isIn(event)) areIn.add(t);
		return areIn;
	}
	List<Transition> getTransitions() {
		return transitions;
	}
	void remove(List<State> l, Transition t) {
		if (!isPersistent) {
			l.remove(this);
		} else {
			transitions.remove(t);
			if (transitions.isEmpty()) {
				l.remove(this);
			}
		}
	}
	StateType getType() {
		return type;
	}
}
