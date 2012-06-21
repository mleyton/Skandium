package cl.niclabs.skandium.autonomic;

class ChildSetter implements StateVisitor {

	State child;
	
	ChildSetter(State child) {
		this.child = child;
	}
	
	@Override
	public void visit(State state) {
		throw new RuntimeException("Should not be here!");
	}

	@Override
	public void visit(PipeState state) {
		if (state.getStage1State() == null) {
			state.setStage1State(child);
			return;
		}
		if (state.getStage1State().getIndex() == child.getIndex()) return;
		if (state.getStage2State() == null) {
			state.setStage2State(child);
			return;
		}
	}

	@Override
	public void visit(SubState state) {
		if (state.getSubState() == null) {
			state.setSubState(child);
		}

	}

	@Override
	public void visit(IfState state) {
		if (state.getSubState() == null) {
			state.setSubState(child);
		}
	}

	@Override
	public void visit(SplitState state) {
		if (!state.hasChild(child)) {
			state.addChildren(child);
		}
	}

	@Override
	public void visit(ChildrenState state) {
		if (!state.hasChild(child)) {
			state.addChildren(child);
		}
	}

}
