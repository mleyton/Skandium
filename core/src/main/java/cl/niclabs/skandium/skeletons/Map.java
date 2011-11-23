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

/**
 * A <code>Map</code> {@link Skeleton} divides an input parameter into a list of sub-parameters,
 * executes the same code on each sub-parameter in parallel, and reduces the results.
 * 
 * @author mleyton
 *
 * @param <P> The input type of the {@link Skeleton}.
 * @param <R> The result type of the {@link Skeleton}. 
 */
public class Map<P,R> extends AbstractSkeleton<P,R> {

	Split<P,?> split;
	Skeleton<?,?> skeleton;
	Merge<?,R> merge;
	
	/**
	 * The constructor.
	 * 
	 * @param split  The code to divide each input parameter.
	 * @param skeleton The code to execute on each sub-parameter.
	 * @param merge The code to reduce the results into a single one.
	 */
	public <X,Y> Map(Split<P,X> split, Skeleton<X,Y> skeleton, Merge<Y,R> merge){
		super();
		this.split=split;
		this.skeleton=skeleton;
		this.merge=merge;
	}
	
	/**
	 * The constructor.
	 * 
	 * @param split  The code to divide each input parameter.
	 * @param execute The code to execute on each sub-parameter.
	 * @param merge The code to reduce the results into a single one.
	 */
	@SuppressWarnings("unchecked")
	public <X,Y>  Map(Split<P,X> split, Execute<X,Y> execute, Merge<Y,R> merge){
		this(split,new Seq(execute), merge);
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}