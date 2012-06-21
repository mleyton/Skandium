package cl.niclabs.skandium.autonomic;

import java.util.HashMap;

import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.Skeleton;

class Controller extends GenericListener {
	
	HashMap<Integer,State> globalState;
	
	Controller() {
		globalState = new HashMap<Integer,State>();
	}

	@Override
	public Object handler(Object param, @SuppressWarnings("rawtypes") Skeleton[] strace, int index,
			boolean cond, int parent, When when, Where where) {
		State state;
		if (globalState.containsKey(index)) {
			state = globalState.get(index);
		} else {
			state = newState(strace[strace.length-1],index);
			globalState.put(index, state);
			State parentState = globalState.get(parent);
			ChildSetter cset = new ChildSetter(state);
			parentState.accept(cset);
			//TODO para que esto funcione los index de un mismo skeleton deben ser iguales
		}
		if (when == When.AFTER && where == Where.SKELETON) {
			state.setFinished(true);
			return param;
		}
		//TODO
		return param;
	}
	
	static State newState(Skeleton<?,?> skel, int index) {
		StateGenerator sgen = new StateGenerator(index);
		skel.accept(sgen);
		return sgen.getState();
	}
}
