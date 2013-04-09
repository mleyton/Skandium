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
		// TODO Auto-generated method stub
/*
	private void estimateDaC(When when, Where where, DaC<?,?> skeleton, int deep) {
		if (card.containsKey(skeleton.getCondition()) && card.containsKey(skeleton.getSplit())) {
			int cardFc = card.get(skeleton.getCondition());
			int cardFs = card.get(skeleton.getSplit());
			Box<Activity> ini = new Box<Activity>(null);
			Box<Activity> las = new Box<Activity>(null);
			estimatedDaCR(cardFc,cardFs,skeleton,deep,ini,las);
			if (when==When.BEFORE && where==Where.CONDITION) {
				setInitialActivity(ini.get());
				setLastActivity(las.get());
				return;
			}
			if ((when==When.AFTER && where==Where.CONDITION) ||
				(when==When.BEFORE && where==Where.SPLIT)) {
				
			}
		}
	}
	
	private void estimatedDaCR(int cardFc, int cardFs, DaC<?,?> skeleton, int deep, Box<Activity> ini, Box<Activity> las) {
		ini.set(new Activity(t,skeleton.getCondition(),rho));
		if (deep == cardFc) {
			SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
			skeleton.getSkeleton().accept(subSkel);
			ini.get().addSubsequent(subSkel.getInitialAct());
			las.set(subSkel.getLastAct());
			return;
		}
		Activity spl = new Activity(t,skeleton.getSplit(),rho);
		ini.get().addSubsequent(spl);
		las.set(new Activity(t,skeleton.getMerge(),rho));
		for (int i=0; i<cardFs; i++) {
			Box<Activity> subini = new Box<Activity>(null);
			Box<Activity> sublas = new Box<Activity>(null);
			estimatedDaCR(cardFc, cardFs, skeleton, deep+1, subini, sublas);
			spl.addSubsequent(subini.get());
			sublas.get().addSubsequent(las.get());
		}
	}

 */
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
