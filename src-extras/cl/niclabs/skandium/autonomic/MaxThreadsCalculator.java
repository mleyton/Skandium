package cl.niclabs.skandium.autonomic;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;

class MaxThreadsCalculator implements SkeletonVisitor{
	
	int threads;
	Controller controller;
	Stack<Skeleton<?,?>> parents;
	int index;
	
	MaxThreadsCalculator(Controller controller, Stack<Skeleton<?,?>> parents, int index) {
		threads = 0;
		this.controller = controller;
		this.parents = parents;
		this.index = index;
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		parents.push(skeleton);
		index = controller.getState().getFarmSubIndex(parents, index);
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		if (controller.getState().isPipeStage1Finished(strace, index)) {
			parents.push(skeleton);
			index = controller.getState().getPipeStage2Index(parents, index);
			skeleton.getStage2().accept(this);
			return;
		}
		MaxThreadsCalculator cstage1 = new MaxThreadsCalculator(controller, strace,
				controller.getState().getPipeStage1Index(strace, index));
		skeleton.getStage1().accept(cstage1);
		MaxThreadsCalculator cstage2 = new MaxThreadsCalculator(controller, strace,
				controller.getState().getPipeStage2Index(strace, index));
		skeleton.getStage1().accept(cstage2);
		threads = Math.max(cstage1.getMaxThreads(), cstage2.getMaxThreads());
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		threads = 1;
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		int trueIndex = controller.getState().getIfTrueIndex(strace, index);
		int falseIndex = controller.getState().getIfFalseIndex(strace, index);
		if (controller.getState().isIfConditionFinished(strace, index)) {	
			parents.push(skeleton);
			if (controller.getState().ifCondition(strace, index)) {
				index = trueIndex;
				skeleton.getTrueCase().accept(this);
			} else {
				index = falseIndex;
				skeleton.getFalseCase().accept(this);
			}
			return;
		}
		MaxThreadsCalculator ctrueCase = new MaxThreadsCalculator(controller, strace, trueIndex);
		skeleton.getTrueCase().accept(ctrueCase);
		MaxThreadsCalculator cfalseCase = new MaxThreadsCalculator(controller, strace, falseIndex);
		skeleton.getFalseCase().accept(cfalseCase);
		threads = Math.max(ctrueCase.getMaxThreads(), cfalseCase.getMaxThreads());
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace,index)) return;
		parents.push(skeleton);
		index = controller.getState().getWhileSubIndex(parents, index);
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace,index)) return;
		parents.push(skeleton);
		index = controller.getState().getForSubIndex(parents, index);
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		if (controller.getState().isBeforeMerge(strace, index)) {
			threads = 1;
			return;
		}
		if (controller.getState().isBeforeSplit(strace, index)) {
			threads = 1;
			return;
		}		
		List<Integer> subIndexes = controller.getState().getSubIndexes(strace,index);
		for (int i=0; i<subIndexes.size(); i++) {
			int subIndex = subIndexes.get(i);
			MaxThreadsCalculator csub = new MaxThreadsCalculator(controller, strace, subIndex);
			skeleton.getSkeleton().accept(csub);
			threads += csub.getMaxThreads();
		}
		
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		if (controller.getState().isBeforeMerge(strace, index)) {
			threads = 1;
			return;
		}
		if (controller.getState().isBeforeSplit(strace, index)) {
			threads = 1;
			return;
		}		
		List<Integer> subIndexes = controller.getState().getSubIndexes(strace,index);
		for (int i=0; i<subIndexes.size(); i++) {
			int subIndex = subIndexes.get(i);
			MaxThreadsCalculator csub = new MaxThreadsCalculator(controller, strace, subIndex);
			skeleton.getSkeletons()[i].accept(csub);
			threads += csub.getMaxThreads();
		}		
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		Stack<Skeleton<?,?>> strace = copyStack(parents);
		strace.push(skeleton);
		if (controller.getState().isFinished(strace, index)) return;
		List<Skeleton<?,?>> children = controller.getState().getDaCChildren(strace,index);
		List<Integer> subIndexes = controller.getState().getSubIndexes(strace,index);
		for (int i=0; i<subIndexes.size(); i++) {
			Skeleton<?,?> child = children.get(i);
			if (child instanceof DaC<?,?>) {
				threads += 1;
			} else {
				int subIndex = subIndexes.get(i);
				MaxThreadsCalculator csub = new MaxThreadsCalculator(controller, strace, subIndex);
				skeleton.getSkeleton().accept(csub);
				threads += csub.getMaxThreads();
			}
		}		
	}
	
	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	private Stack<Skeleton<?,?>> copyStack(Stack<Skeleton<?,?>> stack) {
		Stack<Skeleton<?,?>> newStack = new Stack<Skeleton<?,?>>();
		for (Skeleton<?,?> skel : stack) {
			newStack.add(skel);
		}
		return newStack;
	}
	
	int getMaxThreads() {
		return threads;
	}

}
