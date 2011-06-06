/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2011 NIC Labs, Universidad de Chile.
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
package cl.niclabs.skandium.instructions;

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.events.BooleanParamListener;
import cl.niclabs.skandium.system.events.SkandiumEventListener;
import cl.niclabs.skandium.system.events.IntegerBooleanParamListener;
import cl.niclabs.skandium.system.events.IntegerParamListener;
import cl.niclabs.skandium.system.events.NoParamListener;
import cl.niclabs.skandium.system.events.RBranchBooleanParamListener;
import cl.niclabs.skandium.system.events.RBranchParamListener;
import cl.niclabs.skandium.system.events.UndefinedParamListener;


/**
 * 
 * @author gpabon
 */

public class EventInst extends AbstractInstruction {
	
	When when;
	Where where;
	Object[] params;

	/**
	 * The main constructor.
	 */
	public EventInst(When when, Where where, Skeleton<?,?>[] strace, Object... params){
		super(strace);
		this.when = when;
		this.where = where;
		this.params = params;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		Skeleton<?,?> curr = strace[strace.length-1];
		SkandiumEventListener[] listeners = ((AbstractSkeleton<?,?>) curr).getListeners(when, where);
		for (SkandiumEventListener l : listeners) {
			if (l instanceof NoParamListener<?>) {
				if (((NoParamListener<P>) l).guard(param, strace)) {
					param = ((NoParamListener<P>) l).handler(param, strace);
				}
			} else if (l instanceof IntegerParamListener<?>) {
				if (((IntegerParamListener<P>) l).guard(param, strace, (Integer) params[0])) {
					param = ((IntegerParamListener<P>) l).handler(param, strace, (Integer) params[0]);
				}
			} else if (l instanceof BooleanParamListener<?>) {
				if (((BooleanParamListener<P>) l).guard(param, strace, (Boolean) params[0])) {
					param = ((BooleanParamListener<P>) l).handler(param, strace, (Boolean) params[0]);
				}
			} else if (l instanceof IntegerBooleanParamListener<?>) {
				if (((IntegerBooleanParamListener<P>) l).guard(param, strace, (Integer) params[0], (Boolean) params[1])) {
					param = ((IntegerBooleanParamListener<P>) l).handler(param, strace, (Integer) params[0], (Boolean) params[1]);
				}
			} else if (l instanceof RBranchParamListener<?>) {
				Stack<Integer> rbranch =(Stack<Integer>) params[0];
				if (((RBranchParamListener<P>) l).guard(param, strace, (Integer[]) (rbranch.toArray(new Integer[rbranch.size()])))) {
					param = ((RBranchParamListener<P>) l).handler(param, strace, (Integer[]) (rbranch.toArray(new Integer[rbranch.size()])));
				}
			} else if (l instanceof RBranchBooleanParamListener<?>) {
				Stack<Integer> rbranch =(Stack<Integer>) params[0];
				if (((RBranchBooleanParamListener<P>) l).guard(param, strace, (Integer[]) (rbranch.toArray(new Integer[rbranch.size()])), (Boolean) params[1])) {
					param = ((RBranchBooleanParamListener<P>) l).handler(param, strace, (Integer[]) (rbranch.toArray(new Integer[rbranch.size()])), (Boolean) params[1]);
				}
			} else if (l instanceof UndefinedParamListener) {
				if (((UndefinedParamListener) l).guard(param, strace, when, where, params)) {
					param = (P) ((UndefinedParamListener) l).handler(param, strace, when, where, params);
				}
			} else throw new RuntimeException("Should not be here!");;
		}
		return param;
	}

	@Override
	public Instruction copy() {
		return new EventInst(when, where, strace, params);
	}

}
