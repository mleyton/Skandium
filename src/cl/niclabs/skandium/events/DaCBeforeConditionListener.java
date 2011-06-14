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

import cl.niclabs.skandium.system.events.BeforeListener;
import cl.niclabs.skandium.system.events.ConditionListener;
import cl.niclabs.skandium.system.events.DaCListener;
import cl.niclabs.skandium.system.events.RBranchParamListener;

/**
 * Abstract class intended to be extended in order to include a {@link SkandiumEventListener} to the
 * {@link DaC} {@link When#BEFORE} {@link Where#CONDITION} event.
 * 
 * @param <P> <code>param</code> type before {@link DaC} is executed
 * @param <X> Type of the <code>param</code> after {@link Split}
 * @param <Y> Type of the <code>param</code> before {@link Merge}
 * @param <R> <code>param</code> type after {@link DaC} is executed
 */

public abstract class DaCBeforeConditionListener<P,X,Y,R> extends RBranchParamListener<P> implements DaCListener<P,X,Y,R>, BeforeListener, ConditionListener {

}
