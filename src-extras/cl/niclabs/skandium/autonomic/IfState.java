package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class IfState extends State {

	IfState(Skeleton<?, ?> skel, int index) {
		super(skel, index);
	}

	private boolean isConditionFinished;
	private boolean cond;
	private State subState;
	State getSubState() {
		return subState;
	}
	boolean isConditionFinished() {
		return isConditionFinished;
	}
	boolean getCond() {
		return cond;
	}
	void setConditionFinished(boolean isConditionFinished) {
		this.isConditionFinished = isConditionFinished;
	}
	void setCond(boolean cond) {
		this.cond = cond;
	}
	void setSubState(State trueState) {
		this.subState = trueState;
	}
	
}
