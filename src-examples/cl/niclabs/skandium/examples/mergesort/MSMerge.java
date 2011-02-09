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

import cl.niclabs.skandium.muscles.Merge;

public class MSMerge implements Merge<ArrayList<Integer>, ArrayList<Integer>> {

	@Override
	public ArrayList<Integer> merge(ArrayList<Integer>[] p) throws Exception {
		ArrayList<Integer> left = p[0];
		ArrayList<Integer> right = p[1];
		ArrayList<Integer> result = new ArrayList<Integer>();
		while(left.size()>0 || right.size()>0) {
			if (left.size()>0 && right.size()>0) {
				if(left.get(0) <= right.get(0)) {
					result.add(left.remove(0));
				} else {
					result.add(right.remove(0));
				}
			} else if (left.size()>0) {
				result.add(left.remove(0));
			} else if (right.size()>0) {
				result.add(right.remove(0));
			}
		}
		return result;
	}
}
