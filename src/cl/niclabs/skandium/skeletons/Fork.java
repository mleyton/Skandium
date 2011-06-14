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

import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;
import cl.niclabs.skandium.system.events.BadListenerException;
import cl.niclabs.skandium.system.events.ForkListener;

/**
 * A <code>Fork</code> {@link Skeleton} divides an input parameter into a list of sub-parameters,
 * executes a different code on each sub-parameter in parallel, and reduces the results.
 * 
 * @author mleyton
 *
 * @param <P> The input type of the {@link Skeleton}.
 * @param <R> The result type of the {@link Skeleton}. 
 * */
public class Fork<P,R> extends AbstractSkeleton<P,R> {

	Split<P,?> split;
	Skeleton<?,?> skeletons[];
	Merge<?,R> merge;
	
	/**
	 * The constructor.
	 * 
	 * Note that the number of elements resulting from the division must match
	 * the number of codes to execute or an Exception will be raised. In other words:
	 * 
	 * <code>Split(param).length == skeletons.length</code> 
	 * 
	 * @param split Used to divide the parameter into sub-parameters
	 * @param skeletons  A list of skeletons to execute, one for each sub-parameter.
	 * @param merge The code used to merge the results of the computation into a single one.
	 */
	public <X,Y> Fork(Split<P,X> split, Skeleton<X,Y> skeletons[], Merge<Y,R> merge){
		super();
		this.split=split;
		this.skeletons=skeletons;
		this.merge=merge;
	}
	
	/**
	 * The constructor.
	 * 
	 * Note that the number of elements resulting from the division must match
	 * the number of codes to execute or an Exception will be raised. In other words:
	 * 
	 * <code>Split(param).length == executes.length</code> 
	 * 
	 * @param split Used to divide the parameter into sub-parameters
	 * @param executes  A list of codes to execute, one for each sub-parameter.
	 * @param merge The code used to merge the results of the computation into a single one.
	 */
	@SuppressWarnings("unchecked")
	public <X,Y> Fork(Split<P,X> split, Execute<X,Y> executes[], Merge<Y,R> merge){
		this(split,new Seq[executes.length], merge);
		
		for(int i=0;i<executes.length;i++){
			skeletons[i]=new Seq<X,Y>(executes[i]);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Register an event listener
     * @param <X> Type of the <code>param</code> after {@link Split}
     * @param <Y> Type of the <code>param</code> before {@link Merge}
     * @param l Event listener to register
     * @return true if the event listener registration was successful, false otherwise.
     * @throws BadListenerException
     */
    public <X,Y> boolean addListener(ForkListener<P,X,Y,R> l) throws BadListenerException {
    	return eregis.addListener(l);
    }

    /**
     * Remove an event listener
     * @param <X> Type of the <code>param</code> after {@link Split}
     * @param <Y> Type of the <code>param</code> before {@link Merge}
     * @param l Event listener to remove
     * @return true if the event listener removal was successful, false otherwise.
     * @throws BadListenerException
     */
    public <X,Y> boolean removeListener(ForkListener<P,X,Y,R> l) throws BadListenerException {
    	return eregis.removeListener(l);
    }

	public Skeleton<?, ?>[] getSkeletons() {
		return skeletons;
	}
}