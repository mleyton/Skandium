package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
public class AutonomicThreads {
	public static final float DEFAULT_YELLOW_THRESHOLD = (float) 0.7;
	public static final float DEFAULT_RED_THRESHOLD = (float) 0.8;
	public static final long DEFAULT_POOL_CHECK = (long) 500;
	
	public static void start(Skandium skandium, Skeleton<?,?> skel, float yellowThreshold, float redThresHold, long poolCheck) {
		((AbstractSkeleton<?,?>)skel).addGeneric(new Controller(skel, skandium, yellowThreshold, redThresHold, poolCheck), Skeleton.class, null, null);
	}

	public static void start(Skandium skandium, Skeleton<?,?> skel) {
		start(skandium,skel, DEFAULT_YELLOW_THRESHOLD, DEFAULT_RED_THRESHOLD, DEFAULT_POOL_CHECK);
	}

}
