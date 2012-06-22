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

/**
 * This instruction holds the parallelism behavior of an {@link cl.niclabs.skandium.skeletons.If} skeleton.
 * 
 * @author mleyton
 */
public class IfInst extends AbstractInstruction {
	
	@SuppressWarnings("rawtypes")
	Condition condition;
	Stack<Instruction> trueCaseStack, falseCaseStack;
	int id;
	
	/**
	 * The main condition.
	 * @param condition Used to decide on the instruction. 
	 * @param trueCaseStack Code to execute in the true case.
	 * @param falseCaseStack Code to execute in the false case.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public IfInst(Condition<?> condition, Stack<Instruction> trueCaseStack, Stack<Instruction> falseCaseStack, @SuppressWarnings("rawtypes") Skeleton[] strace, int id, int parent) {
		super(strace);
		this.condition = condition;
		this.trueCaseStack = trueCaseStack;
		this.falseCaseStack = falseCaseStack;
		this.id = id;
		this.parent = parent;
	}
	
	/**
	 * Invokes the {@link Condition} muscle with the given <code>param</code>.
	 * Depending on the result either the trueCaseStack or falseCaseStack code is added for execution.
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {

		new EventInst(When.BEFORE, Where.CONDITION, strace, id, false, parent).interpret(param, stack, children);
		boolean cond = condition.condition(param);

		stack.push(new ChoiceInst(cond, trueCaseStack, falseCaseStack, strace));
		stack.push(new EventInst(When.AFTER, Where.CONDITION, strace, id, cond, parent));

		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		
		return new IfInst(condition, copyStack(trueCaseStack), copyStack(falseCaseStack), copySkeletonTrace(), id, parent);
	}

	@Override
	public void setParent(int parent) {
		for (Instruction inst : trueCaseStack) {
			if (inst.getParent() == this.parent) {
				inst.setParent(parent);
			}
		}
		for (Instruction inst : falseCaseStack) {
			if (inst.getParent() == this.parent) {
				inst.setParent(parent);
			}
		}
		super.setParent(parent);
	}
}
