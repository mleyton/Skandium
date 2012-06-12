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
package cl.niclabs.skandium.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import cl.niclabs.skandium.instructions.Instruction;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.Task;

/**
 * The interpretation code.
 * 
 * @author mleyton
 */
public class Interpreter {
	
	/**
	 * This is the main interpretation method.
	 * 
	 * The method pops instructions from the computation stack, and executes them with the data parameter. 
	 * The data parameter is then updated with the result of the execution. The process is looped until, either
	 * the stack is empty or children stacks are generated (data parallelism).
	 * 
	 * In the later case, each child stack is encapsulated along with its data parameter
	 * into a child {@link Task} object, and submitted for execution.
	 * 
	 * If an exception is raised during the computation of an instruction, the {@link Task} is notified
	 * with the exception.
	 * 
	 * @param task  The source of the stack and parameter to compute.
	 * @return The task holding the current state of the computation (usually the same as the input) 
	 */
	static Task interpret(Task task){
		
		//get the current data, stack and strace
		Object P = task.getP();
		Stack<Instruction> stack = task.getStack();
		@SuppressWarnings("rawtypes")
		Skeleton[] strace = null;
		
		try{	
			//array for storing children if needed
			List<Stack<Instruction>> children = new ArrayList<Stack<Instruction>>();
		
			//iterate while there are instructions and no children tasks
			while(!stack.empty() && children.size() <= 0){
		
				//pop the instruction and interpret it
				Instruction inst = stack.pop();
				strace= inst.getSkeletonTrace();  //skeleton trace in case of exception
				P = inst.interpret(P, stack, children);
				
				//check that the task has not been canceled
				if(task.isCancelled()) return task;
			}
			
			//update the task with the latest data
			task.setP(P);
			
			//Data parallelism has been reached
			if(children.size() > 0){

				task.setP(null); //free memory parent not needed any more
				task.setStack(stack); //store the stack to merge results later
				
				Object[] Ps = (Object[])P;
				
				if(children.size() != Ps.length){
					throw new Exception("Data parallelism error. Substacks size is different than parameter length.");
				}
				
				synchronized(task){
					//add children tasks
					for(int i=0; i < Ps.length; i++){
						task.addChild(Ps[i], children.get(i));
					}
					
					//schedule children for execution
					task.sendChildrenToExecution();
				}
				
				return task;
			}
		}
		catch(Throwable t){
	
			setLogicalException(t, strace);
			
			//if exception was raised we send it back to the user
			task.setException(new ExecutionException(t));
		}
		
		return task;
	}
	
	@SuppressWarnings("rawtypes")
	private static void setLogicalException(Throwable t, Skeleton[] strace){

		ArrayList<StackTraceElement> list = new ArrayList<StackTraceElement>();

		//iterate over every trace
		for(int i=0;i<t.getStackTrace().length;i++){
			
			String className = t.getStackTrace()[i].getClassName();
			Class cls;
			
			try {
				cls = Class.forName(className);
			} catch (ClassNotFoundException e) {
				
				list.add(t.getStackTrace()[i]);
				continue; 
			}
			
			/* If the trace belongs to an instruction then we replace it.
			 * We also double check that the "next" trace corresponds to Interperter.
			 * 
			 * i+1:cl.niclabs.skandium.system.Interpreter.interpret(Interpreter.java:68)
			 *   i:cl.niclabs.skandium.instructions.DaCInst.interpret(DaCInst.java:71) */
			if( Instruction.class.isAssignableFrom(cls) && 
					t.getStackTrace()[i+1].getClassName().equals(Interpreter.class.getCanonicalName())){
			
				for(int j=strace.length-1; j>=0; j--){
					list.add(((AbstractSkeleton)strace[j]).getTrace());
				}
			}
			else{
				list.add(t.getStackTrace()[i]);
			}
		}

		t.setStackTrace(list.toArray(new StackTraceElement[list.size()]));
	}
}