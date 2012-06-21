package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class SubState extends State {
	
	SubState(Skeleton<?, ?> skel, int index) {
		super(skel, index);
	}

	private State subState;
	
	State getSubState() {
		return subState;
	}

	void setSubState(State subState) {
		this.subState = subState;
	}
	
}
