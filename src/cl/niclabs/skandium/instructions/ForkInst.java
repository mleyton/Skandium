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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

/**
 * This instruction holds the parallelism behavior of a {@link cl.niclabs.skandium.skeletons.Fork} skeleton.
 * 
 * @author mleyton
 */
public class ForkInst extends  AbstractInstruction {

	@SuppressWarnings("unchecked")
	Split split; 
	List<Stack<Instruction>> substacks; 
	@SuppressWarnings("unchecked")
	Merge merge;
	
	/**
	 * The main constructor.
	 * @param split  The muscle used to divide the parameters.
	 * @param stacks The code to execute for each subparam.
	 * @param merge The code to merge the result of executing the stack on the subparam.
	 * @param strace 
	 */
	public ForkInst(Split<?, ?> split, List<Stack<Instruction>> stacks, Merge<?, ?> merge, StackTraceElement[] strace) {
		super(strace);
		this.split = split;
		this.substacks = stacks;
		this.merge = merge;
	}

	/**
	 * Invokes the {@link Split} muscle is on param, and a creates a new stack for each subparam.
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {

		(new Event(Event.Type.FORK_BEFORE_SPLIT, null, strace)).interpret(param, stack, children);
		Object[] params = split.split(param);
		(new Event(Event.Type.FORK_AFTER_SPLIT, null, strace)).interpret(param, stack, children);
		
		if(params.length != substacks.size()){
			throw new Exception("Invalid number of divisions for Fork skeleton. Expected "+ substacks.size() +" but was "+params.length+".");
		}
		
		// For each stack copy all of its elements
		for(int i=0; i < params.length; i++){
			Stack<Instruction> subStack = copyStack(this.substacks.get(i));
			subStack.add(0,new Event(Event.Type.FORK_AFTER_NESTED_SKEL, i, strace));
			subStack.push(new Event(Event.Type.FORK_BEFORE_NESTED_SKEL, i, strace));
			children.add(subStack);
		}
		
		stack.push(new Event(Event.Type.FORK_AFTER_MERGE, null, strace));
		stack.push(new MergeInst(merge, strace));
		stack.push(new Event(Event.Type.FORK_BEFORE_MERGE, null, strace));
		
		return params;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		
		List<Stack<Instruction>> newStacks= new ArrayList<Stack<Instruction>>();
		
		for(int i=0; i< substacks.size(); i++){
			newStacks.add(copyStack(substacks.get(i)));
		}
		
		return new ForkInst(split, substacks, merge, strace);
	}
}
