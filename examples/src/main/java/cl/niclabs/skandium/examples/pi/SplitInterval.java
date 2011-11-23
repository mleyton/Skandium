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
package cl.niclabs.skandium.examples.pi;

import cl.niclabs.skandium.muscles.Split;

public class SplitInterval implements Split<Interval,Interval> {

	int numParts;
	
	/**
	 * @param numParts The number of Intervals to create from an Interval
	 */
	public SplitInterval(int numParts){
		this.numParts=numParts;
	}
	
	@Override
	public Interval[] split(Interval param) throws Exception {
		
		int size = (param.end - param.start)/numParts;
		Interval[] result = new Interval[numParts];
		
		for(int i=0; i<numParts; i++){
			int start = param.start + size*i;
			int end   = i < (numParts-1) ? param.start + size*(i+1)-1 : param.end;
			
			result[i] = new Interval(start, end);
		}
		
		return result;
	}
}
