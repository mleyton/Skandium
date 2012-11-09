package cl.niclabs.skandium.autonomic;

import java.util.HashSet;
import java.util.Set;

class Activity {
	private final static int UNDEFINED = -1;
	private static Activity initial;
	private static Activity finalA;
	
	private long ed; // Estimated duration
	private int e_S_; // Estimated |S|
	private Activity ea; // Estimated model of subsequent activity
	
	private long es; // Earliest start
	private long lf; // Latest finish
	private Set<Activity> P; // Predecessors

	private long as; // Actual start
	private long af; // Actual finish
	private Set<Activity> S; // Actual Successors
	
	Activity() {
		ed = UNDEFINED;
		e_S_ = UNDEFINED;
		as = UNDEFINED;
		af = UNDEFINED;
		P = new HashSet<Activity>();
	}
	
	void addP(Activity a) {
		P.add(a);
	}
	
	void setEd(long ed) {
		this.ed = ed;
		reset();
	}
	
	void setE_S_(int e_S_) {
		this.e_S_ = e_S_;
		reset();
	}
	
	void setEa(Activity ea) {
		ea.addP(this);
		this.ea = ea;
	}
	
	void setAs(long as) {
		this.as = as;
		reset();
	}
	
	void setAf(long af, int a_S_) {
		this.af = af;
		S = new HashSet<Activity>();
		for (int i=0; i<a_S_; i++) {
			S.add(ea.copy());
		}
		reset();
	}
	
	private void calculateEs() {
		if (as == UNDEFINED)  {
			if (P.size()== 0) es = System.nanoTime();
			else {
				long max = Long.MIN_VALUE;
				for (Activity p : P) {
					long aef = p.getEf();
					max = aef > max ? aef : max;
				}
				es = max;
			}
		} else {
			es = as;
		}
		if (af == UNDEFINED) {
			if (ea != null) {
				ea.calculateEs();
			} else {
				finalA = this;
			}			
		} else {
			if (S.size()==0) finalA = this;
			else {
				for (Activity s: S) {
					s.calculateEs();
				}
			}
		}
	}

	private long getEf() {
		if (af == UNDEFINED) return es + ed;
		return af;
	}

	private long getLs() {
		if (as == UNDEFINED) return lf - ed;
		return as;
	}

	
	private void calculateLf() {
		if (af == UNDEFINED)  {
			lf = e_S_== 0 ? getEf() : ea.getLs(); 
		} else {
			lf = af;
		}
		for (Activity p: P) {
			p.calculateLf();
		}
	}

	private Activity copy() {
		Activity a = new Activity();
		a.ed = ed;
		a.e_S_ = e_S_;
		if (ea != null) a.setEa(ea);
		for (Activity p:P) {
			a.addP(p);
		}
		return a;
	}
	
	static void setInitial(Activity a) {
		initial = a;
		reset();
	}
	
	private void print() {
		System.out.println(es + "\t" + getEf() + "\t" + getLs() + "\t" + lf);
		if (ea != null) {
			ea.print();
		}
	}

	private static void reset() {
		if (initial != null) {
			initial.calculateEs();
			finalA.calculateLf();
			initial.print();
			System.out.println();
		}
	}
	
}