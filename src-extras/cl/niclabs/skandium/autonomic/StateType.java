/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2013 NIC Labs, Universidad de Chile.
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

package cl.niclabs.skandium.autonomic;

/**
 * Enumeration of Types of States
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 *
 */
enum StateType {
	/**
	 * Initial state for all skeletons
	 */
	I, 
	
	/**
	 * Final state for all skeletons
	 */
	F, 
	
	/**
	 * After split state for Map, and before split state for DaC
	 */
	S, 
	
	/**
	 * Before merge state for Map and DaC
	 */
	M, 
	
	/**
	 * After condition state for DaC
	 */
	C, 
	
	/**
	 * True case on while and after split state for DaC 
	 */
	T, 
	
	/**
	 * Fase case, after condition, for DaC
	 */
	G
}
