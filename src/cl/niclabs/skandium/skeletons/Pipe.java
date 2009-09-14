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

/**
 * A <code></code> {@link Skeleton}
 * @author mleyton
 *
 * @param <P> The input type of the {@link Skeleton}.
 * @param <R> The result type of the {@link Skeleton}. 
 * */
public class Pipe<P,R> extends AbstractSkeleton<P,R> {

	Skeleton<P,R> stage1, stage2;
	
	public Pipe(Skeleton<P,R> stage1, Skeleton<P,R> stage2){
		super();
		this.stage1=stage1;
		this.stage2=stage2;
	}
	
	public Pipe(Execute<P,R> stage1,Execute<P,R> stage2){
		this(new Seq<P,R>(stage1),new Seq<P,R>(stage2));
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }
}
