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
import cl.niclabs.skandium.system.events.BadListenerException;
import cl.niclabs.skandium.system.events.SeqListener;

/**
 * A Seq skeleton is used to terminate recursive nesting of {@link Skeleton}s.
 * It simply wraps an {@link Execute} {@link Muscle} to be nested inside another skeleton. 
 * 
 * @author mleyton
 *
 * @param <P> The input type of the {@link Skeleton}.
 * @param <R> The result type of the {@link Skeleton}.
 */
public class Seq<P,R> extends AbstractSkeleton<P,R> {

	Execute<P,R> execute;
	
	public Seq(Execute <P,R> execute){
		this.execute = execute;
	}
	
	/**
	 * {@inheritDoc}
	 */
    public void accept(SkeletonVisitor visitor) {
        visitor.visit(this);
    }

    public boolean addListener(SeqListener l) throws BadListenerException {
    	return eregis.addListener(l);
    }

    public boolean removeListener(SeqListener l) throws BadListenerException {
    	return eregis.removeListener(l);
    }
}