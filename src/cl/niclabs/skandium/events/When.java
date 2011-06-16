/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2011 NIC Labs, Universidad de Chile.
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
package cl.niclabs.skandium.events;

/**
 * Enumeration that define constants to specify the temporal dimension of events.
 * An event can be uniquely identified by {@link cl.niclabs.skandium.skeletons.Skeleton}, {@link Where} dimension, 
 * and {@link When} dimension.  For example DaC, Condition, After; or Seq, Nested Skeleton, 
 * Before.
 */
public enum When {
	/**
	 * Constant that defines a Before Event
	 */
	BEFORE, 

	/**
	 * Constant that defines an After Event
	 */
	AFTER }
