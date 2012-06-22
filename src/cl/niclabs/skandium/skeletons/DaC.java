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

import cl.niclabs.skandium.events.ParentConditionListener;
import cl.niclabs.skandium.events.ParentListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Execute;
import cl.niclabs.skandium.muscles.Merge;
import cl.niclabs.skandium.muscles.Split;

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
    
    public boolean addBeforeCondition(ParentListener<P> l) {
    	return eregis.addListener(When.BEFORE, Where.CONDITION, l);
    }

    public boolean removeBeforeCondition(ParentListener<P> l) {
    	return eregis.removeListener(When.BEFORE, Where.CONDITION, l);
    }

    public boolean addAfterCondition(ParentConditionListener<P> l) {
    	return eregis.addListener(When.AFTER, Where.CONDITION, l);
    }

    public boolean removeAfterCondition(ParentConditionListener<P> l) {
    	return eregis.removeListener(When.AFTER, Where.CONDITION, l);
    }

    public boolean addBeforeSplit(ParentListener<P> l) {
    	return eregis.addListener(When.BEFORE, Where.SPLIT, l);
    }

    public boolean removeBeforeSplit(ParentListener<P> l) {
    	return eregis.removeListener(When.BEFORE, Where.SPLIT, l);
    }

    public <X> boolean addAfterSplit(ParentListener<X[]> l) {
    	return eregis.addListener(When.AFTER, Where.SPLIT, l);
    }

    public <X> boolean removeAfterSplit(ParentListener<X[]> l) {
    	return eregis.removeListener(When.AFTER, Where.SPLIT, l);
    }

    public <Y> boolean addBeforeMerge(ParentListener<Y[]> l) {
    	return eregis.addListener(When.BEFORE, Where.MERGE, l);
    }

    public <Y> boolean removeBeforeMerge(ParentListener<Y[]> l) {
    	return eregis.removeListener(When.BEFORE, Where.MERGE, l);
    }

    public boolean addAfterMerge(ParentListener<R> l) {
    	return eregis.addListener(When.AFTER, Where.MERGE, l);
    }

    public boolean removeAfterMerge(ParentListener<R> l) {
    	return eregis.removeListener(When.AFTER, Where.MERGE, l);
    }

    public Skeleton<?, ?> getSkeleton() {
		return skeleton;
	}
    
}