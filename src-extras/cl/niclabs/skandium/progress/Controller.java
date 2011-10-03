package cl.niclabs.skandium.progress;

import java.util.Stack;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

class Controller {
	private int i;
	private ProgressListener l;
	Controller(ProgressListener l) {
		i=0;
		this.l = l;
	}
	private void fireMuscleExecuted() {
		i++;
		l.handler(i, 0, 0);
	}
	synchronized void seqm(Execute<?, ?> e) {
		fireMuscleExecuted();
	}

	synchronized void ifm(Condition<?> c, boolean cond) {
		fireMuscleExecuted();
	}

	synchronized void whilem(Condition<?> c, int index, boolean cond) {
		fireMuscleExecuted();
	}

	synchronized void mapm(Split<?,?> s) {
		fireMuscleExecuted();
	}
	
	synchronized void mapm(Merge<?,?> m) {
		fireMuscleExecuted();
	}

	synchronized void forkm(Split<?,?> s) {
		fireMuscleExecuted();
	}
	
	synchronized void forkm(Merge<?,?> m) {
		fireMuscleExecuted();
	}
	
	synchronized void dacm(Condition<?> c, Stack<Integer> rbranch, boolean cond) {
		fireMuscleExecuted();
	}
	
	synchronized void dacm(Split<?,?> s, Stack<Integer> rbranch, int plen) {
		fireMuscleExecuted();
	}
	
	synchronized void dacm(Merge<?,?> m, Stack<Integer> rbranch) {
		fireMuscleExecuted();
	}
	
}
