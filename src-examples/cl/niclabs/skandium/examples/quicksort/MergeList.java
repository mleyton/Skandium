package cl.niclabs.skandium.examples.quicksort;

import cl.niclabs.skandium.muscles.Merge;

public class MergeList implements Merge<Range, Range>{

	@Override
	public Range merge(Range[] r) throws Exception {
		
		Range result = new Range( r[0].array, r[0].left, r[1].right);
		
		return result;
	}
}