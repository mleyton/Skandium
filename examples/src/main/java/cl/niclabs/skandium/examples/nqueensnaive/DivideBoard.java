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

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Split;

public class DivideBoard implements Split<Board, Board> {

	@Override
	public Board[] split(Board board) throws Exception {
		
		ArrayList<Board> boards = new ArrayList<Board>();
		
		for(int i = 0; i < board.n; i++){
			
			Board b           = board.copy(board.depth+1);
			
			if(board.depth == 0){ //if its the first row we remember the queens position
				b.q=i;
			}
			
			b.horizontal     |= (1 << 31-i);
			b.diagonalsRight |= (1 << 61 - (board.n-1 + board.depth -i));
			b.diagonalsLeft  |= (1 << 61 - (i + board.depth));
			
			if(	board.horizontal     !=  b.horizontal &&
				board.diagonalsRight !=  b.diagonalsRight &&
				board.diagonalsLeft  !=  b.diagonalsLeft ){
				
				boards.add(b);
			}
		}

		return boards.toArray(new Board[boards.size()]);
	}
}