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
	Stack<Integer> rbranch;
	
	/**
	 * The constructor.
	 * @param condition the condition to subdivide.
	 * @param split the code to subdivide.
	 * @param stack the stack of instructions to execute when the base case is reached.
	 * @param merge the code to merge the results of a subdivision.
	 * @param rbranch
	 * @param strace 
	 */
	public DaCInst(Condition<?> condition, Split<?, ?> split, Stack<Instruction> stack, Merge<?, ?> merge, 
			Stack<Integer> rbranch, StackTraceElement[] strace) {
		super(strace);
		this.condition = condition;
		this.split = split;
		this.substack = stack;
		this.rbranch = rbranch;
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

		(new Event(Event.Type.DAC_BEFORE_CONDITION, rbranch, strace)).interpret(param, stack, children);

		//if condition is true we split 
		if(condition.condition(param)){
			Object eventParam[] = {rbranch, true};
			(new Event(Event.Type.DAC_AFTER_CONDITION, eventParam, strace)).interpret(param, stack, children);

			(new Event(Event.Type.DAC_BEFORE_SPLIT, rbranch, strace)).interpret(param, stack, children);
			Object result[]  =  split.split(param);
			(new Event(Event.Type.DAC_AFTER_SPLIT, rbranch, strace)).interpret(result, stack, children);
			
			for(int i = 0 ; i < result.length ; i++){
				Stack<Integer> subrbranch = new Stack<Integer>();
				subrbranch.addAll(rbranch);
				subrbranch.push(i);
				
				Stack<Instruction> newStack = new Stack<Instruction>();

				newStack.push(new Event(Event.Type.DAC_AFTER, subrbranch, strace));
				newStack.add(this.copy());
				newStack.push(new Event(Event.Type.DAC_BEFORE, subrbranch, strace));
				
				children.add(newStack);
			}
			
			//Put a merge instruction on the current stack
			//to merge results when children are finished.
			stack.push(new Event(Event.Type.DAC_AFTER_MERGE, rbranch, strace));
			stack.push(new MergeInst(merge, strace));
			stack.push(new Event(Event.Type.DAC_BEFORE_MERGE, rbranch, strace));
			
			return result;
		}
		//else we execute 
		else{
			Object eventParam[] = {rbranch, false};
			(new Event(Event.Type.DAC_AFTER_CONDITION, eventParam, strace)).interpret(param, stack, children);
			stack.push(new Event(Event.Type.DAC_AFTER_NESTED_SKEL, rbranch, strace));
			stack.addAll(this.substack);
			stack.push(new Event(Event.Type.DAC_BEFORE_NESTED_SKEL, rbranch, strace));
		}
		
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new DaCInst(condition, split, copyStack(substack), merge, rbranch, strace);
	}
}
