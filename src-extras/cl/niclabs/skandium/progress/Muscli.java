package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

interface Muscli {
	int ub();
	int lb();
	void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children);
	Muscli copy();
}
