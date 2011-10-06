package cl.niclabs.skandium.progress;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Muscle;
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
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class MuscliStackBuilder implements SkeletonVisitor {
	
	private Stack<Muscli> stack;
	private Stack<SkeletonTraceElement> strace;
	private HashMap<Muscle<?,?>,Estimator> estimators;
	
	public MuscliStackBuilder(HashMap<Muscle<?,?>,Estimator> estimators) {
		stack = new Stack<Muscli>();
		strace = new Stack<SkeletonTraceElement>();
		this.estimators = estimators;
	}
	
	public MuscliStackBuilder(HashMap<Muscle<?,?>,Estimator> estimators, Stack<SkeletonTraceElement> trace) {
		this(estimators);
		strace.addAll(trace);
	}
	
	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		skeleton.getSubskel().accept(this);

	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		MuscliStackBuilder m1 = new MuscliStackBuilder(estimators,strace);
		MuscliStackBuilder m2 = new MuscliStackBuilder(estimators,strace);
		skeleton.getStage1().accept(m1);
		skeleton.getStage2().accept(m2);
		stack.addAll(m2.stack);
		stack.addAll(m1.stack);
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		stack.add(new SeqMuscli(getStraceAsArray(),skeleton.getExecute()));

	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		MuscliStackBuilder mtrue = new MuscliStackBuilder(estimators,strace);
		MuscliStackBuilder mfalse = new MuscliStackBuilder(estimators,strace);
		skeleton.getTrueCase().accept(mtrue);
		skeleton.getFalseCase().accept(mfalse);
		stack.add(new IfCMuscli(getStraceAsArray(),skeleton.getCondition(),mtrue.stack,mfalse.stack));
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		MuscliStackBuilder subm = new MuscliStackBuilder(estimators,strace);
		skeleton.getSubskel().accept(subm);
		stack.add(new WhileCMuscli(getStraceAsArray(),skeleton.getCondition(),subm.stack,(WhileCondEstimator)estimators.get(skeleton.getCondition())));
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		MuscliStackBuilder subm = new MuscliStackBuilder(estimators,strace);
		skeleton.getSubskel().accept(subm);
		for (int i=0; i<skeleton.getTimes(); i++) {
			stack.addAll(subm.stack);
		}
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		MuscliStackBuilder subm = new MuscliStackBuilder(estimators,strace);
		skeleton.getSkeleton().accept(subm);
		stack.add(new MapSMuscli(getStraceAsArray(),skeleton.getSplit(),subm.stack,skeleton.getMerge(),(MapSplitEstimator) estimators.get(skeleton.getSplit())));
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));
		
		@SuppressWarnings("unchecked")
		Stack<Muscli>[] subms = new Stack[skeleton.getSkeletons().length];
		for (int i=0; i<skeleton.getSkeletons().length; i++) {
			MuscliStackBuilder subm = new MuscliStackBuilder(estimators,strace);
			skeleton.getSkeletons()[i].accept(subm);
			subms[i] = subm.stack;
		}
		stack.add(new ForkSMuscli(getStraceAsArray(),skeleton.getSplit(),subms,skeleton.getMerge()));
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		Stack<Integer> rbranch = new Stack<Integer>();
		int id = Arrays.deepHashCode(rbranch.toArray(new Integer[rbranch.size()]));
		strace.add(new SkeletonTraceElement(skeleton,id));
		MuscliStackBuilder subm = new MuscliStackBuilder(estimators,strace);
		skeleton.getSkeleton().accept(subm);
		stack.add(new DaCCMuscli(getStraceAsArray(),skeleton.getCondition(),subm.stack,skeleton.getSplit(),skeleton.getMerge(), new Stack<Integer>(), (DaCCondEstimator)estimators.get(skeleton.getCondition()),(DaCSplitEstimator)estimators.get(skeleton.getSplit())));
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}
	
	private SkeletonTraceElement[] getStraceAsArray() {
		return strace.toArray(new SkeletonTraceElement[strace.size()]);
	}

	Stack<Muscli> getStack() {
		return stack;
	}
}
