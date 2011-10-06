package cl.niclabs.skandium.progress;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class MapSMuscli extends AbstractMuscli {
	
	private Stack<Muscli> subm;
	private Merge<?,?> merge;
	private int plen;
	private MapSplitEstimator estimator;
	private int estimate;
	private boolean hasEstimate;

	MapSMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m, Stack<Muscli> subm, Merge<?,?> merge, MapSplitEstimator estimator) {
		super(s, m);
		this.subm = subm;
		this.merge = merge;
		this.estimator = estimator;
		hasEstimate = false;
	}

	public void setPlen(int plen) {
		this.plen = plen;
	}

	@Override
	public void reduce(Stack<Muscli> stack, List<Stack<Muscli>> children) {
		stack.push(new MergeMuscli(s, merge));
		for (int i=0; i<plen; i++) {
			Stack<Muscli> subi = copy(subm);
			setNewIds(subi,s.length,i);
			children.add(subi);
		}
	}

	@Override
	public int ub() {
		if (!hasEstimate) {
			estimate = estimator.estimate();
			hasEstimate = true;
		}
		int fse = estimate;
		return (fse * (subm.size() + ub(subm))) + 1;
	}

	@Override
	public int lb() {
		if (!hasEstimate) {
			estimate = estimator.estimate();
			hasEstimate = true;
		}
		int fse = estimate;
		return (fse * (subm.size() + lb(subm))) + 1;
	}

	@Override
	public Muscli copy() {
		MapSMuscli newMuscli = new MapSMuscli(copy(s), m, copy(subm), merge, estimator);
		newMuscli.setPlen(plen);
		newMuscli.estimate = this.estimate;
		newMuscli.hasEstimate = this.hasEstimate;
		return newMuscli;
	}

}
