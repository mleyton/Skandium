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
package cl.niclabs.skandium;

import java.util.concurrent.Future;

import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.StackBuilder;
import cl.niclabs.skandium.system.Task;
import cl.niclabs.skandium.system.TaskExecutor;

/**
 * A <code>Stream</code> is a channel to input parameters for execution.
 * Each Stream has an associated skeleton program, defined at creation,
 * which will be used to compute the parameters.
 * 
 * To compute a different skeleton program, simply instantiate a new Stream via the
 * {@link Skandium#newStream} factory method.
 * 
 * Multiple Streams created from the same {@link cl.niclabs.skandium.Skandium} instance will share the same {@link java.util.concurrent.ExecutorService}. 
 * 
 * @author mleyton
 *
 * @param <P>  The type of the input parameter.
 * @param <R>  The type of the computatino's result.
 */
public class Stream<P,R> {

	TaskExecutor executor;
	@SuppressWarnings("rawtypes")
	Skeleton skeleton;
	
	/**
	 * The main constructor of a new <code>Stream</code>. It is not ment to be used directly by programmers.
	 * Instead use {@link Skandium#newStream}.
	 * @param skeleton The associated skeleton program used to compute all parameters inputed through this <code>Stream</code>.
	 * @param executor The TaskExecutor in charge of scheduling a {@link Task} with a thread.
	 */
	Stream(Skeleton<P,R> skeleton, TaskExecutor executor){
		this.skeleton = skeleton;
		this.executor = executor;		
	}
	
	/**
	 * This method can be used to input a single parameter for computation.
	 * @param param  The parameter to input
	 * @return A future object which can wait for the result of computation. 
	 */
	@SuppressWarnings("unchecked")
	public Future<R> input(P param){
		
        StackBuilder builder = new StackBuilder();
        skeleton.accept(builder);
        
        Task task = new Task(param, builder.stack, executor);
		this.executor.execute(task);
		
		return (Future<R>)task.getFuture();
	}
	
	/**
	 * This method allows multiple parameter input for computation.
	 * @param param The list of parameters to compute.
	 * @return An array of futures which can wait for the result of the submitted parameters. 
	 * The i-th element of the array represents the i-th element of the input array. 
	 */
	@SuppressWarnings("unchecked")
	public Future<R>[] input(P param[]){
		
		Future<R> results[] = new Future[param.length];

		for(int i=0;i<param.length ; i++){
			results[i] = input(param[i]);
		}

		return results;
	}
}