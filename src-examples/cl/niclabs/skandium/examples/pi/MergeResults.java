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

import java.math.BigDecimal;

import cl.niclabs.skandium.muscles.Merge;

public class MergeResults implements Merge<BigDecimal, BigDecimal>{

	@Override
	public BigDecimal merge(BigDecimal[] param) throws Exception {
		
		BigDecimal total = new BigDecimal(0);

        for (int i = 0; i < param.length; i++) {
        	total = total.add(param[i]);
        }

        return total;
	}
}
