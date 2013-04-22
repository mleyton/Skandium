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

import java.util.Collections;
import java.util.TreeMap;

/**
 * Structure that holds information about intervals of time and concurrency
 * on each interval for two cases: best effort and fifo.
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 *
 */
class TimeLine {
	
	/*
	 * The internal structure used was a TreeMap in order to keep a ordered
	 * list of of intervals.  For example
	 * 
	 * tl:
	 * 
	 * 0 -> 1
	 * 2 -> 2
	 * 3 -> 1
	 * 4 -> 0
	 * 
	 * Means that the interval [0,2) there are 1 activity occurring in 
	 * concurrently.  Interval [2,3) there are 2, interval [3,4) one and
	 * 4 is the total execution time.
	 * 
	 */
	private TreeMap<Long,Integer> tl;
	
	TimeLine() {
		tl = new TreeMap<Long,Integer>();
	}
	
	/**
	 * Adds and activity for best effort analisys
	 * @param a Activity to add for best effort analisys
	 */
	void addActivity(Activity a) {
		addActivity(a.getTi(),a.getTf());
	}
	
	/*
	 * Internal addition of an activity that started (or will start) at "ti" 
	 * time and finished (or will finish) at "tf" time to the "tl" struture.  
	 */
	private void addActivity(long ti, long tf) {
		if (tl.size()==0) {
			tl.put(ti, 1);
			tl.put(tf, 0);
			return;
		}
		if (ti > tl.lastKey() || ti < tl.firstKey()) tl.put(ti, 0);
		else if (!tl.containsKey(ti)) tl.put(ti, tl.get(tl.floorKey(ti)));
		for (long l : tl.subMap(ti, tf).keySet()){
			tl.put(l, tl.get(l)+1);
		}
		if (tf > tl.lastKey()) tl.put(tf, 0);
		if (!tl.containsKey(tf)) tl.put(tf, tl.get(tl.floorKey(tf))-1);
	}
	
	/**
	 * Adds an activity for fifo analisys
	 * @param a Activity to add
	 * @param maxThreads maximum concurrency allowed for the analysis
	 */
	void addActivity(Activity a, int maxThreads) {
		if (maxThreads < 1) throw new RuntimeException("Should not be here!");
		long ti = a.getTi();
		long d = a.getMuscleDuration();
		if (!tl.isEmpty()&&(((ti+d) > tl.firstKey()) && (ti < tl.lastKey()))) {
			boolean fits = false;
			while (!fits && ti < tl.lastKey()) {
				int max = Collections.max(tl.subMap(ti, ti+d).values());
				if ((max+1)>maxThreads) {
					ti = tl.higherKey(ti);
				} else fits = true; 
			}
		}
		addActivity(ti,ti+d);
		a.setTi(ti);
		a.setTf(ti+d);
	}
	
	/**
	 * Return of the maximum concurrency as result of the best effort or fifo
	 * analysis.
	 * 
	 * @param from From what point of time get the maximum concurrency.  This 
	 * parameter allows to filter actual execution from the estimated one.
	 * @return Maximum concurrency expected from "from" to the end of the 
	 * execution.
	 */
	int maxThreads(long from) {
		return Collections.max(tl.tailMap(from).values());
	}
}
