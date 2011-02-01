package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Merge;

public class MSMerge implements Merge<ArrayList<Integer>, ArrayList<Integer>> {

	@Override
	public ArrayList<Integer> merge(ArrayList<Integer>[] p) throws Exception {
		ArrayList<Integer> left = p[0];
		ArrayList<Integer> right = p[1];
		ArrayList<Integer> result = new ArrayList<Integer>();
		while(left.size()>0 || right.size()>0) {
			if (left.size()>0 && right.size()>0) {
				if(left.get(0) <= right.get(0)) {
					result.add(left.remove(0));
				} else {
					result.add(right.remove(0));
				}
			} else if (left.size()>0) {
				result.add(left.remove(0));
			} else if (right.size()>0) {
				result.add(right.remove(0));
			}
		}
		return result;
	}
}
