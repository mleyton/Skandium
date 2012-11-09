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
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.lang.reflect.Array;

import cl.niclabs.skandium.instructions.Instruction;

/**
 * A Task is the internal representation of the execution of a skeleton program with a given parameter.
 * A Task is never seen by a <code>Skandium</code> library user.
 * 
 * The Task keeps track, among other of the execution state, data state, exceptions, future updating, and children tasks.
 * Children tasks are created when data parallelism is encountered.
 * 
 * @author mleyton
 */
public class Task implements Runnable, Comparable<Task>{

	static Random random = new Random();
	
	public long id;					//The unique identifier of this task.
	Task root, parent;    			//A reference to the root task and the parent of this task.
	int priority;					//Higher number is lower priority
	
	Stack<Instruction> stack;		//The stack representing the execution of the skeleton program.
	Object P;						//The data of the current execution. 
	ArrayList<Task> children;		//The children tasks (if any).
	long unfinishedChildren;		//The number of unfinished children tasks.
	Throwable exception;			//The current exception.
	
	@SuppressWarnings("rawtypes")
	SkandiumFuture future;			//The future object (if this is a root task) which will hold the computation's result.
	TaskExecutor executor;			//The executor service used for computation.

	boolean canceled;				//True if this task was canceled, false otherwise.
	
	/**
	 * Constructs a root task, with its default priority.
	 * 
	 * @param P	The initial data used to compute this <code>Task</code>.
	 * @param stack	The stack program used to compute the data.
	 * @param executor The executor service used to compute this <code>Task</code>.
	 */
	public Task(Object P, Stack<Instruction> stack, TaskExecutor executor){
		this(null, null, P, stack, Integer.MAX_VALUE, executor);
		this.root=this;
	}
	
