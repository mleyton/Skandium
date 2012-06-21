package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class State {
	private Skeleton<?,?> skel;
	private int index;
	private boolean isFinished;
	
	State(Skeleton<?,?> skel, int index) {
		this.skel = skel;
		this.index = index;
		isFinished = false;
	}

	Skeleton<?,?> getSkel() {
		return skel;
	}
	
	int getIndex() {
		return index;
	}

	boolean isFinished() {
		return isFinished;
	}
	
	void accept(StateVisitor visitor) {
		visitor.visit(this);
	}

	void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
}
