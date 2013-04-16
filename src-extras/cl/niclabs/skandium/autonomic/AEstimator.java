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

import java.util.HashMap;
import java.util.HashSet;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;

/**
 * This class implements an instance of the visitor pattern over the different
 * types of Skeletons. Its goal is to complete the Dependency Activity Graph 
 * (DAG) with the estimated values of duration and cardinality of activities. 
 * Therefore, this visitor should be called once the estimating variables 
 * (muscles execution time, and condition and split cardinality) are defined.
 * 
 * @author Gustavo Pabón <gustavo.pabon@gmail.com>
 *
 */
class AEstimator implements SkeletonVisitor {

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

	/**
	 * 
	 * @param t Map that holds the function time "t", where t(f) is the 
	 * estimated execution time in nanosecs of muscle f
	 * 
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
	 * @param smHead There is a relation one-to-one between a Skeleton, on the 
	 * nested Skeletons, and a State Machine Header (SMHead).  The smHead holds 
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
	 *    
	 * @param muscles Set of muscles, filled during the State Machine 
	 * generation (SGenerator), it is used for check if the dependency activity
	 * graph is ready to be completed (AEstimator)
	 * 
	 * @param rho parameter that defines the weight of a new actual value for
	 * the calculation of the new estimated value.  The formula is: 
	 * estimated_value = rho*actual_value + (1-rho)*previous_estimated_value
	 */
	AEstimator(HashMap<Muscle<?,?>,Long> t, 
			HashMap<Muscle<?,?>, Integer> card, SMHead smHead,
			HashSet<Muscle<?,?>> muscles, double rho) {
		this.t = t;
		this.card = card;
		this.smHead = smHead;
		this.muscles = muscles;
		this.rho = rho;
	}
	
