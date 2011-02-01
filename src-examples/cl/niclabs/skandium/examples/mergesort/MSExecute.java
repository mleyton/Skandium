package cl.niclabs.skandium.examples.mergesort;

import java.util.ArrayList;
import java.util.Collections;

import cl.niclabs.skandium.muscles.Execute;

public class MSExecute implements Execute<ArrayList<Integer>, ArrayList<Integer>> {

	@Override
	public ArrayList<Integer> execute(ArrayList<Integer> p) throws Exception {
		if (p.size()>1) {
			Collections.sort(p);
		}
		return p;
	}

}
