package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Condition;

public class MSCondition implements Condition<ArrayList<Integer>> {

	int maxTimes,times;
	
	public MSCondition(int maxTimes) {
		this.maxTimes = maxTimes;
		this.times = 0;
	}
	
	@Override
	public synchronized boolean condition(ArrayList<Integer> p) throws Exception {
		return p.size() > 1 && times++ < this.maxTimes;
	}

}
