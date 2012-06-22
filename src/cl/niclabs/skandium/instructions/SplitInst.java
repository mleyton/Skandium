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
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.skeletons.Skeleton;


/**
 * This is a utility instruction and does not represent a {@link cl.niclabs.skandium.skeletons.Skeleton} in particular.
 * Given a list of substacks and taking the array produced by the {@link cl.niclabs.skandium.muscles.Split} execution, it creates
 * a list of child substacks to execute each element of the <code>param</code> array.
 */

public class SplitInst extends AbstractInstruction {
	
	boolean cond;
	List<Stack<Instruction>> substacks;
	@SuppressWarnings("rawtypes")
	Merge merge;
	int id;

	/**
	 * The main constructor.
	 * @param substacks list of substacks, if the list has just 1 substack, it is copied to complete the <code>param</code> size. 
	 * @param merge The code to merge the results of the execution of each subparam.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	@SuppressWarnings("rawtypes")
	public SplitInst(List<Stack<Instruction>> substacks, Merge merge, Skeleton[] strace, int id, int parent){
		super(strace);
		this.substacks = substacks;
		this.merge = merge;
		this.id = id;
		this.parent = parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		Object[] params = (Object[])param;
		int subsize = substacks.size();
		if((subsize != 1) && params.length != subsize){
			throw new Exception("Invalid number of divisions. Expected "+ substacks.size() +" but was "+params.length+".");
		}
		
		// For each stack copy all of its elements
		for(int i=0; i < params.length; i++){
			Stack<Instruction> subStack;

			subStack = copyStack(subsize == 1? this.substacks.get(0) : this.substacks.get(i));			
			children.add(subStack);
		}
		
		stack.push(new EventInst(When.AFTER, Where.MERGE, strace, id, false, parent));
		stack.push(new MergeInst(merge, strace));
		stack.push(new EventInst(When.BEFORE, Where.MERGE, strace, id, false, parent));
	
		return params;
	}

	@Override
	public Instruction copy() {
		return new SplitInst(substacks, merge, copySkeletonTrace(),id, parent);
	}
}
