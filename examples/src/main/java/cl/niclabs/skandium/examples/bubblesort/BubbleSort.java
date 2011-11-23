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
import java.util.Random;
import java.util.concurrent.Future;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Skeleton;

public class BubbleSort {
	static Random random = new Random();

	public static void main(String[] args) {

		int THREADS  = Runtime.getRuntime().availableProcessors();
		int SIZE     = (int) Math.pow(2, 26);

		if(args.length != 0) {
			THREADS  = Integer.parseInt(args[0]);
			SIZE     = Integer.parseInt(args[1]);
		}

		ArrayList<Integer> in = generate(SIZE);
		ArrayList<Integer> out;

		System.out.println("Computing BubbleSort threads="+THREADS+" size="+ SIZE + ".");

		Skeleton<ArrayList<Integer>, ArrayList<Integer>> bsort = new For<ArrayList<Integer>>(
				new BSExecute(), in.size());

		Skandium skandium = new Skandium(THREADS);
		
		long init = System.currentTimeMillis();
		
		Future<ArrayList<Integer>> future = bsort.input(in);
		try {
			out = future.get();
			System.out.println((System.currentTimeMillis() - init)+"[ms]: "+out.toArray());
			for(int i=0; i<out.size()-2;i++) {
				if(out.get(i) > out.get(i+1)) throw new Exception("Not sorted! "+ i + " "+ (i+1));
     			}
			
			skandium.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
        
	public static ArrayList<Integer> generate(int size){

		ArrayList<Integer> array = new ArrayList<Integer>(size);

		for(int i=0;i<size;i++){
			array.add(i, new Integer(random.nextInt()));
		}

		return array;
	}

}
