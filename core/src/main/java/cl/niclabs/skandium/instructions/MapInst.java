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

import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

/**
 * This instruction holds the parallelism behavior of a {@link cl.niclabs.skandium.skeletons.Map} skeleton.
 * 
 * @author mleyton
 */
public class MapInst extends  AbstractInstruction {
	
	@SuppressWarnings("unchecked")
	Split split;
	Stack<Instruction> substack;
	@SuppressWarnings("unchecked")
	Merge merge;

	/**
	 * The main constructor.
	 * 
	 * @param split The muscle to divide a param.
	 * @param stack The code to execute for each subparam.
	 * @param merge The code to merge the results of the execution of each subparam.
	 * @param strace 
	 */
	public MapInst(Split<?, ?> split, Stack<Instruction> stack, Merge<?, ?> merge, StackTraceElement[] strace) {
		super(strace);

		this.split=split;
		this.substack=stack;
		this.merge=merge;
	}

	/**
	 * Subdivides param using the {@link Split} muscle, and then a stack is copied for each subparam.
	 *  
	 * {@inheritDoc} 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		
		Object[] params = split.split(param);
		
		for(int i=0;i<params.length;i++){
			children.add(copyStack(this.substack));
		}
		
		stack.push(new MergeInst(merge, strace));

		return params;
	}
	
	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Instruction copy() {
		
		return new MapInst(split, copyStack(substack), merge, strace);
	}
}
