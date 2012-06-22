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

import cl.niclabs.skandium.events.ConditionListener;
import cl.niclabs.skandium.events.IndexListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.muscles.Condition;
import cl.niclabs.skandium.muscles.Execute;

/**
 * A <code></code> {@link cl.niclabs.skandium.skeletons.Skeleton}
 * @author mleyton
 *
 * @param <P> The input and output type of the {@link cl.niclabs.skandium.skeletons.Skeleton}.
 * */
public class While<P> extends AbstractSkeleton<P,P> {

	Skeleton<P,P> subskel;
	Condition<P> condition;
	
	public While(Skeleton<P,P> skeleton, Condition<P> condition){
		super();
		this.subskel=skeleton;
		this.condition = condition;
	}
	
	public While(Execute<P,P> execute, Condition<P> condition){
		this(new Seq<P,P>(execute), condition);
	}
	
	public Condition<P> getCondition() {
		return condition;
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    public boolean addBeforeCondition(IndexListener<P> l) {
    	return eregis.addListener(When.BEFORE, Where.CONDITION, l);
    }

    public boolean removeBeforeCondition(IndexListener<P> l) {
    	return eregis.removeListener(When.BEFORE, Where.CONDITION, l);
    }

    public boolean addAfterCondition(ConditionListener<P> l) {
    	return eregis.addListener(When.AFTER, Where.CONDITION, l);
    }

    public boolean removeAfterCondition(ConditionListener<P> l) {
    	return eregis.removeListener(When.AFTER, Where.CONDITION, l);
    }

	public Skeleton<P, P> getSubskel() {
		return subskel;
	}
}
