package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Split;

public class MSSplit implements Split<ArrayList<Integer>, ArrayList<Integer>> {

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer>[] split(ArrayList<Integer> p) throws Exception {
		int middle = p.size() / 2;
		ArrayList<Integer> left = new ArrayList<Integer>();
		ArrayList<Integer> right = new ArrayList<Integer>();
		for (int i=0; i<middle; i++) {
			left.add(p.get(i));
		}
		for (int i=middle; i<p.size(); i++) {
			right.add(p.get(i));
		}
		ArrayList<Integer>[] r = new ArrayList[2]; 
		r[0] = new ArrayList<Integer>(left);
		r[1] = new ArrayList<Integer>(right);
		return r;
	}

}
