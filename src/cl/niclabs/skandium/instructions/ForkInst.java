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

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.skeletons.Skeleton;

/**
 * This instruction holds the parallelism behavior of a {@link cl.niclabs.skandium.skeletons.Fork} skeleton.
 * 
 * @author mleyton
 */
public class ForkInst extends  AbstractInstruction {

	@SuppressWarnings("rawtypes")
	Split split; 
	List<Stack<Instruction>> substacks; 
	@SuppressWarnings("rawtypes")
	Merge merge;
	int id;
	
	/**
	 * The main constructor.
	 * @param split  The muscle used to divide the parameters.
	 * @param stacks The code to execute for each subparam.
	 * @param merge The code to merge the result of executing the stack on the subparam.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public ForkInst(Split<?, ?> split, List<Stack<Instruction>> stacks, Merge<?, ?> merge, @SuppressWarnings("rawtypes") Skeleton[] strace, int id, int parent) {
		super(strace);
		this.split = split;
		this.substacks = stacks;
		this.merge = merge;
		this.id = id;
		this.parent = parent;
	}

	/**
	 * Invokes the {@link cl.niclabs.skandium.muscles.Split} muscle is on param, and a creates a new stack for each subparam.
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		(new EventInst(When.BEFORE, Where.SPLIT, strace, id, false, parent)).interpret(param, stack, children);
		Object[] params = split.split(param);
		
		stack.push(new SplitInst(substacks, merge, strace, id, parent));
		stack.push(new EventInst(When.AFTER, Where.SPLIT, strace, id, false, parent));
		
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
		
		return new ForkInst(split, newStacks, merge, copySkeletonTrace(), id, parent);
	}
	
	@Override
	public void setParent(int parent) {
		for (Stack<Instruction> stack : substacks) {
			for (Instruction inst : stack) {
				if (inst.getParent() == this.parent) {
					inst.setParent(parent);
				}
			}
		}
		super.setParent(parent);
	}
}
