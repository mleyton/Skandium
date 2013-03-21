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
	private HashMap<Muscle<?,?>,Long> t;
	private double rho;
	Activity(HashMap<Muscle<?,?>,Long> t, Muscle<?,?> m, double rho) {
		this.ti = UDEF;
		this.tf = UDEF;
		this.m = m;
		this.s = new ArrayList<Activity>();
		this.t = t;
		this.rho = rho;
	}
	void setTi(long ti) {
		this.ti = ti;
	}
	void setTf(long tf) {
		this.tf = tf;
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
	long getD() {
		if (tf != UDEF && ti != UDEF) return tf - ti;
		return UDEF;
	}
	void addSubsequent(Activity a) {
		s.add(a);
	}
	List<Activity> getSubsequents() {
		return s;
	}
	// TODO: borrar getMuscle
	public Muscle<?,?> getMuscle() {
		return m;
	}
}
