package cl.niclabs.skandium.examples.quicksort;

import java.util.Arrays;

import cl.niclabs.skandium.muscles.Execute;

public class Sort implements Execute<Range, Range> {

	@Override
	public Range execute(Range r) throws Exception {
		
		if (r.right <= r.left) return r;
		
		Arrays.sort(r.array, r.left, r.right+1);
		
		return r;
	}
}