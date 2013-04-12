package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
	Activity copyForward(Controller ctrl) {
		return copyForwardRec(ctrl, new HashMap<Activity,Activity>());
	}
	private Activity copyForwardRec(Controller ctrl, HashMap<Activity,Activity> amap) {
		if (amap.containsKey(this)) return amap.get(this);
		Activity a = new Activity(t,m,rho);
		a.ti = ti;
		a.tf = tf;
		amap.put(this, a);
		if (ctrl.isLastActivity(this)) return a;
		for (Activity n: s)
			a.addSubsequent(n.copyForwardRec(ctrl, amap));
		return a;
	}
	Muscle<?,?> getMuscle() {
		return m;
	}
	void setTi(long ti) {
		this.ti = ti;
	}
	void setTf(long tf) {
		this.tf = tf;
	}
	long getMuscleDuration() {
		return t.get(m);
	}
	void bestEffortFillForward(TimeLine tl, Box<Long> curr, Box<Long> max) {
		bestEffortFillForward(tl, curr, max, new HashSet<Activity>());
	}
	private void bestEffortFillForward(TimeLine tl, Box<Long> curr, Box<Long> max, 
			HashSet<Activity> visitados) {
		if (visitados.contains(this)) return;
		visitados.add(this);
		if (ti != UDEF && tf != UDEF) {
			tl.addActivity(this);
			if (tf > curr.get()) curr.set(tf);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.bestEffortFillForward(tl, curr, max, visitados);
			}
			return;
		}
		if (ti != UDEF && tf == UDEF) {
			tf = ti + getMuscleDuration();
			tl.addActivity(this);
			if (ti > curr.get()) curr.set(ti);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.bestEffortFillForward(tl, curr, max, visitados);
			}
			return;
		}
		if (ti == UDEF && tf != UDEF) {
			throw new RuntimeException("Should not be here!");
		}
		if (ti == UDEF && tf == UDEF) {
			long maxPred = UDEF;
			for (Activity a: p) {
				a.bestEffortFillForward(tl, curr, max, visitados);
				if (a.getTf() > maxPred) maxPred = a.getTf();
			}
			if (maxPred == UDEF) throw new RuntimeException("Should not be here!");
			ti = maxPred;
			tf = ti + getMuscleDuration();
			tl.addActivity(this);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.bestEffortFillForward(tl, curr, max, visitados);
			}
			return;
		}
	}
	void fifoFillForward(TimeLine tl, Box<Long> max, int threads) {
		fifoFillForward(tl, max, new HashSet<Activity>(),
				threads);
	}
	private void fifoFillForward(TimeLine tl, Box<Long> max, 
			HashSet<Activity> visitados, int threads) {
		if (visitados.contains(this)) return;
		visitados.add(this);
		if (ti != UDEF && tf != UDEF) {
			tl.addActivity(this);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.fifoFillForward(tl, max, visitados, threads);
			}
			return;
		}
		if (ti != UDEF && tf == UDEF) {
			tf = ti + getMuscleDuration();
			tl.addActivity(this);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.fifoFillForward(tl, max, visitados, threads);
			}
			return;
		}
		if (ti == UDEF && tf != UDEF) {
			throw new RuntimeException("Should not be here!");
		}
		if (ti == UDEF && tf == UDEF) {
			long maxPred = UDEF;
			for (Activity a: p) {
				a.fifoFillForward(tl, max, visitados,threads);
				if (a.getTf() > maxPred) maxPred = a.getTf();
			}
			if (maxPred == UDEF) throw new RuntimeException("Should not be here!");
			ti = maxPred;
			tl.addActivity(this,threads);
			if (tf > max.get()) max.set(tf);
			for (Activity a: s) {
				a.fifoFillForward(tl, max, visitados,threads);
			}
			return;
		}
	}
}
