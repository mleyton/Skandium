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
package cl.niclabs.skandium.skeletons;

import java.util.concurrent.Future;

/**
 * A <code>Skeleton</code> is the API for a parallelism pattern.
 * 
 * @author mleyton
 *
 * @param <P> The input type of the parameter.
 * @param <R> The output type of the parameter.
 */
public interface Skeleton<P,R> {
	
	/**
	 * Used to navigate the a <code>Skeleton</code> structure with the visitor design pattern.
	 * @param visitor The visitor.
	 */
	public void accept(SkeletonVisitor visitor);
	
	/**
	 * This method can be used to input a single parameter for computation using the default Skandium singleton.
	 * @param param  The parameter to input
	 * @return A future object which can wait for the result of computation. 
	 */
	public Future<R> input(P param);
	
	/**
	 * This method allows multiple parameter input for computation using the default Skandium singleton.
	 * @param param The list of parameters to compute.
	 * @return An array of futures which can wait for the result of the submitted parameters. 
	 * The i-th element of the array represents the i-th element of the input array. 
	 */
	public Future<R>[] input(P[] param);
}
