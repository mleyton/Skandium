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

import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;


/**
 * 
 * @author gpabon
 */

public class EventInst extends AbstractInstruction {
	
	When when;
	Where where;
	Skeleton<?,?>[] strace;
	Object[] params;

	/**
	 * The main constructor.
	 * @param type
	 * @param eventParam
	 * @param strace
	 */
	public EventInst(When when, Where where, Skeleton<?,?>[] strace, Object... params){
		super(strace);
		this.when = when;
		this.where = where;
		this.strace = strace;
		this.params = params;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		System.out.printf("Inicio evento\n");
		System.out.printf("Cuando: %s, Donde %s\n", when, where);
		for (Skeleton<?,?> e : strace) {
			System.out.println(((AbstractSkeleton<?,?>)e).getTrace());
		}
		System.out.printf("Fin evento\n\n");
		return param;
	}

	@Override
	public Instruction copy() {
		return new EventInst(when, where, strace, params);
	}

}
