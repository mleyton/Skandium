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

import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

/**
 * This instruction holds the parallelism behavior of a Divide and Conquer ({@link cl.niclabs.skandium.skeletons.DaC}) skeleton.
 * 
 * @author mleyton
 */
public class DaCInst extends  AbstractInstruction {
	
	@SuppressWarnings("unchecked")
	Condition condition;
	@SuppressWarnings("unchecked")
	Split split;
	Stack<Instruction> substack;
	@SuppressWarnings("unchecked")
	Merge merge;
	
	/**
	 * The constructor.
	 * @param condition the condition to subdivide.
	 * @param split the code to subdivide.
	 * @param stack the stack of instructions to execute when the base case is reached.
	 * @param merge the code to merge the results of a subdivision.
	 * @param strace 
	 */
	public DaCInst(Condition<?> condition, Split<?, ?> split, Stack<Instruction> stack, Merge<?, ?> merge, StackTraceElement[] strace) {
		super(strace);
		this.condition = condition;
		this.split = split;
		this.substack = stack;
		this.merge = merge;
	}

	/**
	 * This method evaluates the {@link Condition} muscle with the given <code>param</code>.
	 * If the {@link Condition} returns true then the <code>param</code> is divided with the {@link Split} muscle,
	 * and new stacks are created to execute each subparam.
	 * If the {@link Condition} returns false then this instruction's stack is added to the parameter stack.
	 * 
	 * {@inheritDoc}}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack,List<Stack<Instruction>> children) throws Exception {

		//if condition is true we split 
		if(condition.condition(param)){
			
			Object result[]  =  split.split(param);
			
			for(int i = 0 ; i < result.length ; i++){
				
				Stack<Instruction> newStack = new Stack<Instruction>();
				
				newStack.add(this.copy());
				
				children.add(newStack);
			}
			
			//Put a merge instruction on the current stack
			//to merge results when children are finished.
			stack.push(new MergeInst(merge, strace));
			
			return result;
		}
		//else we execute 
		else{
			stack.addAll(this.substack);
		}
		
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new DaCInst(condition, split, copyStack(substack), merge, strace);
	}
}