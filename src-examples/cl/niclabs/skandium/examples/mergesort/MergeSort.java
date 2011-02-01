package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Future;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Skeleton;

public class MergeSort {
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

		System.out.println("Computing Mergesort threads="+THREADS+" size="+ SIZE + ".");

		Skeleton<ArrayList<Integer>, ArrayList<Integer>> msort = new DaC<ArrayList<Integer>, ArrayList<Integer>>(
				new MSCondition(SIZE/(THREADS*2)), new MSSplit(), new MSExecute(),
				new MSMerge());
		
		Skandium skandium = new Skandium(THREADS);

		long init = System.currentTimeMillis();
		Future<ArrayList<Integer>> future = msort.input(in);
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
