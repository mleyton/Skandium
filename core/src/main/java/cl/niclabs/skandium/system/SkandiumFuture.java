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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is the <code>Skandium</code> {@link Future} implementation.
 * 
 * @author mleyton
 *
 * @param <V> The type of the awaited object.
 */
public class SkandiumFuture<V> implements Future<V> {

	TaskExecutor executor;
	Task task;
	V result;
	ExecutionException exception;
	private boolean cancelled;
	
	/**
	 * The main constructor of the class.
	 * @param executor The executor where the task is executed.
	 * @param task The awaited value of this future is the result of the specified task. 
	 */
	SkandiumFuture(TaskExecutor executor, Task task){
		this.executor = executor;
		this.task     = task;
		this.result = null; 
		this.exception = null;
		this.cancelled = false;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public synchronized boolean cancel(boolean mayInterrupt) {
		if(result != null || exception !=null) return false;
		
		this.cancelled = task.cancel();
		
		notifyAll();
		
		return cancelled;
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized V get() throws InterruptedException, ExecutionException {
		
		while(exception == null && result == null && !cancelled){
			wait();
		}
		
		if(exception != null) throw exception;
		if(cancelled) throw new CancellationException("Execution was cancelled.");
		
		return result;
	}

	/** {@inheritDoc}
	 */
	@Override
	public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		return exception != null || result != null || cancelled;
	}
	
	public synchronized void set(V v){
		result = v;
		notifyAll();
	}
	
	public synchronized void  setException(ExecutionException e){
		this.exception = e;
		notifyAll();
	}
}