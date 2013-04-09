package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.Stack;

import cl.niclabs.skandium.skeletons.Skeleton;

class SMHead {
	static final int UDEF = -1;
	
	private Stack<Skeleton<?,?>> strace;
	private int index;
	private int dacParent;
	
	private State currState;
	private Activity initial;
	private Activity last;
	
	private int whileCounter;
	private Activity whileCurrentActivity;
	
	private ArrayList<SMHead> subs;
	
	SMHead(Stack<Skeleton<?,?>> strace) {
		this.strace = strace;
		index = UDEF;
		dacParent = UDEF;
		currState = null;
		subs = new ArrayList<SMHead>();
	}
	void setIndex(int index) {
		this.index = index;
	}
	void setCurrentState(State currState) {
		this.currState = currState;
	}
	void setDaCParent(int parent) {
		this.dacParent = parent;
	}
	void setInitialActivity(Activity initial) {
		this.initial = initial;
	}
	void setLastActivity(Activity last) {
		this.last = last;
	}
	void setWhileCounter(int c) {
		this.whileCounter = c;
	}
	void setWhileCurrentActivity(Activity a) {
		this.whileCurrentActivity = a;
	}
	int getWhileCounter() {
		return whileCounter;
	}
	Activity getWhileCurrentActivity() {
		return whileCurrentActivity;
	}
	Activity getInitialActivity() {
		return initial;
	}
	Activity getLastActivity() {
		return last;
	}
	Stack<Skeleton<?,?>> getStrace() {
		return strace;
	}
	int getIndex() {
		return index;
	}
	int getDaCParent() {
		return dacParent;
	}
	State getCurrentState() {
		return currState;
	}
	void addSub(SMHead sub) {
		subs.add(sub);
	}
	ArrayList<SMHead> getSubs() {
		return subs;
	}
}
