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
 * Instructions are the code which actually harness parallelism patterns, 
 * as opposed to their {@link cl.niclabs.skandium.skeletons.Skeleton} counterparts which express the pattern
 * to the programmers.
 * 
 * The execution of Instructions can be done in parallel.
 * 
 * @author mleyton
 */
public interface Instruction {

	/**
	 * The entry point to execute an instruction. This method is implemented by each instruction and executes
	 * the corresponding behavior. 
	 * 
	 * @param <P>  The type of the input Parameter
	 * @param param The input parameter for the instruction. 
	 * @param stack The stack of remaining instructions.
	 * @param children An empty array where child instructions can be placed.
	 * @return the result of invoking the instruction on the parameter.
	 * @throws Exception System or user exceptions 
	 */
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception;
	
	/**
	 * @return A state independent copy of this instruction.
	 */
	public Instruction copy();
	
	/**
	 * @return The skeleton trace associated with this instruction. 
	 */
	@SuppressWarnings("rawtypes")
	public Skeleton[] getSkeletonTrace();

	/**
	 * @return A copy of the skeleton trace associated with this instruction. 
	 */
	@SuppressWarnings("rawtypes")
	public Skeleton[] copySkeletonTrace();
	
	public void setParent(int parent);
	public int getParent();
}
