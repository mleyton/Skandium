package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Skeleton;

class TransitionLabel implements Comparable<TransitionLabel> {
	static final int VOID = 0;
	static final int INDEX = 1;
	static final int FS_CARD = 2;
	
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
	boolean isTheOne(int eventIndex, int eventParent) {
		if(ts.getParent() != TransitionSkelIndex.UDEF && ts.getParent() != eventParent)
			return false; 
		if(ts.getIndex() == TransitionSkelIndex.UDEF) return true;
		if(eventIndex == ts.getIndex()) return true;		
		return false;
	}
	
	int getType() {
		if (when == When.AFTER && where == Where.SPLIT) return FS_CARD;
		if (when == When.BEFORE) {
			if (where == Where.MERGE ) return VOID;
			{
				Skeleton<?,?> current = ts.getStrace()[ts.getStrace().length-1];
				if (current instanceof DaC && where == Where.SPLIT) return VOID;
			}
			return INDEX;
		}
		return VOID;
	}
	TransitionSkelIndex getTs() {
		return ts;
	}
}
