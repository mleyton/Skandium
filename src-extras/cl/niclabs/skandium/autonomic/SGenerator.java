package cl.niclabs.skandium.autonomic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
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
	private Activity initialAct;
	private Activity lastAct;
	private double rho;

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
		this.muscles = muscles; 
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
		initialAct = stage1.getInitialAct();
		stage1.getLastAct().addSubsequent(stage2.getInitialAct());
		lastAct = stage2.getLastAct();
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getExecute());
		lastState = new State();
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		initialAct = new Activity(t,skeleton.getExecute(),rho);
		lastAct = initialAct;
		final TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.SKELETON, false),lastState) {
			@Override
			protected void execute() {
				initialAct.setTf();
			}
		};
		State I = new State();
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE, Where.SKELETON, false),I) {
			@Override
			protected void execute(int i) {
				tsi.setIndex(i);
				initialAct.setTi();
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
		lastState = new State();
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		
		final Condition<?> cond = skeleton.getCondition();
		initialAct = new Activity(t,cond,rho);
		lastAct = new Activity(t,cond,rho);
		final Box<Activity> currentAct = new Box<Activity>(initialAct);
		final Box<Integer> c = new Box<Integer>(0);
		
		final TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, false),lastState) {
			@Override
			protected void execute() {
				setLastActivity(currentAct.get());
				lastAct.setTf();
				setCard(cond,c.get());
			}
		};
		State I = new State();
		I.addTransition(toF);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE, Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				tsi.setIndex(i);
				initialAct.setTi();
				estimateWhile(initialAct, 0, skeleton, cond);
			}
		};
		final Transition toI = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				currentAct.get().setTi();
				estimateWhile(currentAct.get(), c.get(), skeleton, cond);
			}
		};
		final State T = new State();
		Transition toT = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, true),T) {
			@Override
			protected void execute() {
				SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
				skeleton.getSubskel().accept(subSkel);
				T.addTransition(subSkel.getInitialTrans());
				subSkel.getLastState().addTransition(toI);
				currentAct.get().setTf();
				c.set(c.get()+1);
				currentAct.get().addSubsequent(subSkel.getInitialAct());
				currentAct.set(new Activity(t,cond,rho));
				subSkel.getLastAct().addSubsequent(currentAct.get());
				estimateWhile(currentAct.get(), c.get(), skeleton, cond);
			}
		};
		I.addTransition(toT);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(skeleton);
		State lastLastState = null;
		Activity lastLastAct = null;
		int n = skeleton.getTimes();
		for (int i=0; i<n; i++) {
			SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
			skeleton.getSubskel().accept(subSkel);
			if (lastLastState == null) {
				initialTrans = subSkel.getInitialTrans();
				initialAct = subSkel.getInitialAct();
			} else {
				lastLastState.addTransition(subSkel.getInitialTrans());
				lastLastAct.addSubsequent(subSkel.getInitialAct());
			}
			lastLastState = subSkel.getLastState();
			lastLastAct = subSkel.getLastAct();
			if (i==n-1) {
				lastState = lastLastState;
				lastAct = lastLastAct;
			}
		}
	}

	@Override
	public <P, R> void visit(final Map<P, R> skeleton) {
		strace.add(skeleton);
		muscles.add(skeleton.getSplit());
		muscles.add(skeleton.getMerge());
		lastState = new State();
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		
		initialAct = new Activity(t,skeleton.getSplit(),rho);
		lastAct = new Activity(t,skeleton.getMerge(),rho);
		
		final TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.MERGE, false),lastState) {
			@Override
			protected void execute() {
				lastAct.setTf();
			}
		};
		State M = new State();
		M.addTransition(toF);
		final Transition toM = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.MERGE, false),M) {
			@Override
			protected void execute() {
				lastAct.setTi();
			}
		};
		final State S = new State(true);
		Transition toS = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.SPLIT, false),S) {
			@Override
			protected void execute(int fsCard) {
				initialAct.setTf();
				setCard(skeleton.getSplit(), fsCard);
				initialAct.resetSubsequents();
				lastAct.resetPredcesors();
				for (int i=0; i<fsCard; i++) {
					SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
					skeleton.getSkeleton().accept(subSkel);
					S.addTransition(subSkel.getInitialTrans());
					subSkel.getLastState().addTransition(toM);

					initialAct.addSubsequent(subSkel.getInitialAct());
					subSkel.getLastAct().addSubsequent(lastAct);
				}
			}
		};
		State I = new State();
		I.addTransition(toS);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.SPLIT, false),I) {
			@Override
			protected void execute(int i) {
				tsi.setIndex(i);
				initialAct.setTi();
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
		lastState = new State();
		initialAct = new Activity(t, skeleton.getCondition(), rho);		
		final Activity splitAct = new Activity(t, skeleton.getSplit(), rho);
		lastAct = new Activity(t, skeleton.getMerge(), rho);
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		final TransitionSkelIndex tsi = new TransitionSkelIndex(straceArray);
		Transition toF = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.MERGE, false),lastState) {
			@Override
			protected void execute() {
				lastAct.setTf();
			}
		};
		State M = new State();
		M.addTransition(toF);
		final Transition toM = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.MERGE, false),M) {
			@Override
			protected void execute() {
				lastAct.setTi();
			}
		};		
		final State T = new State(true);
		Transition toT = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.SPLIT, false),T) {
			@Override
			protected void execute(int fsCard) {
				for (int i=0; i<fsCard; i++) {
					SGenerator subDaC = new SGenerator(strace,t,card,rho,muscles);
					subDaC.DaCM(skeleton, fcCard+1);
					subDaC.getInitialTrans().tl.getTs().setParent(this.tl.getTs().getIndex());
					T.addTransition(subDaC.getInitialTrans());
					subDaC.getLastState().addTransition(toM);
					splitAct.addSubsequent(subDaC.getInitialAct());
					subDaC.getLastAct().addSubsequent(lastAct);
				}
				splitAct.setTf();
				setCard(skeleton.getSplit(), fsCard);
			}
		};
		State S = new State();
		S.addTransition(toT);
		Transition toS = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.SPLIT, false),S) {
			@Override
			protected void execute() {
				splitAct.setTi();
			}
		};
		State C = new State();
		C.addTransition(toS);
		Transition toC = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, true),C) {
			@Override
			protected void execute() {
				initialAct.setTf();
				initialAct.addSubsequent(splitAct);
			}
		};
		State I = new State();
		I.addTransition(toC);
		final State G = new State();
		Transition toG = new Transition(new TransitionLabel(tsi, When.AFTER,  Where.CONDITION, false),G) {
			@Override
			protected void execute() {
				SGenerator subSkel = new SGenerator(strace,t,card,rho,muscles);
				skeleton.getSkeleton().accept(subSkel);
				G.addTransition(subSkel.getInitialTrans());
				for (Transition t: lastState.getTransitions())
					subSkel.getLastState().addTransition(t);
				lastState = subSkel.getLastState();
				initialAct.setTf();
				initialAct.addSubsequent(subSkel.getInitialAct());
				setLastActivity(subSkel.getLastAct());
				setCard(skeleton.getCondition(),fcCard);
			}
		};
		I.addTransition(toG);
		initialTrans = new Transition(new TransitionLabel(tsi, When.BEFORE,  Where.CONDITION, false),I) {
			@Override
			protected void execute(int i) {
				tsi.setIndex(i);
				initialAct.setTi();
				// TODO estimateDaC(When.BEFORE,  Where.CONDITION, skeleton, fcCard);
			}
		};
	}
	Activity getInitialAct() {
		return initialAct;
	}
	Activity getLastAct() {
		return lastAct;
	}
	boolean isActivityDiagramReady() {
		for (Muscle<?,?> m:muscles) {
			if (!t.containsKey(m)) return false;
			if ((m instanceof Condition<?>)&&(!card.containsKey(m))) return false;
			if ((m instanceof Split<?,?>)&&(!card.containsKey(m))) return false;
		}
		return true;
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
		for (Activity s: lastAct.getSubsequents()) {
			a.addSubsequent(s);
		}
		lastAct.resetSubsequents();
		lastAct = a;
		
	}
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
*/
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
	// TODO borrar get muscles, T y card.
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
