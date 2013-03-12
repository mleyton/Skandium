package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.List;


class State {
	private List<Transition> transitions;
	State() {
		transitions = new ArrayList<Transition>();
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
}
