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

class Controller extends GenericListener {
	
	List<State> active;
	Activity initialAct;
	private Skandium skandium;
	private long lastExecution;
	private long poolCheck;
	private HashMap<Muscle<?,?>,Long> t;
	private HashMap<Muscle<?,?>, Integer> card;
	private HashSet<Muscle<?,?>> muscles;
	private SMHead smHead;
	private double rho;
	private Skeleton<?,?> skel;
	private long wallClocktimeGoal;
	private int threadLimit;
	private boolean verboseMode;
	
	Controller(Skeleton<?,?> skel, Skandium skandium, long poolCheck, 
			HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, Integer> card, double rho,
			long wallClocktimeGoal, int threadLimit, boolean verboseMode) {
		this.skandium = skandium; 
		this.poolCheck = poolCheck;
		this.threadLimit = threadLimit;
		this.verboseMode = verboseMode;
		this.t = t;
		this.card = card;
		this.wallClocktimeGoal = wallClocktimeGoal;
		lastExecution = 0;
		//Inicializar maquina de estados
		SGenerator visitor = new SGenerator(t, card, rho);
		skel.accept(visitor);
		State I = new State(StateType.I);
		I.addTransition(visitor.getInitialTrans());
		active = new ArrayList<State>();
		active.add(I);
		initialAct = visitor.getInitialAct();
		Activity lastAct = visitor.getLastAct();
		lastAct.addSubsequent(initialAct);
		muscles = visitor.getMuscles();
		smHead = visitor.getSMHead();
		this.rho = rho;
		this.skel = skel;
	}

	@Override
	public boolean guard(Object param, @SuppressWarnings("rawtypes") Skeleton[] strace, int index,
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
	
	@Override
	synchronized public Object handler(Object param, @SuppressWarnings("rawtypes") Skeleton[] strace, int index,
			boolean cond, int parent, When when, Where where) {
		Stack<Skeleton<?,?>> trace = new Stack<Skeleton<?,?>>();
		for(Skeleton<?,?> s: strace) trace.add(s);
		TransitionLabel event = new TransitionLabel(new SMHead(trace),when, where, cond);
		HashMap<Transition,State> fromTransToState = new HashMap<Transition,State>();
		// Buscar evento entre transiciones de estados activos.
		PriorityQueue<Transition> areIn = new PriorityQueue<Transition>() ;
		for (State s: active) {
			for(Transition t: s.getTransitions(event)) {
				areIn.add(t);
				fromTransToState.put(t, s);
			}
		}
		Transition t = null;
		{
			boolean found = false;
			while (found == false && !areIn.isEmpty()) {
				t = areIn.poll();
				if (t.isTheOne(index,parent)) found=true;
			}
			if(found==false) throw new RuntimeException("Event not expected, should not be here! " +
			strace[strace.length-1] + " " + when + " " + where + " " + cond + "" + index);
		}
		fromTransToState.get(t).remove(active, t);
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

		t.setCurrentState();
		active.add(t.getDest());
		if (System.currentTimeMillis() - lastExecution > poolCheck) {
			if (isActivityDiagramReady()) {
				threadsControl();
			}
		}

		return param;
	}
	boolean isLastActivity(Activity a) {
		for (Activity s: a.getSubsequents()) {
			if (s == initialAct) return true;
		}
		return false;
	}
	private boolean isActivityDiagramReady() {
		for (Muscle<?,?> m:muscles) {
			if (!t.containsKey(m)) return false;
			if ((m instanceof Condition<?>)&&(!card.containsKey(m))) return false;
			if ((m instanceof Split<?,?>)&&(!card.containsKey(m))) return false;
		}
		return true;
	}
	void printT() {
		System.out.println("Estimated execution times (ms)");
		for (Muscle<?,?> m:t.keySet()) {
			System.out.println(m.getClass().getName() + "\t" + ((long)t.get(m).longValue()/1000000));
		}
	}
	void printCard() {
		System.out.println("Estimated cardinality");
		for (Muscle<?,?> m:card.keySet()) {
			System.out.println(m.getClass().getName() + "\t" + card.get(m).intValue());
		}
	}
	
	private void threadsControl() {
		AEstimator aest = new AEstimator(this.t,card,smHead,muscles,rho);
		skel.accept(aest);
		
		Activity beAct = initialAct.copyForward(this);
		TimeLine beTL = new TimeLine();
		Box<Long> beCurr = new Box<Long>((long)0);
		Box<Long> beMax = new Box<Long>((long)0);
		beAct.bestEffortFillForward(beTL, beCurr, beMax);
		int beMaxThreads = beTL.maxThreads(beCurr.get());
		beMaxThreads = beMaxThreads == 0? 1: beMaxThreads;

		Activity fifoAct = initialAct.copyForward(this);
		TimeLine fifoTL = new TimeLine();
		Box<Long> fifoMax = new Box<Long>((long)0);
		int testThreads = skandium.getMaxThreads() == 1? 1: skandium.getMaxThreads()/2;
		fifoAct.fifoFillForward(fifoTL, fifoMax, testThreads);

		long elapsedTime = beCurr.get() - initialAct.getTi();
		long beTimeLeft = beMax.get() - beCurr.get();
		long fifoTimeLeft = fifoMax.get() - beCurr.get();
		long goal = wallClocktimeGoal - elapsedTime;
		if (goal >= fifoTimeLeft) {
			skandium.setMaxThreads(testThreads);
		} else {
			skandium.setMaxThreads(beMaxThreads > threadLimit? threadLimit : beMaxThreads);
		}

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
