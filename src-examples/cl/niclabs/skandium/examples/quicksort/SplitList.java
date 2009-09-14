package cl.niclabs.skandium.examples.quicksort;

import cl.niclabs.skandium.muscles.Split;

public class SplitList implements Split<Range, Range>{
	
	@Override
	public Range[] split(Range r) throws Exception {

        if (r.right <= r.left) throw new IllegalArgumentException("Right is smaller than Left");
        
        int i = partition(r.array, r.left, r.right);
        
        Range[] intervals = {new Range(r.array, r.left, i-1), new Range(r.array, i+1, r.right)};
        
        return intervals;
	}   
	
	public static int partition(int[] a, int left, int right) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (less(a[++i], a[right]))      // find item on left to swap
				;                               // a[right] acts as sentinel
			while (less(a[right], a[--j]))      // find item on right to swap
				if (j == left) break;           // don't go out-of-bounds
			if (i >= j) break;                  // check if pointers cross
			exch(a, i, j);                      // swap two elements into place
		}
		exch(a, i, right);                      // swap with partition element
		return i;
	}

	private static boolean less(int x,  int y) {
		return (x < y);
	}

	private static void exch(int[] a, int i, int j) {
		int swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}
}