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
 * A <code>Fork</code> {@link Skeleton} divides an input parameter into a list of sub-parameters,
 * executes a different code on each sub-parameter in parallel, and reduces the results.
 * 
 * @author mleyton
 *
 * @param <P> The input type of the {@link Skeleton}.
 * @param <R> The result type of the {@link Skeleton}. 
 * */
public class Fork<P,R> extends AbstractSkeleton<P,R> {

	Split<P,R> split;
	Skeleton<P,R> skeletons[];
	Merge<R,R> merge;
	
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
	public Fork(Split<P,R> split, Skeleton<P,R> skeletons[], Merge<R,R> merge){
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
	public Fork(Split<P,R> split, Execute<P,R> executes[], Merge<R,R> merge){
		this(split,new Seq[executes.length], merge);
		
		for(int i=0;i<executes.length;i++){
			skeletons[i]=new Seq<P,R>(executes[i]);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}