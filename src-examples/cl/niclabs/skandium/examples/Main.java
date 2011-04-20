package cl.niclabs.skandium.examples;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.examples.nqueensnaive.NQueens;
import cl.niclabs.skandium.examples.pi.PI;
import cl.niclabs.skandium.examples.quicksort.QuickSort;
import cl.niclabs.skandium.examples.mergesort.MergeSort;
import cl.niclabs.skandium.examples.bubblesort.BubbleSort;
import cl.niclabs.skandium.examples.strassen.Strassen;

public class Main {

	public static void main(String args[]) throws Exception{
		System.out.println("args length:"+args.length);
		if(args.length < 3 || args.length > 4){
			usage();
		}
		
		if(args[0].equalsIgnoreCase("pi")){
			String[] sub = {args[1],args[2],args[3]};
			PI.main(sub);
		}		
		else if(args[0].equalsIgnoreCase("nqueens")){
			String[] sub = {args[1],args[2],args[3]};
			NQueens.main(sub);
		}	
		else if(args[0].equalsIgnoreCase("strassen")){
			String[] sub = {args[1],args[2],args[3]};
			Strassen.main(sub);
		}
		else if(args[0].equalsIgnoreCase("quicksort")){
			String[] sub = {args[1],args[2]};
			QuickSort.main(sub);
		}
		else if(args[0].equalsIgnoreCase("mergesort")){
			String[] sub = {args[1],args[2]};
			MergeSort.main(sub);
		}
		else if(args[0].equalsIgnoreCase("bubblesort")){
			String[] sub = {args[1],args[2]};
			BubbleSort.main(sub);
		}
		else{
			usage();
		}
		
		System.exit(0);
	}
	
	private static void usage(){
		
		int threads = Runtime.getRuntime().availableProcessors();
		
		System.out.println("Usage:");
		System.out.println("");
		System.out.println("Pi:        java -jar skandium-"+Skandium.version()+"-examples.jar pi "+ threads+ " 2000 8");
		System.out.println("Pi:        java -jar skandium-"+Skandium.version()+"-examples.jar pi <threads> <decimals> <numparts>");
		System.out.println("");
		System.out.println("Nqueens:   java -jar skandium-"+Skandium.version()+"-examples.jar nqueens "+ threads+ " 15 3");
		System.out.println("Nqueens:   java -jar skandium-"+Skandium.version()+"-examples.jar nqueens <threads> <boardsize> <depth>");
		System.out.println("");
		System.out.println("Strassen:  java -jar skandium-"+Skandium.version()+"-examples.jar strassen "+ threads+ " 1024 128");
		System.out.println("Strassen:  java -jar skandium-"+Skandium.version()+"-examples.jar strassen <threads> <matrixsize> <submatrixsize>");
		System.out.println("");
		System.out.println("QuickSort: java -jar skandium-"+Skandium.version()+"-examples.jar quicksort "+ threads+ " 67108864");
		System.out.println("QuickSort: java -jar skandium-"+Skandium.version()+"-examples.jar quicksort <threads> <size>");
		System.out.println("");
		System.out.println("MergeSort: java -jar skandium-"+Skandium.version()+"-examples.jar mergesort "+ threads+ " 67108864");
		System.out.println("MergeSort: java -jar skandium-"+Skandium.version()+"-examples.jar mergesort <threads> <size>");
		System.out.println("");
		System.out.println("BubbleSort: java -jar skandium-"+Skandium.version()+"-examples.jar bubblesort "+ threads+ " 67108864");
		System.out.println("BubbleSort: java -jar skandium-"+Skandium.version()+"-examples.jar bubblesort <threads> <size>");
		
		System.exit(1);
	}
}
