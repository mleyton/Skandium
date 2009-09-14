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
package cl.niclabs.skandium.examples.strassen;

import cl.niclabs.skandium.muscles.Split;

public class DivideOperands implements Split<Operands,Operands>{

	@Override
	public Operands[] split(Operands p){
		
		int n = p.a.length();
		
		Matrix a11= p.a.part(0,0);
		Matrix a12= p.a.part(0,n/2);
		Matrix a21= p.a.part(n/2,0);
		Matrix a22= p.a.part(n/2,n/2);
		
		Matrix b11= p.b.part(0,0);
		Matrix b12= p.b.part(0,n/2);
		Matrix b21= p.b.part(n/2,0);
		Matrix b22= p.b.part(n/2,n/2);
		
		Operands[] results= {//All of these operands require multiplication
			
			new Operands(a11.add(a22), b11.add(b22)),
			new Operands(a21.add(a22), b11),
			new Operands(a11, b12.substract(b22)),
			new Operands(a22, b21.substract(b11)),
			new Operands(a11.add(a12), b22),
			new Operands(a21.substract(a11),b11.substract(b12)),
			new Operands(a12.substract(a22),b21.substract(b22))
		};
		 
		return results;
	}
}
