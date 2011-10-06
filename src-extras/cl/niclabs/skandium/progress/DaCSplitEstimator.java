package cl.niclabs.skandium.progress;

public interface DaCSplitEstimator extends Estimator {
	
	public int estimate(Object param, Integer[] rbranch);

}
