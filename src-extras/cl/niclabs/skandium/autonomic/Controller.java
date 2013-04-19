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
import java.util.PriorityQueue;
import java.util.Stack;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.While;

/**
 * Controller class extends from GenericListener of the event support of 
 * Skandium.  Its goal is to "control" the autonomic feature, initializing 
 * structures, controlling events raised by Skandium, and controlling the
 * threads increasing or decreasing.
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 *
 */
class Controller extends GenericListener {
	
	/*
	 * List of active state on the active state machines
	 */
	private List<State> active;
	
	/*
	 * Initial Activity on the actual Dependency Activity Graph (DAG)
	 */
	private Activity initialAct;
	
	/*
	 * Reference to the Skandium singleton
	 */
	private Skandium skandium;
	
	/*
	 * Nanosecs of the last execution of the threads analysis. This attribute
	 * is maintained for assuring the poll interval parameter 
	 */
	private long lastExecution;
	
	/*
	 * Poll interval parameter. It is the estimation upgrade interval in 
	 * nanosecs.
	 */
	private long pollCheck;
	
	/*
	 * Map that holds the function time "t", where t(f) is the estimated 
	 * execution time in nanosecs of muscle f
	 */
	private HashMap<Muscle<?,?>,Long> t;

	/*
	 * Map that holds the function "card", where card(f) is the estimated 
	 * cardinality of muscles split, and condition.  
	 * 
	 * For split, card(split) is the estimated length of the result array after
	 * the split execution.  
	 * 
	 * For condition muscle, in While skeleton, card(condition) is the 
	 * estimated times condition will return true.
	 * 
	 * For condition muscle, in DaC skeleton, card (condition) is the recursive 
	 * tree deep.
	 */
	private HashMap<Muscle<?,?>, Integer> card;

	/*
	 * Set of muscles, filled during the State Machine generation (SGenerator),
	 * it is used for check if the dependency activity graph is ready to be
	 * completed (AEstimator)
	 */
	private HashSet<Muscle<?,?>> muscles;

	/*
	 * There is a relation one-to-one between a Skeleton, on the nested 
	 * Skeletons, and a State Machine Header (SMHead).  The smHead holds 
	 * runtime information about state machine: 
	 * 1. its related Skeleton trace,
	 * 2. runtime index for identification for relation with the events, 
	 * 3. parent runtime index if its current skeleton is DaC,
	 * 4. current state, 
	 * 5. initial and last activities,
	 * 6. specific skeleton runtime information:
	 * 		- Counter of While
	 * 		- Current Activity of While
	 * 		- Deep of DaC
	 * 7. Sub SMHeads, for modeling nested relations and holds statuses of
	 *    internal nested skeleton's instances.  
	 */
	private SMHead smHead;

	/*
	 * parameter that defines the weight of a new actual value for the 
	 * calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value 
	 */
	private double rho;

	/*
	 * Reference to the main skeleton
	 */
	private Skeleton<?,?> skel;
	
	/*
	 * Wall Clock Time goal.
	 */
	private long wallClocktimeGoal;

	/*
	 * It is possible that the level of parallelism needed
	 * could result in a very high number. This parameter imposes a limit for 
	 * the increase of threads.  Its default value is 2 times the number of 
	 * cores.
	 */
	private int threadLimit;

	/*
	 * true, activates the verbose mode.
	 */
	private boolean verboseMode;
	
