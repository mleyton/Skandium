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
import java.util.concurrent.Future;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.Stream;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Skeleton;

/**
 * <p>The main entry point to compute Pi with a given number of decimals.</p>
 * 
 * <p>The operations (add, multiply, etc..) are performed using BigDecimal types, 
 * which are slower when precision (scale) is increased. Hence, this application experiences
 * superlinear speedups.</p>
 * 
 * <p>To see this behavior play around with the SplitInterval(x), where x is 1, 2, 4, 8, 16, etc. 
 * The bigger the number of subintervals, the smaller the time waiting for the last subinterval to finish, which
 * is the most expensive in CPU time.</p>
 * 
 * @author mleyton
 */
public class PI {

	public static void main(String[] args) throws Exception {
    	
		int THREADS  = Runtime.getRuntime().availableProcessors();
		int DECIMALS = 3000;  //Number of decimals to compute
    	int PARTS = Runtime.getRuntime().availableProcessors()*4; //Number of parts to divide the interval
    	
    	if(args.length != 0) {
    		THREADS  = Integer.parseInt(args[0]);
    		DECIMALS = Integer.parseInt(args[1]);
    		PARTS    = Integer.parseInt(args[2]);
    	}
    	System.out.println("Computing Pi threads="+THREADS+" decimals="+ DECIMALS+" parts="+PARTS+ ".");
    	
    	Skandium skandium = new Skandium(THREADS);
    	
		// 1. Define the skeleton program structure
		Skeleton<Interval, BigDecimal> pi = new Map<Interval, BigDecimal>(
				new SplitInterval(PARTS), //number of parts to divide by
				new PiComputer(),
				new MergeResults());

		Stream<Interval, BigDecimal> stream = skandium.newStream(pi);
		
		// 2. Input parameters with the defauls singleton Skandium object
        long init = System.currentTimeMillis();
		Future<BigDecimal> future = stream.input(new Interval(0, DECIMALS));

		// 3. Do something else here.
		// ...

		// 4. Block for the results
		BigDecimal result = future.get();
		System.out.println((System.currentTimeMillis() - init)+"[ms]: "+result);
	}
}
