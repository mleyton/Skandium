package cl.niclabs.skandium.autonomic;

import java.util.Collection;

import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;

class MaxThreadsCalculator implements SkeletonVisitor{
	
	private int threads;
	private State state;
	
	MaxThreadsCalculator(State state) {
		threads = 0;
		this.state = state;
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		if (state.isFinished()) return;
		state = ((SubState) state).getSubState();
		if (state == null) {
			threads = 1;
			return;
		} 
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		if (state.isFinished()) return;
		State stage1State = ((PipeState) state).getStage1State();
		if (stage1State == null) {
			threads = 1;
			return;
		}
		State stage2State = ((PipeState) state).getStage2State();
		if (stage1State.isFinished()) {
			state = stage2State;
			if (stage2State == null) {
				threads = 1;
				return;
			}
			skeleton.getStage2().accept(this);
			return;
		}
		MaxThreadsCalculator cstage1 = new MaxThreadsCalculator(stage1State);
		skeleton.getStage1().accept(cstage1);
		MaxThreadsCalculator cstage2 = new MaxThreadsCalculator(stage2State);
		skeleton.getStage1().accept(cstage2);
		threads = Math.max(cstage1.getMaxThreads(), cstage2.getMaxThreads());
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		if (state.isFinished()) return;
		threads = 1;
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		if (state.isFinished()) return;
		State subState = ((IfState)state).getSubState();
		if (((IfState)state).isConditionFinished()) {	
			state = subState;
			if (state == null) {
				threads = 1;
				return;
			}
			if (((IfState)state).getCond()) {
				skeleton.getTrueCase().accept(this);
			} else {
				skeleton.getFalseCase().accept(this);
			}
			return;
		}
		threads = 1;
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		if (state.isFinished()) return;
		state = ((SubState)state).getSubState();
		if (state == null) {
			threads = 1;
			return;
		}
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		if (state.isFinished()) return;
		state = ((SubState)state).getSubState();
		if (state == null) {
			threads = 1;
			return;
		}
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		if (state.isFinished()) return;
		threads = 1;
		if (((SplitState)state).isBeforeMerge() || ((SplitState)state).isBeforeSplit()) {
			return;
		}
		Collection<State> subStates = ((SplitState)state).getChildren();
		for (State subState : subStates) {
			MaxThreadsCalculator csub = new MaxThreadsCalculator(subState);
			subState.getSkel().accept(csub);
			threads += csub.getMaxThreads();
		}		
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		if (state.isFinished()) return;
		threads = 1;
		if (((SplitState)state).isBeforeMerge() || ((SplitState)state).isBeforeSplit()) {
			return;
		}
		Collection<State> subStates = ((SplitState)state).getChildren();
		for (State subState : subStates) {
			MaxThreadsCalculator csub = new MaxThreadsCalculator(subState);
			subState.getSkel().accept(csub);
			threads += csub.getMaxThreads();
		}
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		if (state.isFinished()) return;
		threads = 1;
		Collection<State> subStates = ((ChildrenState)state).getChildren();
		for (State subState : subStates) {
			if (subState.getSkel() instanceof DaC<?,?>) {
				threads += 1;
			} else {
				MaxThreadsCalculator csub = new MaxThreadsCalculator(subState);
				subState.getSkel().accept(csub);
				threads += csub.getMaxThreads();
			}
		}		
	}
	
	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	int getMaxThreads() {
		return threads;
	}

}
