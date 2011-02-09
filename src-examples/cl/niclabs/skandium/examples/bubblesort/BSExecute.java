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

package cl.niclabs.skandium.examples.bubblesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Execute;

public class BSExecute implements
		Execute<ArrayList<Integer>, ArrayList<Integer>> {

	int times;
	
	public BSExecute() {
		this.times = 0;
	}

	@Override
	public ArrayList<Integer> execute(ArrayList<Integer> p) throws Exception {
		int n = p.size();
		for (int i = n-1; i > times; i--) {
			if (p.get(i - 1).compareTo(p.get(i)) > 0) {
				Integer tmp = p.get(i - 1);
				p.set(i - 1, p.get(i));
				p.set(i, tmp);
			}
		}
		times++;
		return p;
	}

}
