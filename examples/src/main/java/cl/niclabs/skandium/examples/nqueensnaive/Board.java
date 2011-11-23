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
package cl.niclabs.skandium.examples.nqueensnaive;

public class Board {

	int n; //board width and height
	int q; //position of first row queen
	int depth;
	
	int horizontal;
	long diagonalsRight;
	long diagonalsLeft;
	
	
	public Board(int n){
		this(n, 0 ,0, 0, 0, 0);
	}
	
	Board(int n, int q, int depth, int h, long dr, long dl){
				
		this.n=n;
		this.q=q;
		this.depth=depth;
		this.horizontal=h;
		this.diagonalsRight=dr;
		this.diagonalsLeft=dl;
		
	}
	
	public Board copy(int depth){
	
		Board b = new Board(this.n, this.q, depth, this.horizontal, this.diagonalsRight, this.diagonalsLeft);
		
		return b;
	}
}