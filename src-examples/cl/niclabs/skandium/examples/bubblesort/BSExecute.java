package cl.niclabs.skandium.examples.bubblesort;

import java.util.ArrayList;

import cl.niclabs.skandium.muscles.Execute;

public class BSExecute implements
		Execute<ArrayList<Integer>, ArrayList<Integer>> {

	int times;
	
	public BSExecute() {
		this.times = 0;
	}

	@Override
	public ArrayList<Integer> execute(ArrayList<Integer> p) throws Exception {
		int n = p.size();
		for (int i = n-1; i > times; i--) {
			if (p.get(i - 1).compareTo(p.get(i)) > 0) {
				Integer tmp = p.get(i - 1);
				p.set(i - 1, p.get(i));
				p.set(i, tmp);
			}
		}
		times++;
		return p;
	}

}