	/**
	 * In Farm case, the estimation makes a passthru to the nested skeleton of
	 * Farm. 
	 */
	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		skeleton.getSubskel().accept(this);
	}

	/**
	 * In Pipe case, the estimation makes a passthru to the nested skeletons of
	 * Pipe.
	 */
	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		AEstimator stage1 = 
				new AEstimator(t,card,smHead.getSubs().get(0),muscles,rho);
		skeleton.getStage1().accept(stage1);
		AEstimator stage2 = 
				new AEstimator(t,card,smHead.getSubs().get(1),muscles,rho);
		skeleton.getStage2().accept(stage2);
	}

	/**
	 * In Seq case, the DAC is complete, there is nothig to do. 
	 */
	@Override
	public <P, R> void visit(Seq<P, R> skeleton) { 
		
	}

	/**
	 * If skeleton is not supported.
	 */
	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	/**
	 * In While case, we have three possible escenarios:
	 * 1. When the While Skeleton has not initiated its execution. In this case
	 *    The DAG is completed as follows:
	 *    -> c -> AEstimator(SGenerator(subSkel)) ... -> c ->
	 *    where "-> c -> AEstimator(SGenerator(SubSkel))" is repeated as many
	 *    as card(c) times, and AEstimator(SGenerator(subSkel)) creates a 
	 *    estimation of the sub DAG.
	 * 2. If the current state is I (initial) means that the before condition 
	 *    event was raised, and it is pending the after condition event. In 
	 *    this case the DAG is in the middle of the execution, but the new
	 *    instance of subSkel is not started, therefore the DAG is completed as
	 *    follows:
	 *    -> current c -> AEstimator(SGenarator(subSkel)) -> c ... ->
	 *    where "-> AEstimator(SGenarator(subSkel)) -> c" is repeated 
	 *    as many as "card(c) - WhileCounter" times.
	 * 3. If the current state is T (condition returns true) means the last 
	 *    execution of condition skeleton returns true, therefore the nested 
	 *    skeleton is currently executing.  DAG is completed as follows:
	 *    -> executed c -> AEstimator(subSkel) -> c ... ->
	 *    where "-> AEstimator(subSkel) -> c" is repeated as many as 
	 *    "card(c) - WhileCounter" times.
	 *    In this case subSkel is running therefore it already have a SMHead, 
	 *    so it is not necessary to call SGenerator.
	 * 4. Current state is F (final), therefore the DAG is completed.      
	 */
	@Override
	public <P> void visit(While<P> skeleton) {
		Condition<?> c = skeleton.getCondition();
		if (card.containsKey(c)) {
			int n = card.get(c);
			if (smHead.getCurrentState() == null) {
				smHead.getInitialActivity().resetSubsequents();
				Activity a = smHead.getInitialActivity();
				for (int i=0; i<n; i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),
							t,card,rho,muscles);
					skeleton.getSubskel().accept(subSkel);
					AEstimator subAE = new AEstimator(t,card, 
							subSkel.getSMHead(),muscles,rho);
					skeleton.getSubskel().accept(subAE);
					a.addSubsequent(subSkel.getSMHead().getInitialActivity());
					a = new Activity(t,c,rho);
					subSkel.getSMHead().getLastActivity().addSubsequent(a);
				}
				setLastActivity(a);
				return;
			}
			if (smHead.getCurrentState().getType()==StateType.I) {
				smHead.getWhileCurrentActivity().resetSubsequents();
				Activity a = smHead.getWhileCurrentActivity();
				for (int i=smHead.getWhileCounter(); i<n; i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),
							t,card,rho,muscles);
					skeleton.getSubskel().accept(subSkel);
					AEstimator subAE = new AEstimator(t,card, 
							subSkel.getSMHead(),muscles,rho);
					skeleton.getSubskel().accept(subAE);
					a.addSubsequent(subSkel.getSMHead().getInitialActivity());
					a = new Activity(t,c,rho);
					subSkel.getSMHead().getLastActivity().addSubsequent(a);
				}
				setLastActivity(a);			
				return;
			}
			if (smHead.getCurrentState().getType()==StateType.T) {
				SMHead subSM = smHead.getSubs().get(smHead.getWhileCounter()-1);
				AEstimator subaest = new AEstimator(t,card,subSM,muscles,rho);
				skeleton.getSubskel().accept(subaest);
				Activity a = subSM.getLastActivity();
				for (int i=smHead.getWhileCounter(); i<n; i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),
							t,card,rho,muscles);
					skeleton.getSubskel().accept(subSkel);
					a.addSubsequent(subSkel.getInitialAct());
					a = new Activity(t,c,rho);
					subSkel.getLastAct().addSubsequent(a);
				}
				setLastActivity(a);
				return;
			}
		}
	}

	
	/**
	 * In For case, the estimation makes a passthru to the nested skeletons of
	 * For. 
	 */
	@Override
	public <P> void visit(For<P> skeleton) {
		for (int i=0; i<skeleton.getTimes(); i++) {
			AEstimator sub = new AEstimator(t,card,
					smHead.getSubs().get(i),muscles,rho);
			skeleton.getSubskel().accept(sub);
		}
	}

	/**
	 * In Map case there are the following scenarios
	 * 1. The Map execution is not started, or the before split event was 
	 *    raised but the after split event is not (I state).  In this case the 
	 *    DAG is completed as follows:
	 *       -> AEstimator(SGenerator(subSkel)) ->
	 *    s --> ...                             --> m
	 *       -> AEstimator(SGenerator(subSkel)) ->
	 *     where "-> AEstimator(SGenerator(subSkel)) ->" is repeated as many as
	 *     card(s). And AEstimator(SGenerator(subSkel)) produces an estimation
	 *     of the sub-DAGs.
	 * 2. The split muscle was executed and the subSkeletons are in the middle 
	 *    of its execution (S state). In this case the DAG is completed as 
	 *    follows:
	 *       -> AEstimator(subSkel) ->
	 *    s --> ...                 --> m
	 *       -> AEstimator(subSkel) ->
	 *    where "-> AEstimator(subSkel) ->" corresponds to the actual sub DAGs,
	 *    Therefore it is not necessary to create a new SGenerator for them 
	 *    because it was created during the transition from state I to S.
	 * 3. States M (before merge event was raised) and F (final). The DAG is 
	 *    completed, therefore there is nothing to do here.
	 */
	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		Split<?,?> s = skeleton.getSplit();
		if(card.containsKey(s)) {
			if(smHead.getCurrentState() == null || 
					smHead.getCurrentState().getType()==StateType.I) {
				smHead.getInitialActivity().resetSubsequents();
				smHead.getLastActivity().resetPredcesors();
				for (int i=0; i<card.get(s); i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),
							t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					AEstimator subAE = new AEstimator(t,card, 
							subSkel.getSMHead(),muscles,rho);
					skeleton.getSkeleton().accept(subAE);
					smHead.getInitialActivity().addSubsequent(
							subSkel.getSMHead().getInitialActivity());
					subSkel.getSMHead().getLastActivity().addSubsequent(
							smHead.getLastActivity());
				}
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.S) {
				for (int i=0; i<smHead.getSubs().size(); i++) {
					AEstimator sub = new AEstimator(t,card,
							smHead.getSubs().get(i),muscles,rho);
					skeleton.getSkeleton().accept(sub);
				}
				return;
			}
		}
	}

	/**
	 * Fork skeleton is not supported.
	 */
	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}
	
	/**
	 * In DaC case, there are the following scenarios:
	 * 1. The DaC execution has not started, or the before condition event was
	 *    raised (state I). In this scenario there are two alternatives for 
	 *    complete the DAG
	 *    a) This is a leaf on the recursive tree (current deep = card(c))
	 *       -> c -> AEstimator(SGenerator(subSkel)) ->
	 *       where "AEstimator(SGenerator(subSkel))" is the subSkel DAG 
	 *       estimation.
	 *    b) Is a node on the recursive tree (current deep < card(c))
	 *                              
	 *                  -> c (deep+1) -> ... ->
	 *       -> c -> s --> ...              --> m ->
	 *                  -> c (deep+1) -> ... -> 
	 *       where "c (deep+1) -> ..." is repeated as many as card(s) calling
	 *       recursively with deep+1, until it reaches alternative (a).
	 * 2. Condition returned "true" (C state) or before split event was raised
	 *    but after split event is not (S state). This is a very similar case
	 *    than the (1b), the only difference is that the root split activity 
	 *    already exists therefore is not necessary to create it. The rest of 
	 *    activities are the same. 
	 * 3. Condition returned "false" (G state). In this case it is just a 
	 *    passthru to the subSkel estimation.
	 * 4. After split event was raised (T state) there fore its sub DaC are
	 *    in the middle of its own execution. It is just a passthru to the
	 *    estimation of the sub DaCs
	 * 5. Before merge event was raised (M state) or after merge was raised (F
	 *    state).  The DAG is complete, nothing to estimate!
	 */
	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		Split<?,?> s = skeleton.getSplit();
		Condition<?> c = skeleton.getCondition();
		if(card.containsKey(s) && card.containsKey(c)) {
			int fsCard = card.get(s);
			int fcCard = card.get(c);
			int deep = smHead.getDaCDeep();
			if(smHead.getCurrentState() == null || 
					smHead.getCurrentState().getType()==StateType.I) {
				smHead.getInitialActivity().resetSubsequents();
				if (deep >= fcCard) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),
							t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					AEstimator subAE = new AEstimator(t,card, 
							subSkel.getSMHead(),muscles,rho);
					skeleton.getSkeleton().accept(subAE);
					smHead.getInitialActivity().addSubsequent(
							subSkel.getSMHead().getInitialActivity());
					setLastActivity(subSkel.getSMHead().getLastActivity());
					return;
				}
				Activity spl = new Activity(t,s,rho);
				addDaCChildren(fsCard, deep, skeleton, spl);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.C || 
					smHead.getCurrentState().getType()==StateType.S) {
				Activity spl = 
						smHead.getInitialActivity().getSubsequents().get(0);
				addDaCChildren(fsCard, deep, skeleton, spl);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.G) {
				AEstimator subAE = new AEstimator(t, card, 
						smHead.getSubs().get(0), muscles, rho);
				skeleton.getSkeleton().accept(subAE);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.T) {
				for (SMHead sub : smHead.getSubs()) {
					AEstimator subAE = new AEstimator(t, 
							card, sub, muscles, rho);
					skeleton.accept(subAE);
				}
				return;
			}
		}
	}

	/*
	 * This private method has the common code for the DaC 1b and 2 scenarios
	 */
	private void addDaCChildren(int fsCard, int deep, DaC<?,?> skeleton,
			Activity spl) {
		Activity mrg = new Activity(t,skeleton.getMerge(),rho);
		smHead.getInitialActivity().addSubsequent(spl);
		for (int i=0; i<fsCard; i++) {
			SMHead subSM = new SMHead(smHead.getStrace());
			subSM.setInitialActivity(
					new Activity(t,skeleton.getCondition(),rho));
			subSM.setLastActivity(new Activity(t,skeleton.getMerge(),rho));
			subSM.setDaCParent(smHead.getIndex());
			subSM.setDaCDeep(deep+1);
			AEstimator subAE = new AEstimator(t,card,subSM,muscles,rho);
			skeleton.accept(subAE);
			spl.addSubsequent(subSM.getInitialActivity());
			subSM.getLastActivity().addSubsequent(mrg);
		}
		setLastActivity(mrg);
	}
	
	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	/*
	 * This private method is used for setting Activity a as last activity.
	 */
	private void setLastActivity(Activity a) {
		a.resetSubsequents();
		for (Activity s: smHead.getLastActivity().getSubsequents()) {
			a.addSubsequent(s);
		}
		smHead.getLastActivity().resetSubsequents();
		smHead.setLastActivity(a);
		
	}

}
