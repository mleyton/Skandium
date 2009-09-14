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

import cl.niclabs.skandium.muscles.Execute;

/**
 * Represent the behavior of a {@link cl.niclabs.skandium.skeletons.Seq} {@link cl.niclabs.skandium.skeletons.Skeleton}, used to wrap an {@link Execute} muscle.
 * 
 * @author mleyton
 */
public class SeqInst extends AbstractInstruction {

	@SuppressWarnings("unchecked")
	Execute execute;

	/**
	 * The main constructor.
	 * @param execute The {@link cl.niclabs.skandium.muscles.Muscle} to execute. 
	 * @param strace The logical stack trace of this instruction.
	 */
	public SeqInst(Execute<?,?> execute, StackTraceElement[] strace){
		super(strace);
		this.execute = execute;
	}
	
	/**
	 * Evaluates the {@link Execute} muscle on the param.
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		
		return execute.execute(param);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new SeqInst(execute, strace);
	}
}
