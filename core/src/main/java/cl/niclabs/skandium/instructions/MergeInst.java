/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2009 NIC Labs, Universidad de Chile.
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

import java.lang.reflect.Array;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Merge;

/**
 * This is a utility instruction and does not represent a {@link cl.niclabs.skandium.skeletons.Skeleton} in particular.
 * When child substacks are finished, this instruction can be placed on a stack to merge
 * the list of results into a single one.
 * 
 * @author mleyton
 */
public class MergeInst extends AbstractInstruction{
	
	@SuppressWarnings("unchecked")
	Merge merge;
	StackTraceElement[] strace;
	
	public MergeInst(Merge<?,?> merge, StackTraceElement[] strace) {
		super(strace);
		this.merge = merge;
	}

	/**
	 * Merges a list of subparams into a single one using a {@link Merge} muscle.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {

		Object []o = (Object [])param;

		return merge.merge(o);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		
		return new MergeInst(merge, strace);
	}
}
