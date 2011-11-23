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
 * For a parameter a <code>Condition</code> {@link Muscle} returns true or false. 
 * 
 * @author mleyton
 *
 * @param <P> The type of the parameter.
 */
public interface Condition<P> extends Muscle<P, Boolean> {

	/**
	 * Implements the conditional behavior.
	 * 
	 * <p><strong>The synchronized label should be used if this method cannot be executed concurrently!</strong>
	 * 
	 * Note however that synchronizing this method effectively serializes its execution (no parallelism), 
	 * and thus finer grain synchronizations should be used.</p>
	 * 
	 * @param param The parameter data to analyze.
	 * @return true or false.
	 */
	public boolean condition(P param) throws Exception;
}
