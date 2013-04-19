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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;

/**
 * This skeleton visitor class initializes the State Machine and Dependency
 * Activity graph (DAG). 
 * 
 * Its goal is to calculate the following values:
 * 1. initialTrans: Initial transition who its destination state is the initial 
 *    state of the state machine created.
 * 2. lastState: Final state of the state machine created.
 * 3. Muscles: set of different muscles that could be executed
 * 4. smHead: State Machine Header. There is a relation one-to-one between a 
 *    Skeleton, on the nested Skeletons, and a State Machine Header (SMHead).
 *    The smHead holds runtime information about state machine:
 *       a. its related Skeleton trace,
 *       b. runtime index for identification for relation with the events, 
 *       c. parent runtime index if its current skeleton is DaC, 
 *       d. current state, 
 *       e. initial and last activities, 
 *       f. specific skeleton runtime information:
 *        	- Counter of While
 *			- Current Activity of While
 *          - Deep of DaC
 *       g. Sub SMHeads, for modeling nested relations and holds statuses of 
 *          internal nested skeleton's instances.  
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 *
 */
class SGenerator implements SkeletonVisitor {

	/*
	 * Initial transition who its destination state is the initial 
	 * state of the state machine created.
	 */
	private Transition initialTrans;

	/*
	 * Final state of the state machine created.
	 */
	private State lastState;
	
	/*
	 * Skeleton's trace
	 */
	private Stack<Skeleton<?,?>> strace;
	
	/*
	 * Map that holds the function time "t", where t(f) is the estimated 
	 * execution timein nanosecs of muscle f
	 */	
	private HashMap<Muscle<?,?>,Long> t;
	
	/*
	 * Map that holds the funtion "card", where card(f) is the estimated 
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
	 * parameter that defines the weight of a new actual value for the 
	 * calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value 
	 */
	private double rho;
	
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

	/**
	 * Constructor intended to be called from the controller for 
	 * initialization.
	 * 
	 * @param t Map that holds the function time "t", where t(f) is the 
	 * estimated execution time in nanosecs of muscle f
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
	 * @param rho parameter that defines the weight of a new actual value for
	 * the calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value 
	 */
	SGenerator(HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, Integer> card,
			double rho) {
		this.strace = new Stack<Skeleton<?,?>>();
		this.t = t;
		this.card = card;
		this.muscles = new HashSet<Muscle<?,?>>();
		this.rho = rho;
	}

	/**
	 * Constructor intended to be called as part of an ongoing execution. 
	 * The difference with respect to the above one, is that this one creates a
	 * copy of the skeleton trace and the muscles is not initialized.
	 *  
	 * @param trace Skeleton's trace
	 * @param t Map that holds the function time "t", where t(f) is the 
	 * estimated execution time in nanosecs of muscle f
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
	 * @param rho parameter that defines the weight of a new actual value for
	 * the calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value 
	 * @param muscles Set of muscles, filled during the State Machine
	 * generation (SGenerator), it is used for check if the dependency activity
	 * graph is ready to be completed (AEstimator)
	 */
	SGenerator(
			Stack<Skeleton<?,?>> trace,
			HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>,Integer> card,
			double rho, HashSet<Muscle<?,?>> muscles) {
		this(t, card, rho);
		this.strace.addAll((Collection<? extends Skeleton<?, ?>>) trace);
		this.muscles = muscles; 
	}

