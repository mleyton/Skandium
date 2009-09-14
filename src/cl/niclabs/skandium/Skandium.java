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

import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.system.TaskExecutor;

/**
 * This class is the entry point for the <code>Skandium</code> library.
 * 
 * A <code>Skandium</code> instance provides mechanism to register skeleton programs with a given Executor.
 * 
 * @author mleyton
 */
public class Skandium {

	TaskExecutor executor;
	private static Skandium singleton = null;
	
	/**
	 * A constructor which creates a new Skandium instance with a maximum number of computation 
	 * threads equal to the number of cores on the machine. 
	 */
	public Skandium(){
		this(Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * A constructor which creates a new Skandium instance with the specified number of threads.
	 * @param maxThreads The maximum number of threads to compute concurrently. This number must be larger than 1.
	 */
	public Skandium(int maxThreads){
		
		if(maxThreads < 1) throw new IllegalArgumentException("The specified number of threads must be larger than 1");
		
		executor = new TaskExecutor(maxThreads);
	}
	
	/**
	 * Factory method is used to create a new {@link Stream}, which in turn can be used to input parameters for computation.
	 * @param <P> The type of skeleton program's input.
	 * @param <R> The type of the skeleton programs' result.
	 * @param skeleton  The skeleton program which will be used to compute each parameter entered through the {@link Stream}.
	 * @return A new {@link Stream} associated with the specified {@link Skeleton} program.
	 */
	public <P,R> Stream<P,R> newStream(Skeleton<P,R> skeleton){
		
		if(skeleton == null) throw new IllegalArgumentException("The specified skeleton cannot be null");
		
		return new Stream<P,R>(skeleton, executor);
	}
	
	/**
	 * This method shuts down the <code>Skandium</code> instance by shutting down the {@link java.util.concurrent.ExecutorService}.
	 */
	public void shutdown(){
		executor.shutdown();
	}

	/**
	 * @return The default singleton instance of Skandium.
	 */
	public synchronized static Skandium getSingleton() {

		if(singleton == null || singleton.executor.isShutdown()){
			singleton = new Skandium();
		}
		
		return singleton;
	}
	
	public static String version(){
		return "1.0b2";
	}
}