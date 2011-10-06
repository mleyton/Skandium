package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class IfCMuscli extends AbstractMuscli {
	
	private Stack<Muscli> mtrue;
	private Stack<Muscli> mfalse;
	private boolean cond;

	IfCMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli> mtrue, Stack<Muscli> mfalse) {
		super(s, m);
		this.mtrue = mtrue;
		this.mfalse = mfalse;
	}
	
	public void setCond(boolean cond) {
		this.cond = cond;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		if (cond) {
			copyIds(mtrue);
			stack.addAll(mtrue);
		} else {
			copyIds(mfalse);
			stack.addAll(mfalse);
		}
	}

	@Override
	public int ub() {
		int btrue = mtrue.size() + ub(mtrue);
		int bfalse = mfalse.size() + ub(mfalse);
		return Math.max(btrue, bfalse);
	}

	@Override
	public int lb() {
		int btrue = mtrue.size() + lb(mtrue);
		int bfalse = mfalse.size() + lb(mfalse);
		return Math.min(btrue, bfalse);
	}

	@Override
	public Muscli copy() {
		IfCMuscli newMuscli = new IfCMuscli(copy(s), m, copy(mtrue), copy(mfalse));
		newMuscli.setCond(cond);
		return newMuscli;
	}

}
