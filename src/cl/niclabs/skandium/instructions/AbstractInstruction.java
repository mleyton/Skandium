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

import java.util.Stack;

import cl.niclabs.skandium.system.events.SkeletonTraceElement;

/**
 * The main class from which all other Instructions inherit.
 * 
 * @{inheritDoc}
 * 
 * @author mleyton
 */
abstract class AbstractInstruction implements Instruction {

	final SkeletonTraceElement[] strace;
	
	AbstractInstruction(SkeletonTraceElement[] strace){
		this.strace = strace;
	}
	
	/**
	 * This is a utility method to copy a stack.
	 *  
	 * @param stack The stack to be copied.
	 * @return The copy of the stack.
	 */
	static Stack<Instruction> copyStack(Stack<Instruction> stack){
	
		Stack<Instruction> newStack= new Stack<Instruction>();
		
		for(int i=0; i < stack.size(); i++){
			Instruction inst = stack.get(i).copy();
			newStack.push(inst);
		}
		
		return newStack;
	}
	
	@Override
	public SkeletonTraceElement[] getSkeletonTrace(){
		
		return strace;
	}

	@Override
	public SkeletonTraceElement[] copySkeletonTrace(){
		SkeletonTraceElement[] newStrace = new SkeletonTraceElement[strace.length];
		for (int i=0; i<strace.length; i++) {
			newStrace[i] = new SkeletonTraceElement(strace[i].getSkel(), strace[i].getId());
		}
		return newStrace;
	}
	
	void setChildIds(Stack<Instruction> subStack, int id) {
		for (Instruction i:subStack) {
			SkeletonTraceElement[] subStrace = i.getSkeletonTrace();
			int j=strace.length;
			subStrace[j] = new SkeletonTraceElement(subStrace[j].getSkel(), id);
		}
	}
	
	void copyIds(Stack<Instruction> subStack) {
		for(Instruction ins:subStack) {
			for (int i=0; i<strace.length; i++) {
				ins.getSkeletonTrace()[i] = new SkeletonTraceElement(strace[i].getSkel(), strace[i].getId());
			}
		}
	}
}