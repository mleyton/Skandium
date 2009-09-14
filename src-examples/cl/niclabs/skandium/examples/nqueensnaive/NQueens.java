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

import java.util.Random;
import java.util.concurrent.Future;

import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.Stream;

/**
 * The main class to execute a naive NQueens counting algorithm which does not consider board symmetries.
 * 
 * @author mleyton
 */
public class NQueens{

	static Random random = new Random();
	
    public static void main(String[] args) throws Exception {
       
    	int THREADS = Runtime.getRuntime().availableProcessors();
    	int BOARD = 15;  //Size board of the board
    	int DEPTH = 3;
    	
    	if(args.length != 0) {
    		THREADS = Integer.parseInt(args[0]);
    		BOARD   = Integer.parseInt(args[1]);
    		DEPTH   = Integer.parseInt(args[2]);
    	}
    	
    	System.out.println("Computing NQueens threads="+THREADS+" board="+ BOARD+" depth="+DEPTH+ ".");
    	
    	//1. Define the skeleton program structure
    	 Skeleton<Board, Count> subskel = new DaC<Board, Count>(   //We use a divide and conquer skeleton pattern
    			 new ShouldDivide(DEPTH),  //Dive until the depth is "N-3" 
    			 new DivideBoard(), 
    			 new Solve(), 
    			 new ConquerCount());
    	
    	 Skeleton<Board, Count> nqueens = //Always subdivide the first row.
    		 new Map<Board, Count>(new DivideBoard(), subskel, new ConquerCount());
    	 
    	 //2. Create a new Skandium instance with 2 execution threads
         Skandium skandium = new Skandium(THREADS);

         //3. Open a Stream to input parameters
         Stream<Board, Count> stream = skandium.newStream(nqueens);
         
         //4. Input parameters
         long init = System.currentTimeMillis();
         Future<Count> future = stream.input(new Board(BOARD));

         //5. Do something else here.
         //...
         
         //6. Block for the results
         Count result = future.get();
         System.out.println(result+" in "+(System.currentTimeMillis() - init)+"[ms]");
         
         //7. Shutdown the system
         skandium.shutdown();
         
    }
}