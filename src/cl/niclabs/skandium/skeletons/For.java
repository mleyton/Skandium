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
import cl.niclabs.skandium.muscles.Muscle;

/**
 * A <code>For</code> {@link cl.niclabs.skandium.skeletons.Skeleton} provides a fixed iteration.
 * The nested skeleton code is executed a fixed amount of times. 
 * 
 * @author mleyton
 *
 * @param <P> The input and result type of the {@link cl.niclabs.skandium.skeletons.Skeleton}.
 * */
public class For<P> extends AbstractSkeleton<P,P> {

	Skeleton<P,P> subskel;
	int times;
	
	/**
	 * The constructor.
	 * 
	 * @param skeleton The skeleton pattern to execute.
	 * @param times The number of times to execute the skeleton.
	 */
	public For(Skeleton<P,P> skeleton, int times){
		super();
		this.subskel=skeleton;
		this.times = times;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param execute The skeleton pattern to execute.
	 * @param times The number of times to execute the {@link Muscle}.
	 */
	public For(Execute<P,P> execute, int times){
		this(new Seq<P,P>(execute), times);
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

	public Skeleton<P, P> getSubskel() {
		return subskel;
	}

	public int getTimes() {
		return times;
	}
    
}