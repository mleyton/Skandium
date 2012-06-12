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

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is an extension of {@link ThreadPoolExecutor}, but with custom @{link #beforeExecute}
 * and #{link #afterExecute} methods to manipulate taske execution.
 * 
 * 
 * @author mleyton
 */
public class TaskExecutor extends ThreadPoolExecutor {
	
	/**
	 * Constructs a <code>TaskExecutor</code> with a maximum number of threads.
	 * @param maxThreads The number maximum number of threads to use.
	 */
	public TaskExecutor(int maxThreads){
		
		super(maxThreads, maxThreads, 10, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
	}


	/** {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
	}

	/**
	 * A {@link Task}'s parent is notified when its child is finished.
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		
		if( !(r instanceof Task) ) return;
		
		Task task = (Task)r;
		
		if(task.isFinished()){ 
	
			task.notifyParent();
		}
	}
}
