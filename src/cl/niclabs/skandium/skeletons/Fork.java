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

import cl.niclabs.skandium.events.IndexListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

/**
 * A <code>Fork</code> {@link cl.niclabs.skandium.skeletons.Skeleton} divides an input parameter into a list of sub-parameters,
 * executes a different code on each sub-parameter in parallel, and reduces the results.
 * 
 * @author mleyton
 *
 * @param <P> The input type of the {@link cl.niclabs.skandium.skeletons.Skeleton}.
 * @param <R> The result type of the {@link cl.niclabs.skandium.skeletons.Skeleton}. 
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
	
	public Split<P, ?> getSplit() {
		return split;
	}

	public Merge<?, R> getMerge() {
		return merge;
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

    public boolean addBeforeSplit(IndexListener<P> l) {
    	return eregis.addListener(When.BEFORE,Where.SPLIT,l);
    }

    public boolean removeBeforeSplit(IndexListener<P> l) {
    	return eregis.removeListener(When.BEFORE,Where.SPLIT,l);
    }

    public <X> boolean addAfterSplit(IndexListener<X[]> l) {
    	return eregis.addListener(When.AFTER,Where.SPLIT,l);
    }

    public <X> boolean removeAfterSplit(IndexListener<X[]> l) {
    	return eregis.removeListener(When.AFTER,Where.SPLIT,l);
    }

    public <Y> boolean addBeforeMerge(IndexListener<Y[]> l) {
    	return eregis.addListener(When.BEFORE,Where.MERGE,l);
    }

    public <Y> boolean removeBeforeMerge(IndexListener<Y[]> l) {
    	return eregis.removeListener(When.BEFORE,Where.MERGE,l);
    }

    public boolean addAfterMerge(IndexListener<R> l) {
    	return eregis.addListener(When.AFTER,Where.MERGE,l);
    }

    public boolean removeAfterMerge(IndexListener<R> l) {
    	return eregis.removeListener(When.AFTER,Where.MERGE,l);
    }

    public Skeleton<?, ?>[] getSkeletons() {
		return skeletons;
	}
}