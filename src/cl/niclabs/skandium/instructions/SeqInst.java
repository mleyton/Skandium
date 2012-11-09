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
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.skeletons.Skeleton;

/**
 * Represent the behavior of a {@link cl.niclabs.skandium.skeletons.Seq} {@link cl.niclabs.skandium.skeletons.Skeleton}, used to wrap an {@link Execute} muscle.
 * 
 * @author mleyton
 */
public class SeqInst extends AbstractInstruction {

	@SuppressWarnings("rawtypes")
	Muscle execute;

	/**
	 * The main constructor.
	 * @param execute The {@link cl.niclabs.skandium.muscles.Muscle} to execute. 
	 * @param strace nested skeleton tree branch of the current execution.
	 */
	public SeqInst(Muscle<?,?> execute, @SuppressWarnings("rawtypes") Skeleton[] strace){
		super(strace);
		this.execute = execute;
	}
	
	/**
	 * Evaluates the {@link Execute} muscle on the param.
	 * 
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <P> Object interpret(P param, Stack<Instruction> stack, List<Stack<Instruction>> children) throws Exception {
		
		if (execute instanceof Split) return ((Split)execute).split(param);
		return ((Execute)execute).execute(param);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instruction copy() {
	
		return new SeqInst(execute, copySkeletonTrace());
	}
}
