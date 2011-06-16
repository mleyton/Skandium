/*   Skandium: A Java(TM) based parallel skeleton library. 
 *   
 *   Copyright (C) 2011 NIC Labs, Universidad de Chile.
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
package cl.niclabs.skandium.events;

import cl.niclabs.skandium.system.events.AfterListener;
import cl.niclabs.skandium.system.events.DaCListener;
import cl.niclabs.skandium.system.events.RBranchParamListener;
import cl.niclabs.skandium.system.events.SplitListener;

/**
 * Abstract class intended to be extended in order to include a {@link cl.niclabs.skandium.system.events.SkandiumEventListener} to the
 * {@link cl.niclabs.skandium.skeletons.DaC} {@link When#AFTER} {@link Where#SPLIT} event.
 * 
 * @param <P> <code>param</code> type before {@link cl.niclabs.skandium.skeletons.DaC} is executed
 * @param <X> Type of the <code>param</code> after {@link cl.niclabs.skandium.muscles.Split}
 * @param <Y> Type of the <code>param</code> before {@link cl.niclabs.skandium.muscles.Merge}
 * @param <R> <code>param</code> type after {@link cl.niclabs.skandium.skeletons.DaC} is executed
 */

public abstract class DaCAfterSplitListener<P,X,Y,R> extends RBranchParamListener<X[]> implements DaCListener<P,X,Y,R>, AfterListener, SplitListener {

}
