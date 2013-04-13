/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2013 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	
	private int dacDeep;
	
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
	void setDaCDeep(int deep) {
		this.dacDeep = deep;
	}
	int getDaCDeep() {
		return this.dacDeep;
	}
}
