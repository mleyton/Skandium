package cl.niclabs.skandium.examples.quicksort;

import cl.niclabs.skandium.muscles.Condition;

public class ShouldSplit implements Condition<Range>{

	int threshold;
	
	public ShouldSplit(int threshold){
		this.threshold=threshold;
	}
	
	@Override
	public boolean condition(Range r) throws Exception {
		
		return r.right - r.left > threshold;
	}
}
