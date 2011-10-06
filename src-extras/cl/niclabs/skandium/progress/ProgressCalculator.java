package cl.niclabs.skandium.progress;

import java.util.HashMap;

import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class ProgressCalculator {
		
	public static void setup(Skeleton<?,?> skel, ProgressListener l, HashMap<Muscle<?,?>, Estimator> estimators) {
		SkeletonListener.register(new Controller(l,skel, estimators), (AbstractSkeleton<?,?>) skel);
	}
	
	public static void setup(Skeleton<?,?> skel, ProgressListener l) {
		setup(skel,l, new HashMap<Muscle<?,?>, Estimator>());
	}

}
