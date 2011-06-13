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
import cl.niclabs.skandium.system.events.MapListener;
import cl.niclabs.skandium.system.events.NoParamListener;
import cl.niclabs.skandium.system.events.SplitListener;

/**
 * Abstract class intended to be extended in order to include a Listener to the
 * {@link Map} before split skeleton event.
 * 
 * @param <P> <code>param</code> type before {@link Map} is executed
 * @param <X> Type of the <code>param</code> after {@link Split}
 * @param <Y> Type of the <code>param</code> before {@link Merge}
 * @param <R> <code>param</code> type after {@link Map} is executed
 */
public abstract class MapBeforeSplitListener<P,X,Y,R> extends NoParamListener<P> implements MapListener<P,X,Y,R>, BeforeListener, SplitListener {

}
