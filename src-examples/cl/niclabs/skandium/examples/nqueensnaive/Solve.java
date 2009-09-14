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

import cl.niclabs.skandium.muscles.Execute;

public class Solve implements Execute<Board, Count> {

	@Override
	public Count execute(Board board) throws Exception {

		Count c = new Count(board.n);
		
		//trigger the backtracking
		backtrack(c, board.n, board.q, board.depth, board.horizontal, board.diagonalsRight, board.diagonalsLeft);
				
		return c;
	}
	
	void backtrack(Count c, int n, int q, int depth, int horizontal, long diagonalsRight, long diagonalsLeft){
		
		if(depth == n){
			c.column[q]++; 
			return;
		}
		
		for(int i = 0; i < n ; i++ ){
			
			int newHorizontal      = (1 << 31-i)                  | horizontal;
			long newDiagonalsRight = (1 << 61 - (n-1 + depth -i)) | diagonalsRight;
			long newDiagonalsLeft  = (1 << 61 - (i + depth))      | diagonalsLeft;
				
			if( (newHorizontal != horizontal) &&
				(newDiagonalsRight != diagonalsRight) &&
				(newDiagonalsLeft != diagonalsLeft)){
			
				backtrack(c, n, q, depth+1, newHorizontal,newDiagonalsRight, newDiagonalsLeft);	
			}
		}
	}
}