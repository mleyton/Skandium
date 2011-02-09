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

package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Split;

public class MSSplit implements Split<ArrayList<Integer>, ArrayList<Integer>> {

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer>[] split(ArrayList<Integer> p) throws Exception {
		int middle = p.size() / 2;
		ArrayList<Integer> left = new ArrayList<Integer>();
		ArrayList<Integer> right = new ArrayList<Integer>();
		for (int i=0; i<middle; i++) {
			left.add(p.get(i));
		}
		for (int i=middle; i<p.size(); i++) {
			right.add(p.get(i));
		}
		ArrayList<Integer>[] r = new ArrayList[2]; 
		r[0] = new ArrayList<Integer>(left);
		r[1] = new ArrayList<Integer>(right);
		return r;
	}

}
