/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2013 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */

package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cl.niclabs.skandium.muscles.Muscle;

/**
 * Objects of the Activity class represents activities of the dependency 
 * activity graph generated during the skeleton execution.
 * 
 * @author Gustavo Pabon <gustavo.pabon@gmail.com>
 *
 */
class Activity {

	/**
	 * UDEF, constant that represents the value "undefined" for time variables
	 */
	public static final long UDEF = -1;

	/*
	 * activity initial time
	 */
	private long ti;
	
	/*
	 * activity final time
	 */
	private long tf;
	
	/*
	 * muscle that will be/is being/was executed
	 */
	private Muscle<?,?> m;	
	
	/*
	 * list of subsequent activities, activities that depends of "this"
	 */
	private List<Activity> s;
	
	/*
	 * list of predecessor activities, "this" depends of such activities
	 */
	private List<Activity> p;
	
	/*
	 * Map that holds the function time "t", where t(f) is the expected 
	 * execution timein nanosecs of muscle f
	 */
	private HashMap<Muscle<?,?>,Long> t;
	
	/*
	 * parameter that defines the weight of a new actual value for the 
	 * calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value 
	 */
	private double rho;
	
	/**
	 * 
	 * @param t	Map that holds the function time "t", where t(f) is the 
	 * expected execution timein nanosecs of muscle f.
	 * @param m	muscle that will be/is being/was executed.
	 * @param rho parameter that defines the weight of a new actual value for 
	 * the calculation of the new estimated value. the formula is:
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value
	 */
	Activity(HashMap<Muscle<?,?>,Long> t, Muscle<?,?> m, double rho) {
		this.ti = UDEF;
		this.tf = UDEF;
		this.m = m;
		this.s = new ArrayList<Activity>();
		this.p = new ArrayList<Activity>();
		this.t = t;
		this.rho = rho;
	}
	
	/**
	 * Sets the initial time of the activity with the current time in nanosecs.
	 */
	void setTi() {
		this.ti = System.nanoTime();
	}
	
	/**
	 * Sets the final time of the activity with the current time in nanosecs, 
	 * and updates the t(f) function with a new actual value using rho.
	 */
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
	
	/**
	 * @return activity's initial value.
	 */
	long getTi() {
		return ti;
	}
	
	/**
	 * @return activity's final value.
	 */
	long getTf() {
		return tf;
	}
	
	/*
	 * Returns the actual duration of an activity given its initial and final
	 * time values.
	 */
	private long getD() {
		if (tf != UDEF && ti != UDEF) return tf - ti;
		return UDEF;
	}
	
	/**
	 * Adds the activity "a", as subsequent activity.
	 * @param a Subsequent activity to be declared as such.
	 */
	void addSubsequent(Activity a) {
		s.add(a);
		a.p.add(this);
	}
	
	/**
	 * Adds the activity "a" as predecessor activity.
	 * @param a Pedecessor activity to be declared as such.
	 */
	void addPredecessor(Activity a) {
		p.add(a);
		a.s.add(this);
	}
	
	/**
	 * Reset list of subsequent activities, removing "this" from the list of 
	 * predecessor activities on each subsequent activity. 
	 */
	void resetSubsequents() {
		for (Activity a: s) {
			a.p.remove(this);
		}
		s.clear();
	}
	
	/**
	 * Reset list of predecessor activities, removing "this" from the list of 
	 * subsequent activities on each subsequent activity.
	 */
	void resetPredcesors() {
		for (Activity a: p) {
			a.s.remove(this);
		}
		p.clear();
	}
	
	/**
	 * @return list of subsequent activities
	 */
	List<Activity> getSubsequents() {
		return s;
	}
	
	/**
	 * @return list of predecessor activities
	 */
	List<Activity> getPredecesors() {
		return p;
	}
	
	/**
	 * @param ctrl Reference of the controlling object that knows which is the
	 * last activity.
	 * @return a copy of "this" activity copying its subsequent activities
	 * recursively.
	 */
	Activity copyForward(Controller ctrl) {
		return copyForwardRec(ctrl, new HashMap<Activity,Activity>());
	}
	
