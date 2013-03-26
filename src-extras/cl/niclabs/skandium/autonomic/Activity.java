package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.niclabs.skandium.muscles.Muscle;

class Activity {

	public static final long UDEF = -1; 
	private long ti;
	private long tf;
	private Muscle<?,?> m;
	private List<Activity> s;
	private List<Activity> p;
	private HashMap<Muscle<?,?>,Long> t;
	private double rho;
	Activity(HashMap<Muscle<?,?>,Long> t, Muscle<?,?> m, double rho) {
		this.ti = UDEF;
		this.tf = UDEF;
		this.m = m;
		this.s = new ArrayList<Activity>();
		this.p = new ArrayList<Activity>();
		this.t = t;
		this.rho = rho;
	}
	void setTi() {
		this.ti = System.nanoTime();
	}
	void setTf() {
		this.tf = System.nanoTime();
		double v;
		if (t.containsKey(m)) {
			double vp = t.get(m);
			v = rho*((double)getD()) + (1-rho)*vp;
		} else {
			v = (double)getD();
		}
		t.put(m, (long) v);
	}
	long getTi() {
		return ti;
	}
	long getTf() {
		return tf;
	}
	private long getD() {
		if (tf != UDEF && ti != UDEF) return tf - ti;
		return UDEF;
	}
	void addSubsequent(Activity a) {
		s.add(a);
		a.p.add(this);
	}
	void addPredecesor(Activity a) {
		p.add(a);
		a.s.add(this);
	}
	void resetSubsequents() {
		for (Activity a: s) {
			a.p.remove(this);
		}
		s.clear();
	}
	void resetPredcesors() {
		for (Activity a: p) {
			a.s.remove(this);
		}
		p.clear();
	}
	List<Activity> getSubsequents() {
		return s;
	}
	List<Activity> getPredecesors() {
		return p;
	}
	// TODO: borrar getMuscle
	public Muscle<?,?> getMuscle() {
		return m;
	}
}
