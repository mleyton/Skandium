package cl.niclabs.skandium.autonomic;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


abstract class Transition {
	
	protected TransitionLabel tl;
	
	Transition(TransitionLabel tl) {
		this.tl = tl;
	}

	protected State execute() {
		throw new NotImplementedException();
	}

	protected State execute(int i) {
		throw new NotImplementedException();
	}

}
