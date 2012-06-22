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

import cl.niclabs.skandium.skeletons.Skeleton;


/**
 * This instruction holds the parallelism behavior of a {@link cl.niclabs.skandium.skeletons.For} skeleton.
 * 
 * @author mleyton
 */
public class ForInst extends AbstractInstruction{

	Stack<Instruction> substack;
	int times;
	
	/**
	 * The main constructor.
	 * @param substack  The stack to execute on every iteration.
	 * @param times The number of times to iterate.
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public ForInst(Stack<Instruction> substack, int times, @SuppressWarnings("rawtypes") Skeleton[] strace) {
		super(strace);
		this.substack = substack;
		this.times = times;
	}
	
	/**
	 * The number of times left to execute the substack is reduced by one, and the substack's instructions are
	 * added to the stack parameter.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		
		if (times > 0) {		
			times--;
			stack.push(this);
			stack.addAll(substack);
		}
		return param;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
		
		return new ForInst(copyStack(substack), times, copySkeletonTrace());
	}

	@Override
	public void setParent(int parent) {
		for (Instruction inst : substack) {
			if (inst.getParent() == this.parent) {
				inst.setParent(parent);
			}
		}		
		super.setParent(parent);
	}
	
}
