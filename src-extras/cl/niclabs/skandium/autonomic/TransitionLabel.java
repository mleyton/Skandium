package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;

class TransitionLabel implements Comparable<TransitionLabel> {
	private TransitionSkelIndex ts;
	private When when;
	private Where where;
	private boolean cond;
	TransitionLabel(TransitionSkelIndex ts, When when, Where where, boolean cond) {
		this.ts = ts;
		this.when = when;
		this.where = where;
		this.cond = cond;
	}
	boolean isIn(TransitionLabel event) {
		if (!stackComparison(event.ts.getStrace())) return false;
		if (!when.equals(event.when)) return false;
		if (!where.equals(event.where)) return false;
		if (cond != event.cond) return false;
		return true;
	}
	private boolean stackComparison(@SuppressWarnings("rawtypes") Skeleton[] s) {
		if (ts.getStrace().length != s.length) return false;
		for (int i=0; i<s.length; i++) {
			@SuppressWarnings("rawtypes")
			Skeleton s1 = ts.getStrace()[i];
			@SuppressWarnings("rawtypes")
			Skeleton s2 = s[i];
			if (!s1.equals(s2)) return false;
		}
		return true;
	}
	@Override
	public int compareTo(TransitionLabel tl) {
		Integer i1 = new Integer(ts.getIndex());
		Integer i2 = new Integer(tl.ts.getIndex());
		return i1.compareTo(i2);
	}	
	
	//TODO Borrar despues
	TransitionSkelIndex getTs() {
		return ts;
	}
	When getWhen() {
		return when;
	}
	Where getWhere() {
		return where;
	}
	boolean isCond() {
		return cond;
	}

}
