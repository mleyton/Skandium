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

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.events.EventIdGenerator;

/**
 * Represents the behavior of a {@link cl.niclabs.skandium.skeletons.While} skeleton.
 * 
 * @author mleyton
 */
public class WhileInst extends AbstractInstruction {

	@SuppressWarnings("rawtypes")
	Condition condition;
	Stack<Instruction> substack;

	/**
	 * The main constructor
	 * @param condition The condition to evaluate.
	 * @param stack The code to execute while the condition holds true.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public WhileInst(Condition<?> condition, Stack<Instruction> stack, @SuppressWarnings("rawtypes") Skeleton[] strace) {
		super(strace);
		this.condition = condition;
		this.substack = stack;
	}

	/**
	 * If the {@link Condition} holds true then we add this instruction and then the stack code for execution.
	 * This will simulate a loop behavior.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		
		int id = EventIdGenerator.getSingleton().increment();
		(new EventInst(When.BEFORE, Where.CONDITION, strace, id, false, 0)).interpret(param, stack, children);
		boolean cond = condition.condition(param);
		if(cond){
			stack.push(this);
			stack.push(new EventInst(When.AFTER, Where.NESTED_SKELETON, strace, id, false, 0));
			stack.addAll(this.substack);
			stack.push(new EventInst(When.BEFORE, Where.NESTED_SKELETON, strace, id, false, 0));
		}
		stack.push(new EventInst(When.AFTER, Where.CONDITION, strace, id, cond, 0));
				
		return param;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		return new WhileInst(condition, copyStack(substack), copySkeletonTrace());
	}
}
