package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class DaCCMuscli extends AbstractMuscli {
	
	private Stack<Muscli> subm;
	private Split<?,?> split;
	private Merge<?,?> merge;
	private Stack<Integer> rbranch;
	private boolean cond;
	private Object p;
	DaCCondEstimator cestimator;
	int cestimate;
	boolean chasEstimate;
	DaCSplitEstimator sestimator;
	int sestimate;
	boolean shasEstimate;

	DaCCMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli> subm, Split<?,?> split, Merge<?,?> merge, Stack<Integer> rbranch, DaCCondEstimator cestimator, DaCSplitEstimator sestimator) {
		super(s, m);
		this.subm = subm;
		this.split = split;
		this.merge = merge;
		this.rbranch = rbranch;
		this.cestimator = cestimator;
		this.sestimator = sestimator;
		chasEstimate = false;
		shasEstimate = false;
	}

	void setRbranch(Stack<Integer> rbranch) {
		this.rbranch = rbranch;
	}

	void setCond(boolean cond) {
		this.cond = cond;
	}
	
	void setP(Object p) {
		this.p = p;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		if (cond) {
			DaCSMuscli dacsmuscli = new DaCSMuscli(s, split, subm, (Condition<?>) m, merge, rbranch, cestimator, sestimator);
			dacsmuscli.cestimate = cestimate;
			dacsmuscli.chasEstimate = chasEstimate;
			dacsmuscli.sestimate = sestimator.estimate(p, rbranch.toArray(new Integer[rbranch.size()]));
			dacsmuscli.shasEstimate = true;
			stack.push(dacsmuscli);
		} else {
			Stack<Muscli> newSubm = copy(subm);
			copyIds(newSubm);
			stack.addAll(newSubm);
		}
	}

	@Override
	public int ub() {
		if (!chasEstimate) {
			cestimate = cestimator.estimate(null, rbranch.toArray(new Integer[rbranch.size()]));
			chasEstimate = true;
		}
		if (!shasEstimate) {
			sestimate = sestimator.estimate(null, rbranch.toArray(new Integer[rbranch.size()]));
			shasEstimate = true;
		}
		int fce = cestimate;
		int fse = sestimate;
		return (int) (3 * (1-Math.pow(fse, fce))/(1-fse) + (1+subm.size() + ub(subm))*Math.pow(fse, fce) - 1);
	}

	@Override
	public int lb() {
		if (!chasEstimate) {
			cestimate = cestimator.estimate(null, rbranch.toArray(new Integer[rbranch.size()]));
			chasEstimate = true;
		}
		if (!shasEstimate) {
			sestimate = sestimator.estimate(null, rbranch.toArray(new Integer[rbranch.size()]));
			shasEstimate = true;
		}
		int fce = cestimate;
		int fse = sestimate;
		
		return (int) (3 * (1-Math.pow(fse, fce))/(1-fse) + (1+subm.size() + lb(subm))*Math.pow(fse, fce) - 1);
	}

	@Override
	public Muscli copy() {
		DaCCMuscli newMuscli = new DaCCMuscli(copy(s), m, copy(subm), split, merge, copyRbranch(rbranch), cestimator, sestimator);
		newMuscli.cond = cond;
		newMuscli.p = p;
		newMuscli.cestimate = cestimate;
		newMuscli.chasEstimate = chasEstimate;
		newMuscli.sestimate = sestimate;
		newMuscli.shasEstimate = shasEstimate;
		return newMuscli;
	}

}