	/**
	 * Constructor called from the main class AutonomicThreads. It initializes 
	 * the system parameters: skel, skandium, poolCheck, rho, wall clock time
	 * goal, thread limit and verbose mode; and initilizes the estimation 
	 * structures t and card.
	 * 
	 * The state machines and actual dependency activity graph is created 
	 * 
	 * @param skel skeleton that is about to starts its execution with 
	 * Autonomic behavior.
	 * @param skandium instance of Skandium singleton that has the method for
	 * update the number of threads during an skeleton execution.
	 * @param poolCheck pool check value that defines the poll interval for the
	 * estimation upgrade in nanosecs.
	 * @param t Map that holds the function time "t", where t(f) is the 
	 * estimated execution time in nanosecs of muscle f.  This allows to set 
	 * the seeds for the estimating algorithm.
	 * @param card Map that holds the function "card", where card(f) is the 
	 * estimated cardinality of muscles split, and condition.  
	 * 
	 * For split, card(split) is the estimated length of the result array after
	 * the split execution.  
	 * 
	 * For condition muscle, in While skeleton, card(condition) is the 
	 * estimated times condition will return true.
	 * 
	 * For condition muscle, in DaC skeleton, card (condition) is the recursive 
	 * tree deep.
	 * 
	 * This allows to set the seeds for the estimating algorithm.
	 * @param rho parameter that defines the weight of a new actual value for
	 * the calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value
	 * @param wallClocktimeGoal WCT goal, default value: 0 in order to force a 
	 * best effort execution.
	 * @param threadLimit It is possible that the level of parallelism needed
	 * could result in a very high number. This parameter imposes a limit for 
	 * the increase of threads.  Its default value is 2 times the number of 
	 * cores.
	 * @param verboseMode true, activates the verbose mode. Default value: 
	 * false
	 */
	Controller(Skeleton<?,?> skel, Skandium skandium, long poolCheck, 
			HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, Integer> card, 
			double rho, long wallClocktimeGoal, int threadLimit, 
			boolean verboseMode) {
		this.skandium = skandium; 
		this.pollCheck = poolCheck;
		this.threadLimit = threadLimit;
		this.verboseMode = verboseMode;
		this.t = t;
		this.card = card;
		this.wallClocktimeGoal = wallClocktimeGoal;
		lastExecution = 0;
		/*
		 * Following two lines creates the initial state machine and the
		 * initial DAG.
		 */
		SGenerator visitor = new SGenerator(t, card, rho);
		skel.accept(visitor);
		/*
		 * State I is the global initial state, who's only one possible 
		 * transition is the transition created during the previous line
		 */
		State I = new State(StateType.I);
		I.addTransition(visitor.getInitialTrans());
		/*
		 * The list of active states is initialized with the state I, and the
		 * initialAct (initial DAG) is initialized with the initial activity
		 * created during the creation of the State Machine. 
		 */
		active = new ArrayList<State>();
		active.add(I);
		initialAct = visitor.getInitialAct();
		/*
		 * To identify the last activity, is the only activity who's subsequent
		 * is the initial activity.
		 */
		Activity lastAct = visitor.getLastAct();
		lastAct.addSubsequent(initialAct);
		/*
		 * Initializes the set of muscles and the State Machine Header.
		 */
		muscles = visitor.getMuscles();
		smHead = visitor.getSMHead();
		this.rho = rho;
		this.skel = skel;
	}

	/**
	 * This inherited GenericListener method is the event listener guard.
	 * It is a filter for events, only the important ones for the state 
	 * machines are kept:
	 * - seq(fe)@before(i)
	 * - seq(fe)@after(i)
	 * - while(fc,subSkel)@beforeCondition(i)
	 * - while(fc,subSkel)@afterCondition(i, conditionResult)
	 * - map(fs,subSkel,fm)@beforeSplit(i)
	 * - map(fs,subSkel,fm)@afterSplit(i, splitCardinality)
	 * - map(fs,subSkel,fm)@beforeMerge(i)
	 * - map(fs,subSkel,fm)@afterMerge(i)
	 * - dac(fc,fs,subSkel,fm)@beforeCondition(i)
	 * - dac(fc,fs,subSkel,fm)@afterCondition(i, conditionResult)
	 * - dac(fc,fs,subSkel,fm)@beforeSplit(i)
	 * - dac(fc,fs,subSkel,fm)@afterSplit(i, splitCardinality)
	 * - dac(fc,fs,subSkel,fm)@beforeMerge(i)
	 * - dac(fc,fs,subSkel,fm)@afterMerge(i)
	 */
	@Override
	public boolean guard(Object param, 
			@SuppressWarnings("rawtypes") Skeleton[] strace, int index,
			boolean cond, int parent, When when, Where where) {
		Skeleton<?,?> current = strace[strace.length-1];
		if (current instanceof Seq) return true;
		if (current instanceof While) {
			if (where == Where.CONDITION) return true;
			return false;
		}
		if (current instanceof Map) {
			if (where == Where.SPLIT) return true;
			if (where == Where.MERGE) return true;
			return false;
		}
		if (current instanceof DaC) {
			if (where == Where.CONDITION) return true;
			if (where == Where.SPLIT) return true;
			if (where == Where.MERGE) return true;
			return false;
		}
		return false;
	}
	
