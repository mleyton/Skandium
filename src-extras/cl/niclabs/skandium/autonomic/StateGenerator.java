package cl.niclabs.skandium.autonomic;

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

class StateGenerator implements SkeletonVisitor {
	
	State state;
	int index;
	
	StateGenerator(int index) {
		this.index = index;
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
//		state = new SubState(index);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
//		state = new SubState(index);
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
//		state = new State(index);
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
//		state = new SubState(index);		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
//		state = new SubState(index);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
//		state = new SubState(index);
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
//		state = new ChildrenState(index);
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
//		state = new ChildrenState(index);

	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
//		state = new ChildrenState(index);
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}
	
	State getState() {
		return state;
	}

}
