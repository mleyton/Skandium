package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class ForkSMuscli extends AbstractMuscli {
	
	private Stack<Muscli>[] subms;
	private Merge<?,?> merge;

	ForkSMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli>[] subms, Merge<?,?> merge) {
		super(s, m);
		this.subms = subms;
		this.merge = merge;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		stack.push(new MergeMuscli(s, merge));
		for (int i=0; i<subms.length; i++) {
			Stack<Muscli> subStack = copy(subms[i]);
			setNewIds(subStack,s.length,i);
			children.add(subStack);
		}	
	}

	@Override
	public int ub() {
		int r = 1;
		for (int i=0; i<subms.length; i++) {
			r += subms[i].size() + ub(subms[i]);
		}
		return r;
	}

	@Override
	public int lb() {
		int r = 1;
		for (int i=0; i<subms.length; i++) {
			r += subms[i].size() + lb(subms[i]);
		}
		return r;
	}

	@Override
	public Muscli copy() {
		return new ForkSMuscli(copy(s), m, copy(subms), merge);
	}

}