	/**
	 * Event handler, this method will be executed only if the guard returned 
	 * true.
	 */
	@Override
	synchronized public Object handler(Object param, 
			@SuppressWarnings("rawtypes") Skeleton[] strace, int index,
			boolean cond, int parent, When when, Where where) {
		/*
		 * The stack of skeletons (skeleton's trace) internally is represented
		 * by a Stack<Skeleton>, not Skeleton[], the following two lines make
		 * this transformation.
		 */
		Stack<Skeleton<?,?>> trace = new Stack<Skeleton<?,?>>();
		for(Skeleton<?,?> s: strace) trace.add(s);
		
		/* 
		 * In a deterministic state machine it is necessary to make a 
		 * deterministic and functional relation between the events and the
		 * transitions identifiers between states.
		 * The following line creates the transition identifier 
		 * (TransitionLabel) related to the event that is just raised. 
		 */
		TransitionLabel event = new TransitionLabel(new SMHead(trace),when,
				where, cond);
		/*
		 * It is possible that an event could match more than one transition,
		 * this is the case, for example, after a split on a map the possible
		 * transition could be anyone of the subSkel's, because any of them has 
		 * been initialized with its actual index(i), therefore any transition
		 * could be related to the event.  But if there is some transition 
		 * already initialized the event has to be related to the initialized 
		 * one, in order to solve this, it was implemented using a 
		 * PriorityQueue (areIn) that sorts decrementally the transitions that 
		 * matches the event and the event is then related to the head areIn 
		 * 
		 * fromTransState is the structure that holds the relation of all the 
		 * possible transitions that matches the event, sorted in areIn, with
		 * its corresponding soruce state, from the list of active states.
		 */
		HashMap<Transition,State> fromTransToState = 
				new HashMap<Transition,State>();
		PriorityQueue<Transition> areIn = new PriorityQueue<Transition>() ;
		for (State s: active) {
			for(Transition t: s.getTransitions(event)) {
				areIn.add(t);
				fromTransToState.put(t, s);
			}
		}
		/*
		 * Transition t, will hold the matched transition.  The following lines
		 * searches on the areIn structure for the right transition that 
		 * matches the event if it is not founded, an exception is raised. 
		 */
		Transition t = null;
		{
			boolean found = false;
			while (found == false && !areIn.isEmpty()) {
				t = areIn.poll();
				if (t.isTheOne(index,parent)) found=true;
			}
			if(found==false) throw 
			new RuntimeException("Event not expected, should not be here! " +
			strace[strace.length-1] + " " + when + " " + where + " " + cond + 
			"" + index);
		}
		/*
		 * The from state is removed from the active state's structure only if
		 * it is not persistent.  Persistent states are the ones that are not
		 * removed from the active set, until all its possible transitions are 
		 * executed.  Like map's after split states.
		 */
		fromTransToState.get(t).remove(active, t);
		/*
		 * The following code executed the code related to the transition
		 */
		{
			int type = t.getType(); 
			if (type == TransitionLabel.VOID) {
				t.execute();
			} else if (type == TransitionLabel.FS_CARD) {
				Object[] afterSplit = (Object[]) param;
				t.execute(afterSplit.length);
			} else {
				t.execute(index);
			}
		}
		/*
		 * Sets the current state of the State Machine Header to de destination
		 * state.
		 */
		t.setCurrentState();
		/*
		 * Includes the destination state to the list of active states.
		 */
		active.add(t.getDest());
		/*
		 * If the poll interval for thread analysis is achieved and the 
		 * dependency activity graph is ready (all the estimated variables are
		 * initialized) for its completion, a new thread analysis is done. 
		 */
		long currTime = System.nanoTime(); 
		if (currTime - lastExecution > pollCheck) {
			lastExecution = currTime;
			if (isActivityDiagramReady()) {
				threadsControl();
			}
		}

		return param;
	}
	
	/**
	 * Checks if activity a is the last activity
	 * @param a Activity to check if it is the last activity
	 * @return true if a is the last activity.
	 */
	boolean isLastActivity(Activity a) {
		for (Activity s: a.getSubsequents()) {
			if (s == initialAct) return true;
		}
		return false;
	}
	
