package cl.niclabs.skandium.autonomic;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


abstract class Transition implements Comparable<Transition> {
	
	protected TransitionLabel tl;
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
	
	boolean isTheOne(int eventIndex) {
		return tl.isTheOne(eventIndex);
	}
	int getType() {
		return tl.getType();
	}
}
