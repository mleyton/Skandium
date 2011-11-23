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

import cl.niclabs.skandium.instructions.Instruction;
import cl.niclabs.skandium.muscles.Condition;

/**
 * Represents the behavior of a {@link cl.niclabs.skandium.skeletons.While} skeleton.
 * 
 * @author mleyton
 */
public class WhileInst extends AbstractInstruction {

	@SuppressWarnings("unchecked")
	Condition condition;
	Stack<Instruction> substack;

	/**
	 * The main constructor
	 * @param condition The condition to evaluate.
	 * @param stack The code to execute while the condition holds true.
	 * @param stackTraceElements 
	 */
	public WhileInst(Condition<?> condition, Stack<Instruction> stack, StackTraceElement[] strace) {
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
		
		if(condition.condition(param)){
			stack.push(this);
			stack.addAll(this.substack);
		}
		
		return param;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		
		return new WhileInst(condition, copyStack(substack), strace);
	}
}