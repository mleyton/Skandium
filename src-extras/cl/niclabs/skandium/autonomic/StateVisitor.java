package cl.niclabs.skandium.autonomic;

interface StateVisitor {
	void visit(State state);
	void visit(PipeState state);
	void visit(SubState state);
	void visit(IfState state);
	void visit(SplitState state);
	void visit(ChildrenState state);
}
