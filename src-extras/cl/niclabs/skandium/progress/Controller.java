package cl.niclabs.skandium.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

class Controller {
	private int i;
	private ProgressListener l;
	private ArrayList<Stack<Muscli>> sigma;
	
	Controller(ProgressListener l,Skeleton<?,?> skel, HashMap<Muscle<?,?>,Estimator> estimators) {
		i=0;
		this.l = l;
		MuscliStackBuilder initial = new MuscliStackBuilder(estimators);
		skel.accept(initial);
		Stack<Muscli> newStack = initial.getStack();
		sigma = new ArrayList<Stack<Muscli>>();
		sigma.add(newStack);
		l.handler(0, newStack.size() + AbstractMuscli.ub(initial.getStack()), newStack.size() + AbstractMuscli.lb(initial.getStack()));
	}
	private void fireMuscleExecuted(Muscli m, Stack<Muscli> s) {
		i++;
		List<Stack<Muscli>> children = new ArrayList<Stack<Muscli>>();
		m.reduce(s,children);
		if (children.size()>0) {
			sigma.addAll(children);
		}
		if (s.size() == 0) {
			sigma.remove(s);
		}
		int slen = 0;
		int ub = 0;
		int lb = 0;
		for (Stack<Muscli> t:sigma) {
			slen += t.size();
			ub += AbstractMuscli.ub(t);
			lb += AbstractMuscli.lb(t);
		}
		l.handler(i, slen + ub, slen + lb);
	}
	private Stack<Muscli> search(int hc) {
		Stack<Muscli> r = new Stack<Muscli>();
		boolean found = false;
		int i=0;
		while (!found && i<sigma.size()) {
			Stack<Muscli> s = sigma.get(i);
			if (s.peek().hashCode() == hc) {
				found = true;
				r = s;
			}
			i++;
		}
		if (!found) {
			throw new RuntimeException("Muscle search internal error!");			
		}
		return r;
	}
	synchronized void seqm(SkeletonTraceElement[] strace, Execute<?, ?> e) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,e));
		Muscli m = s.pop();
		fireMuscleExecuted(m,s);
	}

	synchronized void ifm(SkeletonTraceElement[] strace, Condition<?> c, boolean cond) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,c));
		IfCMuscli m = (IfCMuscli) s.pop();
		m.setCond(cond);
		fireMuscleExecuted(m,s);
	}
	
	synchronized void whilem(SkeletonTraceElement[] strace, Condition<?> c, int index, boolean cond, Object p) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,c));
		WhileCMuscli m = (WhileCMuscli) s.pop();
		m.setIndex(index);
		m.setCond(cond);
		m.setP(p);
		fireMuscleExecuted(m,s);
	}

	synchronized void mapm(SkeletonTraceElement[] strace, Split<?,?> sp, int plen) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,sp));
		MapSMuscli m = (MapSMuscli) s.pop();
		m.setPlen(plen);
		fireMuscleExecuted(m,s);
	}

	synchronized void mapm(SkeletonTraceElement[] strace, Merge<?,?> me) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,me));
		Muscli m = s.pop();
		fireMuscleExecuted(m,s);
	}

	synchronized void forkm(SkeletonTraceElement[] strace, Split<?,?> sp) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,sp));
		Muscli m = s.pop();
		fireMuscleExecuted(m,s);
	}
	
	synchronized void forkm(SkeletonTraceElement[] strace, Merge<?,?> me) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,me));
		Muscli m = s.pop();
		fireMuscleExecuted(m,s);
	}
	
	synchronized void dacm(SkeletonTraceElement[] strace, Condition<?> c, Stack<Integer> rbranch, boolean cond, Object p) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,c));
		DaCCMuscli m = (DaCCMuscli) s.pop();
		m.setRbranch(rbranch);
		m.setCond(cond);
		m.setP(p);
		fireMuscleExecuted(m,s);
	}
	
	synchronized void dacm(SkeletonTraceElement[] strace, Split<?,?> sp, Stack<Integer> rbranch, Object[] p) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,sp));
		DaCSMuscli m = (DaCSMuscli) s.pop();
		m.setRbranch(rbranch);
		m.setP(p);
		fireMuscleExecuted(m,s);
	}
		
	synchronized void dacm(SkeletonTraceElement[] strace, Merge<?,?> me) {
		Stack<Muscli> s = search(AbstractMuscli.hashCode(strace,me));
		Muscli m = s.pop();
		fireMuscleExecuted(m,s);
	}
	
}
