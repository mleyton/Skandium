package cl.niclabs.skandium.progress;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class DaCSMuscli extends AbstractMuscli {
	
	private Stack<Muscli> subm;
	private Condition<?> condition;
	private Merge<?,?> merge;
	private Stack<Integer> rbranch;
	private Object[] p;
	DaCCondEstimator cestimator;
	int cestimate;
	boolean chasEstimate;
	DaCSplitEstimator sestimator;
	int sestimate;
	boolean shasEstimate;

	DaCSMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli> subm, Condition<?> condition, Merge<?,?> merge, Stack<Integer> rbranch, DaCCondEstimator cestimator, DaCSplitEstimator sestimator) {
		super(s, m);
		this.subm = subm;
		this.condition = condition;
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

	void setP(Object[] p) {
		this.p = p;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		stack.push(new MergeMuscli(s, merge));
		for (int i=0; i<p.length; i++) {
			SkeletonTraceElement[] t = copy(s);
			Stack<Integer> newRbranch = copyRbranch(rbranch);
			newRbranch.push(i);
			int id = Arrays.deepHashCode(newRbranch.toArray(new Integer[newRbranch.size()]));
			t[t.length-1] = new SkeletonTraceElement(t[t.length-1].getSkel(),id);
			DaCCMuscli child = new DaCCMuscli(t, condition, subm, (Split<?,?>) m, merge, newRbranch, cestimator, sestimator);
			Integer[] a = newRbranch.toArray(new Integer[newRbranch.size()]);
			child.cestimate = cestimator.estimate(p[i], a);
			child.chasEstimate = true;
			child.sestimate = sestimator.estimate(p[i], a);
			child.shasEstimate = true;
			Stack<Muscli> subStack = new Stack<Muscli>();
			subStack.add(child);
			children.add(subStack);
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
		return (int) (3 * (1-Math.pow(fse, fce))/(1-fse) + (1+subm.size() + ub(subm))*Math.pow(fse, fce) - 2);
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
		return (int) (3 * (1-Math.pow(fse, fce))/(1-fse) + (1+subm.size() + ub(subm))*Math.pow(fse, fce) - 2);
	}

	@Override
	public Muscli copy() {
		DaCSMuscli newMuscli = new DaCSMuscli(copy(s),m,copy(subm),condition,merge,copyRbranch(rbranch),cestimator,sestimator);
		newMuscli.p = p;
		newMuscli.cestimate = cestimate;
		newMuscli.chasEstimate = chasEstimate;
		newMuscli.sestimate = sestimate;
		newMuscli.shasEstimate = shasEstimate;
		return newMuscli;
	}

}
