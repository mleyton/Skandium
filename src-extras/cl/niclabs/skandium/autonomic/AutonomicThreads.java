package cl.niclabs.skandium.autonomic;

import java.util.HashMap;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
public class AutonomicThreads {
	public static final long DEFAULT_POOL_CHECK = (long) 500000;
	public static final double RHO = (double) 0.5;
	public static final long DEFAULT_WALL_CLOCK_TIME_GOAL = (long) 0;
	
	public static void start(Skandium skandium, Skeleton<?,?> skel, long poolCheck, 
			HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, Integer> card, double rho,
			long wallClocktimeGoal, int threadLimit, boolean verboseMode) {
		((AbstractSkeleton<?,?>)skel).addGeneric(
				new Controller(skel, skandium, poolCheck, t, card, rho, wallClocktimeGoal, 
						threadLimit, verboseMode), Skeleton.class, null, null);
	}

	public static void start(Skandium skandium, Skeleton<?,?> skel) {
		start(skandium,skel, DEFAULT_POOL_CHECK, new HashMap<Muscle<?,?>,Long>(),
				new HashMap<Muscle<?,?>, Integer>(), RHO, DEFAULT_WALL_CLOCK_TIME_GOAL,
				Runtime.getRuntime().availableProcessors()*2, false);
	}

}
