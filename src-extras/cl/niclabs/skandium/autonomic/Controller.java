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
//	private Skandium skandium;
//	private float yellowThreshold;
//	private float redThreshold;
//	private long lastExecution;
//	private long poolCheck;
	private HashMap<Muscle<?,?>,Long> t;
	private HashMap<Muscle<?,?>, Integer> card;
	private HashSet<Muscle<?,?>> muscles;
	private SMHead smHead;
	private double rho;
	private Skeleton<?,?> skel;
	
	Controller(Skeleton<?,?> skel, Skandium skandium, float yellowThreshold, float redThreshold, long poolCheck, double rho) {
//		this.skandium = skandium; 
//		this.yellowThreshold = yellowThreshold;
//		this.redThreshold = redThreshold;
//		this.poolCheck = poolCheck;
//		lastExecution = 0;
		//Inicializar maquina de estados
		SGenerator visitor = new SGenerator(rho);
		skel.accept(visitor);
		State I = new State(StateType.I);
		I.addTransition(visitor.getInitialTrans());
		active = new ArrayList<State>();
		active.add(I);
		initialAct = visitor.getInitialAct();
		Activity lastAct = visitor.getLastAct();
		lastAct.addSubsequent(initialAct);
		t = visitor.getT();
		card = visitor.getCard();
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
/*		
		if (System.currentTimeMillis() - lastExecution > poolCheck) {
			threadsControl();
			lastExecution = System.currentTimeMillis();
		}
*/
//		if (isActivityDiagramReady()) {
			AEstimator aest = new AEstimator(this.t,card,smHead,muscles,rho);
			skel.accept(aest);
//		}

// TODO BORRAR PRUEBA DE ACTIVIDADES
		System.out.println("ACTIVITIES");
		printActivities(initialAct, new HashSet<Activity>());
		printT();
		printCard();
		System.out.println("Is ready: " + isActivityDiagramReady());
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
	// TODO: borrar printActivities, printT, y printCard,
	void printActivities(Activity a, HashSet<Activity> p) {
		if (p.contains(a)) return;
		p.add(a);
		String sub = new String();
		for (Activity s: a.getSubsequents()) {
			sub += "\t" + s;
		}
		boolean isLA = isLastActivity(a);
		System.out.println(a + "\t" + a.getTi() + "\t" + a.getTf() + "\t" + a.getMuscle() + sub + "\t" + isLA);
		if (!isLA) {
			for (Activity s: a.getSubsequents()) {
				printActivities(s,p);
			}
		}
	}
	void printT() {
		System.out.println("T(f)");
		for (Muscle<?,?> m:t.keySet()) {
			System.out.println(m + "\t" + t.get(m).longValue());
		}
	}
	void printCard() {
		System.out.println("Card(f)");
		for (Muscle<?,?> m:card.keySet()) {
			System.out.println(m + "\t" + card.get(m).intValue());
		}
	}
/*	
	private void threadsControl() {
		long currMemory = Runtime.getRuntime().totalMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		float consumption = (float) currMemory / maxMemory;
		if (consumption < yellowThreshold) {
			int currThreads = Thread.activeCount();
			int neededThreads = 0; //= root.threadsCalculator();
			if (2*currThreads < neededThreads) {
				skandium.setMaxThreads(2*currThreads);
				return;
			}
			skandium.setMaxThreads(neededThreads);
			return;
		}
		if (consumption < redThreshold) {
			int currThreads = Thread.activeCount();
			int neededThreads = 0;/root.threadsCalculator();
			if (currThreads > neededThreads) 
				skandium.setMaxThreads(neededThreads);
			return;
		}
		skandium.setMaxThreads(1);
		System.gc();
	}
*/	

}
