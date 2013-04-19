/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2013 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */

package cl.niclabs.skandium.autonomic;

import java.util.HashMap;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.muscles.Muscle;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;

/**
 * Autonomic Threads Extension.
 * 
 * Its goal is to autonomically control the Skandium number of threads in order
 * to achieve a Wall Clock Time (WCT) goal  
 * (http://en.wikipedia.org/wiki/Wall_clock_time) with the following possible
 * cases:
 * 
 * 1) If with the current number of threads, the WCT goal wno't be achieved,
 *    the number of threads will be increased to the maximum level of 
 *    parallelism needed in order to do the best effort to achieve the WCT goal.
 * 2) If with the half of current number of threads, the WCT will be 
 *    achieved, the number of threads will be decreased in order to free 
 *    resources to another processes that could need them.
 * 
 * This two cases implies that it is needed to estimate the WCT in two cases,
 * (1) with an assumption of unlimited number of resources, in order to 
 * calculate the maximum level of parallelism needed to achieve the best WCT, 
 * and (2) with a fixed number threads. In order to calculate this WCTs 
 * it is needed to estimate the sequential execution time for each muscle 
 * involved in the skeleton's execution. And it is needed to calculate the 
 * estimation of the cardinality of "split"s and "condition"s muscles.  This 
 * estimation is done by a history (previous values) estimating algorithm:
 * 
 * new_estimated_value = rho*new_actual_value + (1-rho)*last_estimated_value
 * 
 * where rho, is a system parameter that gives weight to the last value with
 * respect to its previous values.
 * 
 * This version of the Autonomic Threads extension does not provide support for
 * If and Fork skeletons.
 * 
 * @author Gustavo Adolfo Pabón <gustavo.pabon@gmail.com>
 */
public class AutonomicThreads {
	
	/**
	 * DEFAULT_POLL_CHECK is the default value poll interval for the
	 * estimation upgrade in nanosecs.
	 */
	public static final long DEFAULT_POLL_CHECK = (long) 500000;
	
	/**
	 * RHO is the default value of RHO system parameter.
	 */
	public static final double RHO = (double) 0.5;
	
	/**
	 * DEFAULT_WALL_CLOCK_TIME_GOAL is the default value for the WCT goal.
	 * its value is 0 in order to force a best effort execution.
	 */
	public static final long DEFAULT_WALL_CLOCK_TIME_GOAL = (long) 0;
	
	/**
	 * Static method for starting the autonomic behavior that allows the 
	 * definition of system parameters.
	 * @param skandium instance of Skandium singleton that has the method for
	 * update the number of threads during an skeleton execution.
	 * @param skel skeleton that is about to starts its execution with 
	 * Autonomic behavior.
	 * @param poolCheck pool check value that defines the poll interval for the
	 * estimation upgrade in nanosecs.
	 * @param t Map that holds the function time "t", where t(f) is the 
	 * estimated execution time in nanosecs of muscle f.  This allows to set 
	 * the seeds for the estimating algorithm.
	 * @param card Map that holds the function "card", where card(f) is the 
	 * estimated cardinality of muscles split, and condition.  
	 * 
	 * For split, card(split) is the estimated length of the result array after
	 * the split execution.  
	 * 
	 * For condition muscle, in While skeleton, card(condition) is the 
	 * estimated times condition will return true.
	 * 
	 * For condition muscle, in DaC skeleton, card (condition) is the recursive 
	 * tree deep.
	 * 
	 * This allows to set the seeds for the estimating algorithm.
	 * @param rho rho system parameter, default value: 0.5 
	 * @param wallClocktimeGoal WCT goal, default value: 0 in order to force a 
	 * best effort execution.
	 * @param threadLimit It is possible that the level of parallelism needed
	 * could result in a very high number. This parameter imposes a limit for 
	 * the increase of threads.  Its default value is 2 times the number of 
	 * cores.
	 * @param verboseMode true, activates the verbose mode. Default value: 
	 * false
	 */
	public static void start(Skandium skandium, Skeleton<?,?> skel,
			long poolCheck, HashMap<Muscle<?,?>,Long> t, HashMap<Muscle<?,?>, 
			Integer> card, double rho, long wallClocktimeGoal, int threadLimit,
			boolean verboseMode) {
		((AbstractSkeleton<?,?>)skel).addGeneric(
				new Controller(skel, skandium, poolCheck, t, card, rho,
						wallClocktimeGoal, threadLimit, verboseMode),
						Skeleton.class, null, null);
	}

	/**
	 * Static method for starting the autonomic behavior that uses default 
	 * values for system parameters
	 * @param skandium instance of Skandium singleton that has the method for
	 * update the number of threads during an skeleton execution.
	 * @param skel skeleton that is about to starts its execution with 
	 * Autonomic behavior. 
	 */
	public static void start(Skandium skandium, Skeleton<?,?> skel) {
		start(skandium,skel, DEFAULT_POLL_CHECK, 
				new HashMap<Muscle<?,?>,Long>(), 
				new HashMap<Muscle<?,?>, Integer>(), RHO, 
				DEFAULT_WALL_CLOCK_TIME_GOAL,
				Runtime.getRuntime().availableProcessors()*2, false);
	}

}