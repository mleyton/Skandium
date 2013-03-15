package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.While;

class Controller extends GenericListener {
	
	List<State> active;
//	private Skandium skandium;
//	private float yellowThreshold;
//	private float redThreshold;
//	private long lastExecution;
//	private long poolCheck;
	
	Controller(Skeleton<?,?> skel, Skandium skandium, float yellowThreshold, float redThreshold, long poolCheck) {
//		this.skandium = skandium; 
//		this.yellowThreshold = yellowThreshold;
//		this.redThreshold = redThreshold;
//		this.poolCheck = poolCheck;
//		lastExecution = 0;
		//Inicializar maquina de estados
		TransitionGenerator visitor = new TransitionGenerator();
		skel.accept(visitor);
		State I = new State();
		I.addTransition(visitor.getInitialTrans());
		active = new ArrayList<State>();
		active.add(I);
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
		TransitionLabel event = new TransitionLabel(new TransitionSkelIndex(strace),when, where, cond);
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
//		active.remove(fromTransToState.get(t));
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
		active.add(t.getDest());
/*		
		if (System.currentTimeMillis() - lastExecution > poolCheck) {
			threadsControl();
			lastExecution = System.currentTimeMillis();
		}
*/
		return param;
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
			int neededThreads = 0;//root.threadsCalculator();
			if (currThreads > neededThreads) 
				skandium.setMaxThreads(neededThreads);
			return;
		}
		skandium.setMaxThreads(1);
		System.gc();
	}
*/	
}
