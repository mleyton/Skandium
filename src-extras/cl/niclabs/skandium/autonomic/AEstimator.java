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

class AEstimator implements SkeletonVisitor {

	private HashMap<Muscle<?,?>,Long> t;
	private HashMap<Muscle<?,?>, Integer> card;
	private SMHead smHead;
	private HashSet<Muscle<?,?>> muscles;
	private double rho;

	AEstimator(HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, Integer> card, SMHead smHead,
			HashSet<Muscle<?,?>> muscles, double rho) {
		this.t = t;
		this.card = card;
		this.smHead = smHead;
		this.muscles = muscles;
		this.rho = rho;
	}
	
	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		skeleton.getSubskel().accept(this);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		AEstimator stage1 = new AEstimator(t,card,smHead.getSubs().get(0),muscles,rho);
		skeleton.getStage1().accept(stage1);
		AEstimator stage2 = new AEstimator(t,card,smHead.getSubs().get(1),muscles,rho);
		skeleton.getStage2().accept(stage2);
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) { 
		
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		Condition<?> c = skeleton.getCondition();
		if (card.containsKey(c)) {
			int n = card.get(c);
			if (smHead.getCurrentState() == null) {
				smHead.getInitialActivity().resetSubsequents();
				Activity a = smHead.getInitialActivity();
				for (int i=0; i<n; i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),t,card,rho,muscles);
					skeleton.getSubskel().accept(subSkel);
					a.addSubsequent(subSkel.getInitialAct());
					a = new Activity(t,c,rho);
					subSkel.getLastAct().addSubsequent(a);
				}
				setLastActivity(a);
				return;
			}
			if (smHead.getCurrentState().getType()==StateType.I) {
				smHead.getWhileCurrentActivity().resetSubsequents();
				Activity a = smHead.getWhileCurrentActivity();
				for (int i=smHead.getWhileCounter(); i<n; i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),t,card,rho,muscles);
					skeleton.getSubskel().accept(subSkel);
					a.addSubsequent(subSkel.getInitialAct());
					a = new Activity(t,c,rho);
					subSkel.getLastAct().addSubsequent(a);
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
					SGenerator subSkel = new SGenerator(smHead.getStrace(),t,card,rho,muscles);
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

	@Override
	public <P> void visit(For<P> skeleton) {
		for (int i=0; i<skeleton.getTimes(); i++) {
			AEstimator sub = new AEstimator(t,card,smHead.getSubs().get(i),muscles,rho);
			skeleton.getSubskel().accept(sub);
		}
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		Split<?,?> s = skeleton.getSplit();
		if(card.containsKey(s)) {
			if(smHead.getCurrentState() == null || 
					smHead.getCurrentState().getType()==StateType.I) {
				smHead.getInitialActivity().resetSubsequents();
				smHead.getLastActivity().resetPredcesors();
				for (int i=0; i<card.get(s); i++) {
					SGenerator subSkel = new SGenerator(smHead.getStrace(),t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
					subSkel.getLastAct().addSubsequent(smHead.getLastActivity());
				}
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.S) {
				for (int i=0; i<smHead.getSubs().size(); i++) {
					AEstimator sub = new AEstimator(t,card,smHead.getSubs().get(i),muscles,rho);
					skeleton.getSkeleton().accept(sub);
				}
				return;
			}
		}
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

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
					SGenerator subSkel = new SGenerator(smHead.getStrace(),t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
					setLastActivity(subSkel.getLastAct());
					return;
				}
				Activity spl = new Activity(t,s,rho);
				addDaCChildren(fsCard, deep, skeleton, spl);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.C || 
					smHead.getCurrentState().getType()==StateType.S) {
				Activity spl = smHead.getInitialActivity().getSubsequents().get(0);
				addDaCChildren(fsCard, deep, skeleton, spl);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.G) {
				AEstimator subAE = new AEstimator(t, card, smHead.getSubs().get(0), muscles, rho);
				skeleton.getSkeleton().accept(subAE);
				return;
			}
			if(smHead.getCurrentState().getType()==StateType.T) {
				for (SMHead sub : smHead.getSubs()) {
					AEstimator subAE = new AEstimator(t, card, sub, muscles, rho);
					skeleton.accept(subAE);
				}
				return;
			}
		}
	}

	private void addDaCChildren(int fsCard, int deep, DaC<?,?> skeleton, Activity spl) {
		Activity mrg = new Activity(t,skeleton.getMerge(),rho);
		smHead.getInitialActivity().addSubsequent(spl);
		for (int i=0; i<fsCard; i++) {
			SMHead subSM = new SMHead(smHead.getStrace());
			subSM.setInitialActivity(new Activity(t,skeleton.getCondition(),rho));
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

	private void setLastActivity(Activity a) {
		a.resetSubsequents();
		for (Activity s: smHead.getLastActivity().getSubsequents()) {
			a.addSubsequent(s);
		}
		smHead.getLastActivity().resetSubsequents();
		smHead.setLastActivity(a);
		
	}

}