	/**
	 * Constructs a task, which may or not be root.
	 * 
	 * @param root  The root of this <code>Task</code>, which may be null.
	 * @param parent The parent of this <code>Task</code>, which may be null.
	 * @param P The initial data used to compute this task.
	 * @param stack The stack program used to compute the data.
	 * @param priority The priority of this <code>Task</code>.
	 * @param executor The executor service used to compute this <code>Task</code>.
	 */
	@SuppressWarnings({ "rawtypes" })
	Task(Task root, Task parent, Object P, Stack<Instruction> stack, int priority, TaskExecutor executor){
		this.id       = random.nextLong();
		this.root     = root;
		this.parent   = parent;
		this.P        = P; 
		this.stack    = stack;
		this.priority = priority;
		this.children = null;
		this.unfinishedChildren = 0;
		this.future   = isRoot() ? new SkandiumFuture(executor, this): null;
		this.executor = executor;
		this.canceled= false;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o){
		if(this == o) return true;
		
		if( !(o instanceof Task)) return false;
		
		Task t = (Task) o;
		
		return id == t.id;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public int hashCode(){
	
		//Joshua Bloch recipe for long type fields
		return (int)(id^(id>>>32));
	}
	
	/**
	 * This class has a natural ordering that is inconsistent with equals. 
	 * The following is not always true: (x.compareTo(y)==0) == (x.equals(y)) 
	 * as there can be different tasks with the same priority.
	 * 
	 * {@inheritDoc}
	 */
	public int compareTo(Task task){
		
		if(task == null) throw new NullPointerException("Can't compare with null task.");
			
		return this.priority - ((Task)task).priority;
	}
	
	/**
	 * The run method which executes the computation loop of this <code>Task</code>.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void run() {

		Interpreter.interpret(this);		
	}

	/**
	 * @return true if this <code>Task</code> is root, false otherwise.
	 */
	boolean isRoot(){
		return (parent == null);
	}
	
	/**
	 * A <code>Task</code> is finished if it its stack is empty and
	 * if its children tasks are finished.
	 * 
	 * @return true if this <code>Task</code> is finished, false otherwise.
	 */
	boolean isFinished(){
		
		return stack.empty() && childrenAreFinished();
	}
	
	/**
	 * @return true if this <code>Task</code>'s children are finished.
	 */
	synchronized  boolean childrenAreFinished(){
		
		return this.unfinishedChildren <= 0;
	}
	
	/**
	 * @return true if this <code>Task</code> has children.
	 */
	synchronized boolean hasChildren(){
		return children != null && children.size()>0;
	}

	/**
	 * This method is used to update this <code>Task</code>'s parameter data.
	 * @param P The new data which will overwrite the previous value held in this <code>Task</code>.
	 */
	void setP(Object P) {
		this.P = P;		
	}

	/**
	 * This method is used to update this <code>Task</code>'s parameter data.
	 * @param P The new data which will overwrite the previous value held in this <code>Task</code>.
	 */
	Object getP() {
		return P;
	}
	
	/**
	 * This method is used to set an exception associated with the computation of this task family.
	 * The exception is actually associated with the root task and not this task.
	 * 
	 * Only the first invocation of this method is performed, further invocations are discarded. 
	 * 
	 * By calling this method the computation of this task and all of its 
	 * family (<code>this.root.cancel()</code>) are canceled. 
	 * 
	 * @param e The error detected during the computation of this task.
	 */
	void setException(ExecutionException e){
		
		synchronized(root){
		
			//Don't overwrite previous exceptions
			if(root.exception != null) return; 
			
			root.exception = e;
			root.future.setException(e);
			
			root.cancel();
		}
	}
	
	/**
	 * @return the current exception associated with this task or null if non exists present.
	 */
	Throwable getException(){
		return this.exception;
	}

	/**
	 * Set the current computation stack for this <code>Task</code>. 
	 * 
	 * @param stack The new computation stack.
	 */
	void setStack(Stack<Instruction> stack) {
		this.stack=stack;
	}

	/**
	 * Get the current computation stack for this <code>Task</code> 
	 * @return
	 */
	Stack<Instruction> getStack() {
		return this.stack;
	}

	/**
	 * Create a new child <code>Task</code> with the given data and computation stack.
	 * 
	 * 
	 * @param object The data of the child <code>Task</code>.
	 * @param stack The computation stack of the child <code>Task</code>.
	 * @return The new child <code>Task</code>
	 */
	synchronized Task addChild(Object object, Stack<Instruction> stack) {
		
		if(this.children == null) this.children = new ArrayList<Task>();
			
		Task child = new Task(root, this, object, stack, this.priority - 1, executor);

		this.children.add(child);
		
		this.unfinishedChildren++;
		
		return child;
	}

	/**
	 * @return The future object associated with this <code>Task</code>'s root.
	 */
	public SkandiumFuture<?> getFuture() {
		return root.future;
	}

	/**
	 * This method updates the future of this <code>Task</code>'s root with the root's data parameter P.. 
	 */
	@SuppressWarnings("unchecked")
	void updateFutureWithResult() {
		root.future.set(root.P);
	}

	/**
	 * Notify the parent tasks that this <code>Task</code> is finished.
	 * 
	 * The parent's unfinished children counter will be decreased.
	 * 
	 * Also, if this was the parent's last child then prepare and submit the parent <code>Task</code> for execution.
	 * 
	 * If the <code>Task</code> is root, then the future is updated with the result.
	 */
	@SuppressWarnings("unchecked")
	protected <P> void notifyParent() {

		if(this.isRoot()){
			updateFutureWithResult();
			return;
		}
	
		synchronized(parent){
			parent.unfinishedChildren--;
			
			if(parent.children.size() > 0 && parent.childrenAreFinished()){
				
				P[] results = (P []) Array.newInstance(this.getP().getClass(), parent.children.size());
				
				for(int i=0;i<results.length;i++){
					results[i] = (P) parent.children.get(i).getP();
				}
				
				parent.P=results;
				parent.children.clear();
				
				executor.execute(parent);
			}
		}
	}
	
	/**
	 * Submit children tasks to execution.
	 */
	synchronized void sendChildrenToExecution() {

		if(root.canceled) return;
		
		for(int i=0; i < children.size(); i++){
			executor.execute(children.get(i)); 
		}
	}

	/**
	 * Cancel the execution of this <code>Task</code>
	 * 
	 * After this method returns true successive invocations will always return false.
	 * 
	 * @return true if the cancel was executed false otherwise.
	 */
	synchronized boolean cancel() {
		
		if(isCancelled()) return false;

		this.canceled = true;
		
		if(children != null){
			for(int i = 0; i < children.size(); i++){
				children.get(i).cancel();
			}
		}		
		
		this.executor.remove(this);
		
		return true;
	}
	
	/**
	 * @return true if this task was canceled, false otherwise.
	 */
	boolean isCancelled(){
		
		return this.canceled;
	}
}
