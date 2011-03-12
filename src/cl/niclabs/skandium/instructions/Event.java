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
 * XXXX.
 * 
 * @author gpabon
 */
public class Event extends AbstractInstruction {

	public enum Type {
		BEFORE, AFTER, PIPE_BEFORE_STAGE, PIPE_AFTER_STAGE,
		IF_BEFORE_CONDITION, IF_AFTER_CONDITION, 
		IF_BEFORE_NESTED_SKEL, IF_AFTER_NESTED_SKEL,
		WHILE_BEFORE_CONDITION, WHILE_AFTER_CONDITION,
		WHILE_BEFORE_NESTED_SKEL, WHILE_AFTER_NESTED_SKEL,
		FOR_BEFORE_NESTED_SKEL, FOR_AFTER_NESTED_SKEL,
		MAP_BEFORE_SPLIT, MAP_AFTER_SPLIT, MAP_BEFORE_NESTED_SKEL,
		MAP_AFTER_NESTED_SKEL, MAP_BEFORE_MERGE, MAP_AFTER_MERGE,
		FORK_BEFORE_SPLIT, FORK_AFTER_SPLIT, FORK_BEFORE_NESTED_SKEL,
		FORK_AFTER_NESTED_SKEL, FORK_BEFORE_MERGE, FORK_AFTER_MERGE,
		DAC_BEFORE_CONDITION, DAC_AFTER_CONDITION, 
		DAC_BEFORE_SPLIT, DAC_AFTER_SPLIT, DAC_BEFORE_NESTED_SKEL,
		DAC_AFTER_NESTED_SKEL, DAC_BEFORE_MERGE, DAC_AFTER_MERGE,
		USER
	}
	Type type;
	List<Skeleton> sbranch;
	int i; /* index */
	List<Integer> rb; /* recursive D&C branch */

	/**
	 * The main constructor.
	 * @param strace The logical stack trace of this instruction.
	 */
	public Event(Type type, List<Skeleton> sbranch, int i, 
			List<Integer> rb, StackTraceElement[] strace){
		super(strace);
	}
	
	/**
	 * Evaluates the {@link Execute} muscle on the param.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		System.out.println(type);
		return param;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new Event(type, sbranch, i, rb, strace);
	}
}
