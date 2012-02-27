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

import cl.niclabs.skandium.events.ConditionListener;
import cl.niclabs.skandium.events.IndexListener;
import cl.niclabs.skandium.events.TraceListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.events.SkandiumEventListener;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;
import cl.niclabs.skandium.system.events.UndefinedParamListener;


/**
 * Instruction that fires an Event
 */

public class EventInst extends AbstractInstruction {
	
	When when;
	Where where;
	Object[] params;

	/**
	 * The constructor
	 * @param when Defines the event {@link When} dimension, it could be {@link When#BEFORE} or {@link When#AFTER} 
	 * @param where Defines the event {@link Where} dimension, it could be {@link Where#SKELETON}, {@link Where#CONDITION}, {@link Where#SPLIT}, {@link Where#NESTED_SKELETON} or {@link Where#MERGE}
	 * @param strace nested skeleton tree branch of the current execution.
	 * @param params specific event parameters
	 */
	public EventInst(When when, Where where, SkeletonTraceElement[] strace, Object... params){
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
		Skeleton<?,?> curr = strace[strace.length-1].getSkel();
		SkandiumEventListener[] listeners = ((AbstractSkeleton<?,?>) curr).getListeners(when, where);
		for (SkandiumEventListener l : listeners) {
			if (l instanceof TraceListener<?>) {
				if (((TraceListener<P>) l).guard(param, strace)) {
					param = ((TraceListener<P>) l).handler(param, strace);
				}
			} else if (l instanceof IndexListener<?>) {
				if (((IndexListener<P>) l).guard(param, strace, (Integer) params[0])) {
					param = ((IndexListener<P>) l).handler(param, strace, (Integer) params[0]);
				}
			} else if (l instanceof ConditionListener<?>) {
				if (((ConditionListener<P>) l).guard(param, strace, (Integer) params[0], (Boolean) params[1])) {
					param = ((ConditionListener<P>) l).handler(param, strace, (Integer) params[0], (Boolean) params[1]);
				}
			} else if (l instanceof UndefinedParamListener) {
				if (((UndefinedParamListener) l).guard(param, strace, when, where, params)) {
					param = (P) ((UndefinedParamListener) l).handler(param, strace, when, where, params);
				}
			} else throw new RuntimeException("Should not be here!");
		}
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		return new EventInst(when, where, copySkeletonTrace(), params);
	}

}
