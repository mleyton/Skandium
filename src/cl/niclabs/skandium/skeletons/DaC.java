/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2009 NIC Labs, Universidad de Chile.
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
package cl.niclabs.skandium.skeletons;

import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.system.events.BadListenerException;
import cl.niclabs.skandium.system.events.DaCListener;

/**
 * A Divide and Conquer <code>Dac</code> skeleton provides a recursive parallelism pattern.
 * 
 * An input parameter is subdivided into smaller parameters until a condition is reached. 
 * Then each sub-parameter is computed in parallel, and the results are merged back into a single result. 
 * 
 * @author mleyton
 *
 * @param <P> The type of the input parameter.
 * @param <R> The type of the result.
 */
public class DaC<P,R> extends AbstractSkeleton<P,R> {
	
	Condition<P> condition;
	Split<P,?> split;
	Skeleton<?,?> skeleton;
	Merge<?,R> merge;
	
	/**
	 * The constructor.
	 * 
	 * @param condition the parameter will be subdivided while this condition holds true.
	 * @param split the code to subdivide a parameter.
	 * @param skeleton the skeleton code to execute when the subdivision process is finished. 
	 * @param merge the code to reduce the results into a single output.
	 */
	public <X,Y> DaC(Condition<P> condition, Split<P,X> split, Skeleton<X,Y> skeleton, Merge<Y,R> merge){
    	
		this.condition = condition;
		this.split=split;
		this.skeleton=skeleton;
		this.merge=merge;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param condition the parameter will be subdivided while this condition holds true.
	 * @param split the code to subdivide a parameter.
	 * @param execute the code to execute when the subdivision process is finished. 
	 * @param merge the code to reduce the results into a single output.
	 */
	public <X,Y> DaC(Condition<P> condition, Split<P,X> split, Execute<X,Y> execute, Merge<Y,R> merge){
		this(condition, split,new Seq<X,Y>(execute), merge);
	}
	
	
	public Condition<P> getCondition() {
		return condition;
	}

	public Split<P, ?> getSplit() {
		return split;
	}

	public Merge<?, R> getMerge() {
		return merge;
	}

	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Register an event listener
     * @param <X> Type of the <code>param</code> after {@link cl.niclabs.skandium.muscles.Split}
     * @param <Y> Type of the <code>param</code> before {@link cl.niclabs.skandium.muscles.Merge}
     * @param l Event listener to register
     * @return true if the event listener registration was successful, false otherwise.
     * @throws BadListenerException
     */
    public <X,Y> boolean addListener(DaCListener<P,X,Y,R> l) throws BadListenerException {
    	return eregis.addListener(l);
    }

    /**
     * Remove an event listener
     * @param <X> Type of the <code>param</code> after {@link cl.niclabs.skandium.muscles.Split}
     * @param <Y> Type of the <code>param</code> before {@link cl.niclabs.skandium.muscles.Merge}
     * @param l Event listener to remove
     * @return true if the event listener removal was successful, false otherwise.
     * @throws BadListenerException
     */
    public <X,Y> boolean removeListener(DaCListener<P,X,Y,R> l) throws BadListenerException {
    	return eregis.removeListener(l);
    }

	public Skeleton<?, ?> getSkeleton() {
		return skeleton;
	}
    
}