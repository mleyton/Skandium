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
 * 
 * @author gpabon
 */
public class Event extends AbstractInstruction {

	public enum Type {
		FARM_BEFORE, FARM_AFTER,
		PIPE_BEFORE, PIPE_AFTER, 
		PIPE_BEFORE_STAGE, PIPE_AFTER_STAGE,
		SEQ_BEFORE, SEQ_AFTER,
		IF_BEFORE, IF_AFTER,
		IF_BEFORE_CONDITION, IF_AFTER_CONDITION, 
		IF_BEFORE_NESTED_SKEL, IF_AFTER_NESTED_SKEL,
		WHILE_BEFORE, WHILE_AFTER,
		WHILE_BEFORE_CONDITION, WHILE_AFTER_CONDITION,
		WHILE_BEFORE_NESTED_SKEL, WHILE_AFTER_NESTED_SKEL,
		FOR_BEFORE, FOR_AFTER,
		FOR_BEFORE_NESTED_SKEL, FOR_AFTER_NESTED_SKEL,
		MAP_BEFORE, MAP_AFTER,
		MAP_BEFORE_SPLIT, MAP_AFTER_SPLIT, MAP_BEFORE_NESTED_SKEL,
		MAP_AFTER_NESTED_SKEL, MAP_BEFORE_MERGE, MAP_AFTER_MERGE,
		FORK_BEFORE, FORK_AFTER,
		FORK_BEFORE_SPLIT, FORK_AFTER_SPLIT, FORK_BEFORE_NESTED_SKEL,
		FORK_AFTER_NESTED_SKEL, FORK_BEFORE_MERGE, FORK_AFTER_MERGE,
		DAC_BEFORE, DAC_AFTER,
		DAC_BEFORE_CONDITION, DAC_AFTER_CONDITION, 
		DAC_BEFORE_SPLIT, DAC_AFTER_SPLIT, DAC_BEFORE_NESTED_SKEL,
		DAC_AFTER_NESTED_SKEL, DAC_BEFORE_MERGE, DAC_AFTER_MERGE,
		USER
	}
	Type type;
	Object eventParam;

	/**
	 * The main constructor.
	 * @param type
	 * @param eventParam
	 * @param strace
	 */
	public Event(Type type, Object eventParam, StackTraceElement[] strace){
		super(strace);
		this.type = type;
		this.eventParam = eventParam;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, 
			List<Stack<Instruction>> children) throws Exception {
		System.out.printf("Inicio evento\n");
		System.out.printf("Tipo: %s\n", type.toString());
		for (StackTraceElement e : strace) {
			System.out.println(e);
		}
		System.out.printf("Fin evento\n\n");
		return param;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new Event(type, eventParam, strace);
	}
}
