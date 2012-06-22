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
 * A <code></code> {@link cl.niclabs.skandium.skeletons.Skeleton}
 * @author mleyton
 *
 * @param <P> The input type of the {@link cl.niclabs.skandium.skeletons.Skeleton}.
 * @param <R> The result type of the {@link cl.niclabs.skandium.skeletons.Skeleton}. 
 * */
public class Pipe<P,R> extends AbstractSkeleton<P,R> {

	Skeleton<P,?> stage1;
        Skeleton<?,R> stage2;
	
	public <X> Pipe(Skeleton<P,X> stage1, Skeleton<X,R> stage2){
		super();
		this.stage1=stage1;
		this.stage2=stage2;
	}
	
	public <X> Pipe(Execute<P,X> stage1,Execute<X,R> stage2){
		this(new Seq<P,X>(stage1),new Seq<X,R>(stage2));
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

	public Skeleton<P, ?> getStage1() {
		return stage1;
	}

	public Skeleton<?, R> getStage2() {
		return stage2;
	}
}
