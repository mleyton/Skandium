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

import cl.niclabs.skandium.skeletons.Skeleton;


/**
 * Given a condition result, this instruction choices which sub-stack should be executed
 */

public class ChoiceInst extends AbstractInstruction {
	
	boolean cond;
	Stack<Instruction> trueCaseStack, falseCaseStack;

	/**
	 * The Constructor
	 * @param cond the result of a {@link cl.niclabs.skandium.muscles.Condition} evaluation
	 * @param trueCaseStack sub-stack to be executed if <code>cond</code> is true.
	 * @param falseCaseStack sub-stack to be executed if <code>cond</code> is true.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public ChoiceInst(boolean cond, Stack<Instruction> trueCaseStack, Stack<Instruction> falseCaseStack, @SuppressWarnings("rawtypes") Skeleton[] strace){
		super(strace);
		this.cond = cond;
		this.trueCaseStack = trueCaseStack;
		this.falseCaseStack = falseCaseStack;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		if(cond){
			stack.addAll(trueCaseStack);
		} else {
			stack.addAll(falseCaseStack);
		}
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		return new ChoiceInst(cond, copyStack(trueCaseStack), copyStack(falseCaseStack), copySkeletonTrace());
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
