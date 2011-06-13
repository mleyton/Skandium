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
import cl.niclabs.skandium.system.events.IfListener;
import cl.niclabs.skandium.system.events.NoParamListener;
import cl.niclabs.skandium.system.events.SkeletonListener;

/**
 * Abstract class intended to be extended in order to include a Listener to the
 * before {@link If} event.
 * 
 * @param <P> <code>param</code> type before {@link If} is executed
 * @param <R> <code>param</code> type after {@link If} is executed
 */
public abstract class IfBeforeListener<P,R> extends NoParamListener<P> implements IfListener<P,R>, BeforeListener, SkeletonListener {

}
