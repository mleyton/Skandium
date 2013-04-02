package cl.niclabs.skandium.autonomic;

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

class SGenerator implements SkeletonVisitor {

	private Transition initialTrans;
	private State lastState;
	@SuppressWarnings("rawtypes")
	private Stack<Skeleton> strace;
	
	private HashMap<Muscle<?,?>,Long> t;
	private HashMap<Muscle<?,?>, Integer> card;
	private HashSet<Muscle<?,?>> muscles;
	private double rho;
	private SMHead smHead;

	@SuppressWarnings("rawtypes")
	SGenerator(double rho) {
		this.strace = new Stack<Skeleton>();
		this.t = new HashMap<Muscle<?,?>,Long>();
		this.card = new HashMap<Muscle<?,?>,Integer>();
		this.muscles = new HashSet<Muscle<?,?>>();
		this.rho = rho;
	}

	private SGenerator(
			@SuppressWarnings("rawtypes") Stack<Skeleton> trace,
			HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>,Integer> card, double rho,
			HashSet<Muscle<?,?>> muscles) {
		this(rho);
		this.strace.addAll(trace);
		this.t = t;
		this.card = card;
//		this.muscles = muscles; 
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		strace.add(skeleton);
		skeleton.getSubskel().accept(this);
	}

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
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
		smHead.setInitialActivity(stage1.getInitialAct());
		stage1.getLastAct().addSubsequent(stage2.getInitialAct());
		smHead.setLastActivity(stage2.getLastAct());
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getExecute());
		lastState = new State(StateType.F);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
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

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		throw new RuntimeException("No autonomic support for IF Skeleton");
	}

	@Override
	public <P> void visit(final While<P> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getCondition());
		lastState = new State(StateType.F);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
		
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
				smHead.getWhileCurrentActivity().addSubsequent(subSkel.getInitialAct());
				smHead.setWhileCurrentActivity(new Activity(t,cond,rho));
				subSkel.getLastAct().addSubsequent(smHead.getWhileCurrentActivity());
			}
		};
		I.addTransition(toT);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(skeleton);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
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
		}
	}

	@Override
	public <P, R> void visit(final Map<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getSplit());
		muscles.add(skeleton.getMerge());
		lastState = new State(StateType.F);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
		
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
//				initialAct.resetSubsequents();
//				lastAct.resetPredcesors();
				for (int i=0; i<fsCard; i++) {
					SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					S.addTransition(subSkel.getInitialTrans());
					subSkel.getLastState().addTransition(toM);

					smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
					subSkel.getLastAct().addSubsequent(smHead.getLastActivity());
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
/*
				if (card.containsKey(skeleton.getSplit())) {
					int n = card.get(skeleton.getSplit());
					initialAct.resetSubsequents();
					lastAct.resetPredcesors();
					for (int j=0; j<n; j++) {
						SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
						skeleton.getSkeleton().accept(subSkel);
						initialAct.addSubsequent(subSkel.getInitialAct());
						subSkel.getLastAct().addSubsequent(lastAct);						
					}
				}
*/				
			}
		}; 
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		throw new RuntimeException("No autonomic support for MAP Skeleton");

	}

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

	@SuppressWarnings("rawtypes")
	private Skeleton[] getStraceAsArray() {
		return strace.toArray(new Skeleton[strace.size()]);
	}
	
	private <P,R> void DaCM(final DaC<P,R> skeleton, final int fcCard) {
		lastState = new State(StateType.F);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		smHead = new SMHead(straceArray);
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
				for (int i=0; i<fsCard; i++) {
					SGenerator subDaC = new SGenerator(strace,t,card,rho,muscles);
					subDaC.DaCM(skeleton, fcCard+1);
					subDaC.getInitialTrans().tl.getTs().setDaCParent(this.tl.getTs().getIndex());
					T.addTransition(subDaC.getInitialTrans());
					subDaC.getLastState().addTransition(toM);
					splitAct.addSubsequent(subDaC.getInitialAct());
					subDaC.getLastAct().addSubsequent(smHead.getLastActivity());
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
				smHead.getInitialActivity().addSubsequent(subSkel.getInitialAct());
				setLastActivity(subSkel.getLastAct());
				setCard(skeleton.getCondition(),fcCard);
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

	private void setCard(Muscle<?,?> m, int newCard) {
		double v;
		if (card.containsKey(m)) {
			v = rho*(double)newCard + (1-rho)*(double)card.get(m); 
		} else {
			v = (double)newCard;
		}
		card.put(m, (int)v);
	}
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
/*	
	private void setInitialActivity(Activity a) {
		a.resetPredcesors();
		for (Activity p: initialAct.getPredecesors()) {
			a.addPredecesor(p);
		}
		initialAct.resetPredcesors();
		initialAct = a;
	}

	private void estimateWhile(Activity current, int c, While<?> w, Muscle<?,?> m) {
		if(card.containsKey(m)) {
			current.resetSubsequents();
			Activity a = current;
			int n = card.get(m);
			for (int i=c; i<n; i++) {
				SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
				w.getSubskel().accept(subSkel);
				a.addSubsequent(subSkel.getInitialAct());
				a = new Activity(t,m,rho);
				subSkel.getLastAct().addSubsequent(a);
			}
			setLastActivity(a);
		}
	}

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
	HashSet<Muscle<?,?>> getMuscles() {
		return muscles;
	}
	
	HashMap<Muscle<?, ?>, Long> getT() {
		return t;
	}

	HashMap<Muscle<?, ?>, Integer> getCard() {
		return card;
	}
	
	
}
