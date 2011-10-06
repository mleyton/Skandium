package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class MergeMuscli extends AbstractMuscli {

	MergeMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m) {
		super(s, m);
	}

	@Override
	public int ub() {
		return 0;
	}

	@Override
	public int lb() {
		return 0;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
	}

	@Override
	public Muscli copy() {
		return new MergeMuscli(copy(s), m);
	}

}