	/**
	 * Farm case. It is ignored, all the calculations are passthru to the
	 * subSkel.
	 */
	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		strace.add(skeleton);
		skeleton.getSubskel().accept(this);
	}

	/**
	 * Pipe case. It creates two sub-state-machines, one for the stage one, 
	 * and another one for the stage 2, producing a final state machine as 
	 * follows:
	 * 
	 *  --(--> SM Stage 1)--(--> SM Stage 2)
	 *      ^                             ^
	 *  Pipe initialTrans        Pipe lastState
	 *  
	 * The actual DAG is initialized as follows:
	 * [Stage 1 initialAct] ->...-> [Stage 1 lastAct] -> [Stage 2 initialAct] ->
	 * ... -> [Stage 2 lastAct]
	 */
	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		strace.add(skeleton);
		SGenerator stage2 = new SGenerator(strace,t,card,rho,muscles);
		skeleton.getStage2().accept(stage2);
		SGenerator stage1 = new SGenerator(strace,t,card,rho,muscles);
		skeleton.getStage1().accept(stage1);
		stage1.getLastState().addTransition(stage2.getInitialTrans());
		initialTrans = stage1.getInitialTrans();
		lastState = stage2.getLastState();
		smHead = new SMHead(strace);
		smHead.setInitialActivity(stage1.getInitialAct());
		stage1.getLastAct().addSubsequent(stage2.getInitialAct());
		smHead.setLastActivity(stage2.getLastAct());
		smHead.addSub(stage1.getSMHead());
		smHead.addSub(stage2.getSMHead());
	}

	/**
	 * Seq case. 
	 * 
	 * The DAG is initialized as follows:
	 * 
	 * ->[ti|t(fe)|tf]->
	 *   ^
	 *  inital and last activity 
	 * 
	 * The state machine is created as follows:
	 * 
	 * --seq(fe)@before(i)-->(I)--seq(fe)@after(i)-->(F)
	 *   ^                                           ^
	 *   seq initial trans                      seq last state
	 *   
	 * the execution of "seq(fe)@before(i)" should do:
	 * 1. sets index (i) to the State machine header
	 * 2. sets the ti with the current time
	 * 
	 * the execution of "seq(fe)@after(i)" should do:
	 * 1. sets tf with the current time
	 * 2. updates t(fe) accordingly.
	 */  
	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getExecute());
		lastState = new State(StateType.F);
		smHead = new SMHead(strace);
		Activity a = new Activity(t,skeleton.getExecute(),rho);
		smHead.setInitialActivity(a);
		smHead.setLastActivity(a);
		Transition toF = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.SKELETON, false),lastState) {
			@Override
			protected void execute() {
				smHead.getInitialActivity().setTf();
			}
		};
		State I = new State(StateType.I);
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(smHead, When.BEFORE, Where.SKELETON, false),I) {
			@Override
			protected void execute(int i) {
				smHead.setIndex(i);
				smHead.getInitialActivity().setTi();
			}
		};
	}

	/**
	 * If is not supported
	 */
	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		throw new RuntimeException("No autonomic support for IF Skeleton");
	}

	/**
	 * While case. 
	 * 
	 * The DAG is initialized as follows:
	 * 
	 * ->[ti|t(fc)|tf]-> ... ->[ti|t(fc)|tf]->
	 *   ^                     ^
	 *   ^                     last activity
	 *   initial and current while activity
	 *   
	 * The state machine is created as follows:
	 * 
	 * ---while(fc,subSkel)@beforeCondition(i)--->(I)
	 *    ^   (subSkel lastState)--while(fc,subSkel)@beforeCondition(i)--->(I)
	 *    ^                                        
	 *    initialTrans
	 * 
	 * (I)-while(fc,subSkel)@afterCondition(i,true)->(T)-<subSkel initialTrans>
	 * (I)-while(fc,subSkel)@afterCondition(i,false)->(F)
	 *                                                ^
	 *                                                lastState
	 *    
	 * the execution of "while(fc,subSkel)@beforeCondition(i)" should do:
	 * 1. sets index (i) to the State machine header if it is initialTrans
	 * 2. sets the current activity's ti with the current time
	 * 
	 * the execution of "while(fc,subSkel)@afterCondition(i,true)" should do:
	 * 1. sets the current activity's tf with the current time
	 * 2. updates t(fc) accordingly.
	 * 3. and updates DAG with:
	 *  [whileCurrentAct]->[subSkel initialAct]->[ti|t(fc)|tf]-> ... ->
	 *  
	 * the execution of "while(fc,subSkel)@afterCondition(i,false)" should do:
	 * 1. sets the current activity's tf with the current time
	 * 2. updates t(fc) accordingly.
	 * 3. updates card(fc) accordingly.
	 * 4. sets currentActivity as last activity.
	 */  
	@Override
	public <P> void visit(final While<P> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getCondition());
		lastState = new State(StateType.F);
		smHead = new SMHead(strace);
		
		final Condition<?> cond = skeleton.getCondition();
		smHead.setInitialActivity(new Activity(t,cond,rho));
		smHead.setLastActivity(new Activity(t,cond,rho));
		smHead.setWhileCurrentActivity(smHead.getInitialActivity());
		smHead.setWhileCounter(0);
		
		Transition toF = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.CONDITION, false),lastState) {
			@Override
			protected void execute() {
				setLastActivity(smHead.getWhileCurrentActivity());
				smHead.getLastActivity().setTf();
				setCard(cond,smHead.getWhileCounter());
			}
		};
		State I = new State(StateType.I);
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(smHead, When.BEFORE, Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				smHead.setIndex(i);
				smHead.getInitialActivity().setTi();
			}
		};
		final Transition toI = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				smHead.getWhileCurrentActivity().setTi();
			}
		};
		final State T = new State(StateType.T);
		Transition toT = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.CONDITION, true),T) {
			@Override
			protected void execute() {
				SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
				skeleton.getSubskel().accept(subSkel);
				T.addTransition(subSkel.getInitialTrans());
				subSkel.getLastState().addTransition(toI);
				smHead.getWhileCurrentActivity().setTf();
				smHead.setWhileCounter(smHead.getWhileCounter()+1);
				smHead.getWhileCurrentActivity().resetSubsequents();
				smHead.getWhileCurrentActivity().addSubsequent(subSkel.getInitialAct());
				smHead.setWhileCurrentActivity(new Activity(t,cond,rho));
				subSkel.getLastAct().addSubsequent(smHead.getWhileCurrentActivity());
				smHead.addSub(subSkel.getSMHead());
			}
		};
		I.addTransition(toT);
	}

	/**
	 * For case. 
	 * 
	 * Produces an initial DAG as follows:
	 * 
	 * ->[step1 initialAct]->...->[step1 lastAct]->[step2 initialAct]->
	 *   ^   [step2 lastAct]-> ... n times ... -> [stepn initialAct] ->
	 *   ^   [stepn lastAct]
	 *   ^   ^
	 *   ^   last Activity
	 *   initial Activity
	 *   
	 * State machine as:
	 *  -<step1 initialTrans>->...(step1 lastState)-<step2 initialTrans->...
	 *  ^     -<stepn initialTrans>->(stepn lastState)
	 *  ^                            ^
	 *  ^                            Last state
	 *  initial trans 
	 *  
	 */
	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(skeleton);
		smHead = new SMHead(strace);
		State lastLastState = null;
		Activity lastLastAct = null;
		int n = skeleton.getTimes();
		for (int i=0; i<n; i++) {
			SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
			skeleton.getSubskel().accept(subSkel);
			if (lastLastState == null) {
				initialTrans = subSkel.getInitialTrans();
				smHead.setInitialActivity(subSkel.getInitialAct());
			} else {
				lastLastState.addTransition(subSkel.getInitialTrans());
				lastLastAct.addSubsequent(subSkel.getInitialAct());
			}
			lastLastState = subSkel.getLastState();
			lastLastAct = subSkel.getLastAct();
			if (i==n-1) {
				lastState = lastLastState;
				smHead.setLastActivity(lastLastAct);
			}
			smHead.addSub(subSkel.getSMHead());
		}
	}

	/**
	 * Map case.
	 * 
	 * Initial DAG:
	 * 
	 * 
	 * ->[ti|t(fs)|tf]-> ... ->[ti|t(fm)|tf]->
	 *   ^                     ^
	 *   initial Activity      last Activity
	 * 
	 * State machine
	 * 
	 * -map(fs,subSkel,fm)@beforeSplit(i)->(I)
	 *  ^
	 *  initial trans
	 * 
	 * (I)-map(fs,subSkel,fm)@afterSplit(i,splitCardinality)->(S)
	 * 
	 *     --<subSkel initialTrans>-->...(subSkel lastState)
	 * (S)   ...
	 *     --<subSkel initialTrans>-->...(subSkel lastState)
	 * 
	 *     (subSkel lastState)-map(fs,subSkel,fm)@beforeMerge(i)-->
	 *     ...                                                   -> (M) 
	 *     (subSkel lastState)-map(fs,subSkel,fm)@beforeMerge(i)-->
	 *     
	 *  (M)-map(fs,subSkel,fm)@afterMerge(i)->(F)
	 *                                        ^
	 *                                        last state
	 * Execution of "map(fs,subSkel,fm)@beforeSplit(i)"
	 * 1. sets index (i) to the State machine header
	 * 2. sets the split's ti with the current time
	 * 
	 * Execution of "map(fs,subSkel,fm)@afterSplit(i,splitCardinality)"
	 * 1. sets the split's tf with the current time
	 * 2. updates t(fs) accordingly
	 * 3. updates card(fs) accordingly
	 * 4. create sub-sate machines
	 * 
	 * Execution of "map(fs,subSkel,fm)@beforeMerge(i)"
	 * - sets the merge's ti with the current time
	 * 
	 * Execution of "map(fs,subSkel,fm)@afterMerge(i)"
	 * 1. sets the merge's tf with the current time
	 * 2. updates t(fm) accordingly
	 */
	@Override
	public <P, R> void visit(final Map<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getSplit());
		muscles.add(skeleton.getMerge());
		lastState = new State(StateType.F);
		smHead = new SMHead(strace);
		
		smHead.setInitialActivity(new Activity(t,skeleton.getSplit(),rho));
		smHead.setLastActivity(new Activity(t,skeleton.getMerge(),rho));
		
		Transition toF = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.MERGE, false),lastState) {
			@Override
			protected void execute() {
				smHead.getLastActivity().setTf();
			}
		};
		State M = new State(StateType.M);
		M.addTransition(toF);
		final Transition toM = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.MERGE, false),M) {
			@Override
			protected void execute() {
				smHead.getLastActivity().setTi();
			}
		};
		final State S = new State(StateType.S,true);
		Transition toS = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.SPLIT, false),S) {
			@Override
			protected void execute(int fsCard) {
				smHead.getInitialActivity().setTf();
				setCard(skeleton.getSplit(), fsCard);
				smHead.getInitialActivity().resetSubsequents();
				smHead.getLastActivity().resetPredcesors();
				for (int i=0; i<fsCard; i++) {
					SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					S.addTransition(subSkel.getInitialTrans());
					subSkel.getLastState().addTransition(toM);

					smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
					subSkel.getLastAct().addSubsequent(smHead.getLastActivity());
					smHead.addSub(subSkel.getSMHead());
				}
			}
		};
		State I = new State(StateType.I);
		I.addTransition(toS);
		initialTrans = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.SPLIT, false),I) {
			@Override
			protected void execute(int i) {
				smHead.setIndex(i);
				smHead.getInitialActivity().setTi();
			}
		}; 
	}

	/**
	 * Fork not supported.
	 */
	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		throw new RuntimeException("No autonomic support for MAP Skeleton");

	}

	/**
	 * DaC case.
	 * 
	 * Initial DAG:
	 * 
	 * 
	 * ->[ti|t(fc)|tf]-> ... ->[ti|t(fm)|tf]->
	 *   ^                     ^
	 *   initial Activity      last Activity
	 * 
	 * State machine
	 * 
	 * -dac(fc,fs,subSkel,fm)@beforeCondition(i)->(I)
	 *  ^
	 *  initial trans
	 * 
	 * (I)-dac(fc,fs,subSkel,fm)@afterCondition(i,false)->(G)
	 * 
	 * (G)-<subSkel initialTrans>->...->(subSkel lastState)
	 *                                  ^
	 *                                  last State
	 * (I)-dac(fc,fs,subSkel,fm)@afterCondition(i,true)->(C)
	 * 
	 * (C)-dac(fc,fs,subSkel,fm)@beforeSplit(i)->(S)
	 * 
	 * (S)-dac(fc,fs,subSkel,fm)@afterSplit(i,splitCardinality)->(T)
	 * 
	 *     --<subDaC initialTrans>-->...(subDaC lastState)
	 * (T)   ...
	 *     --<subDaC initialTrans>-->...(subDaC lastState)
	 * 
	 *     (subDaC lastState)-dac(fc,fs,subSkel,fm)@beforeMerge(i)-->
	 *     ...                                                     -> (M) 
	 *     (subDaC lastState)-dac(fc,fs,subSkel,fm)@beforeMerge(i)-->
	 *     
	 *  (M)-dac(fc,fs,subSkel,fm)@afterMerge(i)->(F)
	 *                                           ^
	 *                                           last state
	 *                                           
	 * Execution of "dac(fc,fs,subSkel,fm)@beforeCondition(i)"
	 * 1. sets index (i) to the State machine header
	 * 2. sets the condition's ti with the current time
	 *
	 * Execution of "dac(fc,fs,subSkel,fm)@afterCondition(i,false)"
	 * 1. sets the condition's tf with the current time
	 * 2. updates t(fc) accordingle
	 * 3. updates card(fc) accordingly
	 * 
	 * Execution of "dac(fc,fs,subSkel,fm)@afterCondition(i,true)"
	 * 1. sets the condition's tf with the current time
	 * 2. updates t(fc) accordingly
	 * 
  	 * Execution of "dac(fc,fs,subSkel,fm)@beforeSplit(i)"
	 * - sets the split's ti with the current time
	 * 
	 * Execution of "mdac(fc,fs,subSkel,fm)@afterSplit(i,splitCardinality)"
	 * 1. sets the split's tf with the current time
	 * 2. updates t(fs) accordingly
	 * 3. updates card(fs) accordingly
	 * 4. creates sub machine states
	 * 5. completes the DAG with the sub DAGs
	 * 
	 * Execution of "dac(fc,fs,subSkel,fm)@beforeMerge(i)"
	 * - sets the merge's ti with the current time
	 * 
	 * Execution of "dac(fc,fs,subSkel,fm)@afterMerge(i)"
	 * 1. sets the merge's tf with the current time
	 * 2. updates t(fm) accordingly
	 */
	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getCondition());
		muscles.add(skeleton.getSplit());
		muscles.add(skeleton.getMerge());
		DaCM(skeleton, 0);
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	Transition getInitialTrans() {
		return initialTrans;
	}

	State getLastState() {
		return lastState;
	}

	/*
	 * DaC case in private method for simplify recursive calling.
	 */
	private <P,R> void DaCM(final DaC<P,R> skeleton, final int fcCard) {
		lastState = new State(StateType.F);
		smHead = new SMHead(strace);
		smHead.setDaCDeep(fcCard);
		smHead.setInitialActivity(new Activity(t, skeleton.getCondition(), rho));		
		final Activity splitAct = new Activity(t, skeleton.getSplit(), rho);
		smHead.setLastActivity(new Activity(t, skeleton.getMerge(), rho));
		Transition toF = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.MERGE, false),lastState) {
			@Override
			protected void execute() {
				smHead.getLastActivity().setTf();
			}
		};
		State M = new State(StateType.M);
		M.addTransition(toF);
		final Transition toM = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.MERGE, false),M) {
			@Override
			protected void execute() {
				smHead.getLastActivity().setTi();
			}
		};		
		final State T = new State(StateType.T,true);
		Transition toT = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.SPLIT, false),T) {
			@Override
			protected void execute(int fsCard) {
				splitAct.resetSubsequents();
				smHead.getLastActivity().resetPredcesors();
				for (int i=0; i<fsCard; i++) {
					SGenerator subDaC = new SGenerator(strace,t,card,rho,muscles);
					subDaC.DaCM(skeleton, fcCard+1);
					subDaC.getInitialTrans().tl.getTs().setDaCParent(this.tl.getTs().getIndex());
					T.addTransition(subDaC.getInitialTrans());
					subDaC.getLastState().addTransition(toM);
					splitAct.addSubsequent(subDaC.getInitialAct());
					subDaC.getLastAct().addSubsequent(smHead.getLastActivity());
					smHead.addSub(subDaC.getSMHead());
				}
				splitAct.setTf();
				setCard(skeleton.getSplit(), fsCard);
			}
		};
		State S = new State(StateType.S);
		S.addTransition(toT);
		Transition toS = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.SPLIT, false),S) {
			@Override
			protected void execute() {
				splitAct.setTi();
			}
		};
		State C = new State(StateType.C);
		C.addTransition(toS);
		Transition toC = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.CONDITION, true),C) {
			@Override
			protected void execute() {
				smHead.getInitialActivity().setTf();
				smHead.getInitialActivity().resetSubsequents();
				smHead.getLastActivity().resetPredcesors();
				smHead.getInitialActivity().addSubsequent(splitAct);
			}
		};
		State I = new State(StateType.I);
		I.addTransition(toC);
		final State G = new State(StateType.G);
		Transition toG = new Transition(new TransitionLabel(smHead, When.AFTER,  Where.CONDITION, false),G) {
			@Override
			protected void execute() {
				SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
				skeleton.getSkeleton().accept(subSkel);
				G.addTransition(subSkel.getInitialTrans());
				for (Transition t: lastState.getTransitions())
					subSkel.getLastState().addTransition(t);
				lastState = subSkel.getLastState();
				smHead.getInitialActivity().setTf();
				smHead.getInitialActivity().resetSubsequents();
				smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
				setLastActivity(subSkel.getLastAct());
				setCard(skeleton.getCondition(),fcCard);
				smHead.addSub(subSkel.getSMHead());
			}
		};
		I.addTransition(toG);
		initialTrans = new Transition(new TransitionLabel(smHead, When.BEFORE,  Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				smHead.setIndex(i);
				smHead.getInitialActivity().setTi();
			}
		};
	}
	
	Activity getInitialAct() {
		return smHead.getInitialActivity();
	}
	
	Activity getLastAct() {
		return smHead.getLastActivity();
	}

	/*
	 * Sets cardinality of split or codition muscles with rho parameter
	 */
	private void setCard(Muscle<?,?> m, int newCard) {
		double v;
		if (card.containsKey(m)) {
			v = rho*(double)newCard + (1-rho)*(double)card.get(m); 
		} else {
			v = (double)newCard;
		}
		card.put(m, (int)v);
	}
	
	/*
	 * Sets Activity a as last activity 
	 */
	private void setLastActivity(Activity a) {
		a.resetSubsequents();
		for (Activity s: smHead.getLastActivity().getSubsequents()) {
			a.addSubsequent(s);
		}
		smHead.getLastActivity().resetSubsequents();
		smHead.setLastActivity(a);
		
	}
	
	SMHead getSMHead() {
		return smHead;
	}
	
	HashSet<Muscle<?,?>> getMuscles() {
		return muscles;
	}
}
