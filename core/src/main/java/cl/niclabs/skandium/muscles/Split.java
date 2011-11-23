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
package cl.niclabs.skandium.muscles;

/**
 * For a single parameter, a <code>Split</code> {@link Muscle} divides it into a list of parameters. 
 * 
 * @author mleyton
 *
 * @param <P> The type of the input parameter.
 * @param <R> The type of the results in the list.
 */
public interface Split<P,R> extends Muscle<P,R> {

	/**
	 * Implements the division behavior.
	 * 
	 * <p><strong>The synchronized label should be used if this method cannot be executed concurrently!</strong>
	 * 
	 * Note however that synchronizing this method effectively serializes its execution (no parallelism), 
	 * and thus finer grain synchronizations should be used.</p>
	 * 
	 * @param param  The parameter to subdivide.
	 * @return The list of subdivided parameters
	 */
	public R[] split(P param) throws Exception;
}
