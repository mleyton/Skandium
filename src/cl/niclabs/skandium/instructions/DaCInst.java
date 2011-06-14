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
	
	@SuppressWarnings("rawtypes" )
	Condition condition;
	@SuppressWarnings("rawtypes")
	Split split;
	Stack<Instruction> substack;
	@SuppressWarnings("rawtypes")
	Merge merge;
	Stack<Integer> rbranch;
	
	/**
	 * The constructor.
	 * @param condition the condition to subdivide.
	 * @param split the code to subdivide.
	 * @param stack the stack of instructions to execute when the base case is reached.
	 * @param merge the code to merge the results of a subdivision.
	 * @param rbranch current recursive branch.
	 * @param strace nested skeleton tree branch of the current execution.
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

		boolean cond = condition.condition(param);
		
		Stack<Instruction> splitStack = new Stack<Instruction>();

		Stack<Instruction> newStack = new Stack<Instruction>();
		DaCInst subInst = (DaCInst) this.copy();
		newStack.push(subInst);
		List<Stack<Instruction>> substacks = new ArrayList<Stack<Instruction>>();
		substacks.add(newStack);
		
		splitStack.push(new SplitInst(substacks, merge, strace, rbranch));
		splitStack.push(new EventInst(When.AFTER, Where.SPLIT, strace, rbranch));
		splitStack.push(new SeqInst(split, strace));
		splitStack.push(new EventInst(When.BEFORE, Where.SPLIT, strace, rbranch));
		
		Stack<Instruction> execStack = new Stack<Instruction>();

		execStack.push(new EventInst(When.AFTER, Where.NESTED_SKELETON, strace, rbranch));
		execStack.addAll(this.substack);
		execStack.push(new EventInst(When.BEFORE, Where.NESTED_SKELETON, strace, rbranch));

		stack.push(new ChoiceInst(cond, splitStack, execStack, strace));
		stack.push(new EventInst(When.AFTER, Where.CONDITION, strace, rbranch, cond));
				
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
