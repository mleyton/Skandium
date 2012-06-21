package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class PipeState extends State {
	
	PipeState(Skeleton<?, ?> skel, int index) {
		super(skel, index);
	}

	private boolean isStage1Finished;
	private State stage1State;
	private State stage2State;
	State getStage1State() {
		return stage1State;
	}
	State getStage2State() {
		return stage2State;
	}
	boolean isStage1Finished() {
		return isStage1Finished;
	}
	void setStage1Finished(boolean isStage1Finished) {
		this.isStage1Finished = isStage1Finished;
	}
	void setStage1State(State stage1State) {
		this.stage1State = stage1State;
	}
	void setStage2State(State stage2State) {
		this.stage2State = stage2State;
	}
	
	
}
