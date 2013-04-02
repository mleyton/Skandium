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
