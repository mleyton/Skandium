package cl.niclabs.skandium.examples.quicksort;

import java.util.Random;
import java.util.concurrent.Future;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.Stream;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Skeleton;

public class QuickSort {
	static Random random = new Random();
	
	public static void main(String args[]) throws Exception{
    	
		int THREADS  = Runtime.getRuntime().availableProcessors();
		int SIZE     = (int) Math.pow(2, 26);
		
		if(args.length != 0) {
			THREADS  = Integer.parseInt(args[0]);
    		SIZE     = Integer.parseInt(args[1]);
    	}
		
		System.out.println("Computing Quicksort threads="+THREADS+" size="+ SIZE + ".");
			
		// 1. Define the skeleton program structure
		Skeleton<Range, Range> sort = new DaC<Range, Range>(
				new ShouldSplit(SIZE/(THREADS*2)),  //threshold size for recursion
				new SplitList(), //number of parts to divide by
				new Sort(),
				new MergeList());

		Skandium skandium = new Skandium(THREADS);
		
		Stream<Range, Range> stream = skandium.newStream(sort);
		
		// 2. Input parameters with the defaults singleton Skandium object
	    long init = System.currentTimeMillis();
	    
		Future<Range> future = stream.input(new Range(generate(SIZE),0, SIZE-1));
	
		// 3. Do something else here.
		// ...
	
		// 4. Block for the results
		Range result = future.get();
		System.out.println((System.currentTimeMillis() - init)+"[ms]: "+result.array);
		for(int i=0;i<result.array.length-2;i++){
			if(result.array[i] > result.array[i+1]) throw new Exception("Not sorted! "+ i + " "+ (i+1));
		}
	
	}
	
	public static int[] generate(int size){
		
		int array[] = new int[size];

		for(int i=0;i<size;i++){
			array[i] = random.nextInt();
		}
		
		return array;
	}
}