	/**
	 * Checks if the dependency activity graph is ready to thread analysis.
	 * It is ready if all the muscles has an estimated execution time, and if
	 * all its splits and conditions has cardinality. 
	 */
	private boolean isActivityDiagramReady() {
		for (Muscle<?,?> m:muscles) {
			if (!t.containsKey(m)) return false;
			if ((m instanceof Condition<?>)&&(!card.containsKey(m))) return false;
			if ((m instanceof Split<?,?>)&&(!card.containsKey(m))) return false;
		}
		return true;
	}
	
	/**
	 * Prints in standard output the "t" structure
	 */
	void printT() {
		System.out.println("Estimated execution times (ms)");
		for (Muscle<?,?> m:t.keySet()) {
			System.out.println(m.getClass().getName() + "\t" + ((long)t.get(m).longValue()/1000000));
		}
	}

	/**
	 * Prints in standard output the "card" structure
	 */
	void printCard() {
		System.out.println("Estimated cardinality");
		for (Muscle<?,?> m:card.keySet()) {
			System.out.println(m.getClass().getName() + "\t" + card.get(m).intValue());
		}
	}
	
	/*
	 * Threads analysis method:
	 * 1. Completes the structure (activities and dependencies) estimation of
	 *    the DAG.
	 * 2. Creates 2 copies of the DAG to complete the initial and final times.
	 *    of the best effort and fifo cases.
	 * 3. Increases or decreases the number of threads accordingly.  
	 */
	private void threadsControl() {
		
		/*
		 * Completes the structure (activities and dependencies) estimation of
		 * the DAG
		 */
		AEstimator aest = new AEstimator(this.t,card,smHead,muscles,rho);
		skel.accept(aest);
		
		/* 
		 * copy of DAG for best effort estimation
		 */
		Activity beAct = initialAct.copyForward(this);
		TimeLine beTL = new TimeLine();
		Box<Long> beCurr = new Box<Long>((long)0);
		Box<Long> beMax = new Box<Long>((long)0);
		beAct.bestEffortFillForward(beTL, beCurr, beMax);
		int beMaxThreads = beTL.maxThreads(beCurr.get());
		beMaxThreads = beMaxThreads == 0? 1: beMaxThreads;

		/*
		 * Copy of DAG for fifo estimation
		 */
		Activity fifoAct = initialAct.copyForward(this);
		TimeLine fifoTL = new TimeLine();
		Box<Long> fifoMax = new Box<Long>((long)0);
		int testThreads = skandium.getMaxThreads() == 1? 1: skandium.getMaxThreads()/2;
		fifoAct.fifoFillForward(fifoTL, fifoMax, testThreads);

		/*
		 * Threads increase or decrease analysis
		 */
		long elapsedTime = beCurr.get() - initialAct.getTi();
		long beTimeLeft = beMax.get() - beCurr.get();
		long fifoTimeLeft = fifoMax.get() - beCurr.get();
		long goal = wallClocktimeGoal - elapsedTime;
		if (goal >= fifoTimeLeft) {
			skandium.setMaxThreads(testThreads);
		} else {
			skandium.setMaxThreads(beMaxThreads > threadLimit? threadLimit : beMaxThreads);
		}

		/*
		 * Verbose information
		 */
		if (verboseMode) {
			System.out.println("--");
			System.out.println("CURRENT THREADS:\t"+ ((int) Thread.activeCount()-1));
			System.out.println("GOAL (ms):\t" + ((long)wallClocktimeGoal/1000000));
			System.out.println("ELAPSED TIME (ms):\t" + ((long) elapsedTime/1000000));
			System.out.println("BEST EFFORT TOTAL TIME (ms):\t" + ((long) (beTimeLeft + elapsedTime)/1000000));
			System.out.println("FIFO TOTAL TIME (ms):\t" + ((long) (fifoTimeLeft + elapsedTime)/1000000));
			if (goal >= fifoTimeLeft) {
				System.out.println("NEW THREADS FIFO:\t" + testThreads);
			} else {
				System.out.println("NEW THREADS BEST EFFORT:\t" + beMaxThreads);
			}
			printT();
			printCard();
		}
	}
}
