package cl.niclabs.skandium.progress;

import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class ProgressCalculator {
		
	public static void setup(Skeleton<?,?> skel, ProgressListener l) {
		SkeletonListener.register(new Controller(l), (AbstractSkeleton<?,?>) skel);
	}

}