	/*
	 * amap is an accumulator map that holds the relation of the original 
	 * activity with the copy activity in order to maintain the relations of
	 * subsequents and prececessors consistent.
	 */
	private Activity copyForwardRec(Controller ctrl, HashMap<Activity,
			Activity> amap) {
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
	
	/**
	 * @return the muscle that will be/is being/was executed.
	 */
	Muscle<?,?> getMuscle() {
		return m;
	}
	
	/**
	 * Sets initial time with the "ti" time.
	 * @param ti initial time
	 */
	void setTi(long ti) {
		this.ti = ti;
	}
	
	/**
	 * Sets final time with the "tf" time.
	 * @param tf final time
	 */
	void setTf(long tf) {
		this.tf = tf;
	}
	
	/**
	 * Gets the estimated, not actual, duration of the activity.  Actually 
	 * this is the value of t(m). 
	 * @return
	 */
	long getMuscleDuration() {
		return t.get(m);
	}
	
	/**
	 * Completes the initial and final time givem the relations of dependency
	 * with its subsequent and predecessor activities.
	 * Boxed variable are used to model the pass-by-reference parameters for
	 * parameters with primitive types.
	 * @param tl  in order to improve performance, at the same time that this
	 * method is executing, the time line is filled in order to calculate the 
	 * total best effort wall clock execution time, and the maximum level of 
	 * parallelism needed.
	 * @param curr during the calculation, the last time set during actual 
	 * execution is held in this boxed variable.
	 * @param max during the calculation, the total time (final time of last 
	 * activity) is held in this boxed variable. 
	 */
	void bestEffortFillForward(TimeLine tl, Box<Long> curr, Box<Long> max) {
		bestEffortFillForward(tl, curr, max, new HashSet<Activity>());
	}
	
	/*
	 * visitados is an accumulator set that holds the activities already 
	 * calculated, for avoiding double calculations.
	 * 
	 * This method has three possible escenarion:
	 * 1. when ti and tf are already calculated during actual execution. In 
	 *    this case the only thing left to do is a recursive call for the 
	 *    subsequent activities.
	 * 2. when ti is already calculated, but tf is not. In this case it is 
	 *    needed to estimate the duration using the function t.
	 * 3. when neither ti or tf have values.  In this case ti is calculated as
	 *    the maximum tf of its predecessors and tf is calculated using the 
	 *    estimated duration, function t. 
	 */	
	private void bestEffortFillForward(TimeLine tl, Box<Long> curr, 
			Box<Long> max, HashSet<Activity> visitados) {
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
	
	/**
	 * Completes the initial and final time given the relations of dependency
	 * with its subsequent and predecessor activities and the limitation of
	 * maximum level of parallelism, parameter "threads".
	 * Boxed variable are used to model the pass-by-reference parameters for
	 * parameters with primitive types.
	 * @param tl  in order to improve performance, at the same time that this
	 * method is executing, the time line is filled in order to calculate the 
	 * total fifo wall clock execution time.
	 * @param max during the calculation, the total time (final time of last 
	 * activity) is held in this boxed variable. 
	 * @param threads limitation of maximum level of parallelism
	 */
	void fifoFillForward(TimeLine tl, Box<Long> max, int threads) {
		fifoFillForward(tl, max, new HashSet<Activity>(),
				threads);
	}
	
	/*
	 * visitados is an accumulator set that holds the activities already 
	 * calculated, for avoiding double calculations.
	 * 
	 * This method has three possible escenarion:
	 * 1. when ti and tf are already calculated during actual execution. In 
	 *    this case the only thing left to do is a recursive call for the 
	 *    subsequent activities.
	 * 2. when ti is already calculated, but tf is not. In this case it is 
	 *    needed to estimate the duration using the function t.
	 * 3. when neither ti or tf have values.  In this case ti is calculated as
	 *    the maximum tf of its predecessors and tf is calculated using the 
	 *    estimated duration, function t. 
	 */	
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
