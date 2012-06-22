package cl.niclabs.skandium.autonomic;

import java.util.HashMap;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;

class Controller extends GenericListener {
	
	HashMap<Integer,State> global;
	State root;
	Skandium skandium;
	float yellowThreshold;
	float redThreshold;
	long lastExecution;
	long poolCheck;
	
	Controller(Skandium skandium, float yellowThreshold, float redThreshold, long poolCheck) {
		global = new HashMap<Integer,State>();
		this.skandium = skandium; 
		this.yellowThreshold = yellowThreshold;
		this.redThreshold = redThreshold;
		this.poolCheck = poolCheck;
		lastExecution = 0;
	}

	@Override
	synchronized public Object handler(Object param, @SuppressWarnings("rawtypes") Skeleton[] strace, int index,
			boolean cond, int parent, When when, Where where) {
		/* get state */
		State state;
		if (global.containsKey(index)) {
			state = global.get(index);
		} else {
			state = newState(strace[strace.length-1],index);
			global.put(index, state);
			if (parent == 0) {
				root = state;
			} else {
				State parentState = global.get(parent);
				parentState.childSetter(state);
			}
		}		
		/* Update State */
		if (when == When.AFTER && where == Where.SKELETON) {
			state.setFinished(true);
		}
		if (System.currentTimeMillis() - lastExecution > poolCheck) {
			threadsControl();
			lastExecution = System.currentTimeMillis();
		}
		return param;
	}
	
	static private State newState(Skeleton<?,?> skel, int index) {
		StateGenerator sgen = new StateGenerator(index);
		skel.accept(sgen);
		return sgen.getState();
	}
	
	private void threadsControl() {
		long currMemory = Runtime.getRuntime().totalMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		float consumption = (float) currMemory / maxMemory;
/*
		System.out.println(
				System.currentTimeMillis() + "\t" + 
				Thread.activeCount() + "\t" +
				consumption + "\t" +
				yellowThreshold + "\t" +
				redThreshold);
*/
		if (consumption < yellowThreshold) {
			int currThreads = Thread.activeCount();
			int neededThreads = root.threadsCalculator();
			if (2*currThreads < neededThreads) {
				skandium.setMaxThreads(2*currThreads);
				return;
			}
			skandium.setMaxThreads(neededThreads);
			return;
		}
		if (consumption < redThreshold) {
			int currThreads = Thread.activeCount();
			int neededThreads = root.threadsCalculator();
			if (currThreads > neededThreads) 
				skandium.setMaxThreads(neededThreads);
			return;
		}
		skandium.setMaxThreads(1);
		System.gc();
	}
	
}
