package cl.niclabs.skandium.autonomic;

import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
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

class TransitionGenerator implements SkeletonVisitor {

	private Transition initialTrans;
	private State lastState;
	@SuppressWarnings("rawtypes")
	private Stack<Skeleton> strace;

	@SuppressWarnings("rawtypes")
	TransitionGenerator() {
		this.strace = new Stack<Skeleton>();
	}

	private TransitionGenerator(
			@SuppressWarnings("rawtypes") Stack<Skeleton> trace) {
		this();
		this.strace.addAll(trace);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		strace.add(skeleton);
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		strace.add(skeleton);
		TransitionGenerator stage2 = new TransitionGenerator(strace);
		skeleton.getStage2().accept(stage2);
		TransitionGenerator stage1 = new TransitionGenerator(strace);
		skeleton.getStage1().accept(stage1);
		stage1.getLastState().addTransition(stage2.getInitialTrans());
		initialTrans = stage1.getInitialTrans();
		lastState = stage2.getLastState();
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);
		lastState = new State();
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.SKELETON, false),lastState) {
			@Override
			protected void execute() {
				//TODO
			}
		};
		State I = new State();
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE, Where.SKELETON, false),I) {
			@Override
			protected void execute(int i) {
				//TODO
			}
		};
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		throw new RuntimeException("No autonomic support for IF Skeleton");
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		strace.add(skeleton);
		lastState = new State();
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, false),lastState) {
			@Override
			protected void execute() {
				//TODO
			}
		};
		State I = new State();
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE, Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				//TODO
			}
		};
		State T = new State();
		Transition toT = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, true),T) {
			@Override
			protected void execute() {
				//TODO
			}
		};
		I.addTransition(toT);
		TransitionGenerator subSkel = new TransitionGenerator(strace);
		skeleton.getSubskel().accept(subSkel);
		T.addTransition(subSkel.getInitialTrans());
		Transition toI = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.CONDITION, false),I) {
			@Override
			protected void execute() {
				//TODO
			}
		};
		subSkel.getLastState().addTransition(toI);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(skeleton);
///////////////////////////////////////////////
		TransitionGenerator iniSubSkel = new TransitionGenerator(strace);
		skeleton.getSubskel().accept(iniSubSkel);
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		strace.add(skeleton);
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		strace.add(skeleton);

	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		strace.add(skeleton);
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	Transition getInitialTrans() {
		return initialTrans;
	}

	State getLastState() {
		return lastState;
	}

	@SuppressWarnings("rawtypes")
	private Skeleton[] getStraceAsArray() {
		return strace.toArray(new Skeleton[strace.size()]);
	}
}
