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

import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Skeleton;

class TransitionLabel implements Comparable<TransitionLabel> {
	static final int VOID = 0;
	static final int INDEX = 1;
	static final int FS_CARD = 2;
	
	private SMHead ts;
	private When when;
	private Where where;
	private boolean cond;
	TransitionLabel(SMHead ts, When when, Where where, boolean cond) {
		this.ts = ts;
		this.when = when;
		this.where = where;
		this.cond = cond;
	}
	boolean isIn(TransitionLabel event) {
		if (!stackComparison(event.ts.getStrace())) return false;
		if (!when.equals(event.when)) return false;
		if (!where.equals(event.where)) return false;
		if (cond != event.cond) return false;
		return true;
	}
	private boolean stackComparison(Stack<Skeleton<?,?>> s) {
		if (ts.getStrace().size() != s.size()) return false;
		for (int i=0; i<s.size(); i++) {
			Skeleton<?,?> s1 = ts.getStrace().get(i);
			Skeleton<?,?> s2 = s.get(i);
			if (!s1.equals(s2)) return false;
		}
		return true;
	}
	@Override
	public int compareTo(TransitionLabel tl) {
		Integer i1 = new Integer(ts.getIndex());
		Integer i2 = new Integer(tl.ts.getIndex());
		return i1.compareTo(i2);
	}
	boolean isTheOne(int eventIndex, int eventParent) {
		if(ts.getDaCParent() != SMHead.UDEF && ts.getDaCParent() != eventParent)
			return false; 
		if(ts.getIndex() == SMHead.UDEF) return true;
		if(eventIndex == ts.getIndex()) return true;		
		return false;
	}
	
	int getType() {
		if (when == When.AFTER && where == Where.SPLIT) return FS_CARD;
		if (when == When.BEFORE) {
			if (where == Where.MERGE ) return VOID;
			{
				Skeleton<?,?> current = ts.getStrace().peek();
				if (current instanceof DaC && where == Where.SPLIT) return VOID;
			}
			return INDEX;
		}
		return VOID;
	}
	SMHead getTs() {
		return ts;
	}
}
