package cl.niclabs.skandium.progress;

import java.util.Arrays;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

abstract class AbstractMuscli implements Muscli {
	
	protected SkeletonTraceElement[] s;
	protected Muscle<?,?> m;

	AbstractMuscli(SkeletonTraceElement[] s, Muscle<?, ?> m) {
		super();
		this.s = s;
		this.m = m;
	}

	@Override
	public int hashCode() {
		return hashCode(s,m);
	}
	
	static int hashCode(SkeletonTraceElement[] s, Muscle<?, ?> m) {
		int sh = Arrays.deepHashCode(s);
		int mh = m.hashCode();
		long n = mh;
		n = n << Integer.SIZE;
		n = n + sh;
		return new Long(n).hashCode();		
	}
	
	static int ub(Stack<Muscli> s) {
		int r=0;
		for (Muscli m:s) {
			r += m.ub();
		}
		return r;
	}
	
	static int lb(Stack<Muscli> s) {
		int r=0;
		for (Muscli m:s) {
			r += m.lb();
		}
		return r;
	}
	
	static void setNewIds(Stack<Muscli> s, int l, int id) {
		for (Muscli m:s) {
			SkeletonTraceElement[] t = ((AbstractMuscli) m).s;
			t[l] = new SkeletonTraceElement(t[l].getSkel(), id);
		}
	}
	
	static Stack<Muscli> copy(Stack<Muscli> s) {
		Stack<Muscli> r = new Stack<Muscli>();
		for (int i=0; i<s.size(); i++) {
			r.add(i,s.get(i).copy());
		}
		return r;		
	}

	static Stack<Muscli>[] copy(Stack<Muscli>[] a) {
		@SuppressWarnings("unchecked")
		Stack<Muscli>[] r = new Stack[a.length];
		for (int i=0; i<a.length; i++) {
			r[i] = new Stack<Muscli>();
			r[i] = copy(a[i]);
		}
		return r;		
	}

	static SkeletonTraceElement[] copy(SkeletonTraceElement[] t) {
		return Arrays.copyOf(t, t.length);
	}
	
	static Stack<Integer> copyRbranch(Stack<Integer> t) {
		Stack<Integer> r = new Stack<Integer>();
		r.addAll(t);
		return r;
	}
	
	void copyIds(Stack<Muscli> subStack) {
		for (Muscli m:subStack) {
			SkeletonTraceElement[] s = ((AbstractMuscli) m).s;
			for (int i=0; i<this.s.length; i++) {
				s[i] = new SkeletonTraceElement(this.s[i].getSkel(), this.s[i].getId());
			}
		}		
	}
}
