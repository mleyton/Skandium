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
import cl.niclabs.skandium.skeletons.Skeleton;

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
			Stack<Integer> rbranch, Skeleton<?,?>[] strace) {
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

		(new EventInst(When.BEFORE, Where.CONDITION, strace, rbranch)).interpret(param, stack, children);

		//if condition is true we split 
		if(condition.condition(param)){
			(new EventInst(When.AFTER, Where.CONDITION, strace, rbranch, true)).interpret(param, stack, children);

			(new EventInst(When.BEFORE, Where.SPLIT, strace, rbranch)).interpret(param, stack, children);
			Object result[]  =  split.split(param);
			(new EventInst(When.AFTER, Where.SPLIT, strace, rbranch)).interpret(result, stack, children);
			
			for(int i = 0 ; i < result.length ; i++){
				Stack<Integer> subrbranch = new Stack<Integer>();
				subrbranch.addAll(rbranch);
				subrbranch.push(i);
				
				Stack<Instruction> newStack = new Stack<Instruction>();

				DaCInst subInst = (DaCInst) this.copy();
				subInst.rbranch = subrbranch;
				newStack.add(subInst);
				
				children.add(newStack);
			}
			
			//Put a merge instruction on the current stack
			//to merge results when children are finished.
			stack.push(new EventInst(When.AFTER, Where.MERGE, strace, rbranch));
			stack.push(new MergeInst(merge, strace));
			stack.push(new EventInst(When.BEFORE, Where.MERGE, strace, rbranch));
			
			return result;
		}
		//else we execute 
		else{
			(new EventInst(When.AFTER, Where.CONDITION, strace, rbranch, false)).interpret(param, stack, children);
			stack.push(new EventInst(When.AFTER, Where.NESTED_SKELETON, strace, rbranch));
			stack.addAll(this.substack);
			stack.push(new EventInst(When.BEFORE, Where.NESTED_SKELETON, strace, rbranch));
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
