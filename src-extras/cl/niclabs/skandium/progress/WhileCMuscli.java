package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class WhileCMuscli extends AbstractMuscli {
	
	private Stack<Muscli> subm;
	private int index;
	private boolean cond;
	private Object p;
	private WhileCondEstimator estimator;
	private int estimate;
	private boolean hasEstimate;

	WhileCMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli> subm, WhileCondEstimator estimator) {
		super(s, m);
		this.subm = subm;
		this.estimator = estimator;
		hasEstimate = false;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setCond(boolean cond) {
		this.cond = cond;
	}

	public void setP(Object p) {
		this.p = p;
	}
	
	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		estimate = estimator.estimate(p, index);
		hasEstimate = true;
		if (cond) {
			stack.push(this);
			Stack<Muscli> copySubm = copy(subm);
			copyIds(copySubm);
			stack.addAll(copySubm);
			estimate--;
		} 
	}

	@Override
	public int ub() {
		if (!hasEstimate) {
			 estimate = estimator.estimate(null, -1);
			 hasEstimate = true;
		}
		int fce = estimate;
		return fce * (1 + subm.size() + ub(subm));
	}

	@Override
	public int lb() {
		if (!hasEstimate) {
			 estimate = estimator.estimate(null, -1);
			 hasEstimate = true;
		}
		int fce = estimate;
		return fce * (1 + subm.size() + ub(subm));
	}

	@Override
	public Muscli copy() {
		WhileCMuscli newMuscli = new WhileCMuscli(copy(s), m, copy(subm), estimator);
		newMuscli.setIndex(index);
		newMuscli.setCond(cond);
		newMuscli.setP(p);
		newMuscli.estimate = this.estimate;
		newMuscli.hasEstimate = this.hasEstimate;
		return newMuscli;
	}

}
