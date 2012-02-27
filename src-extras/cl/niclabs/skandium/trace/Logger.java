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

package cl.niclabs.skandium.trace;


import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Logger {
	
	private AbstractSkeleton<?,?> skeleton;
	private PlainHandler handler;
	private boolean running;
	private SkeletonListener listener;
	
	public Logger(Skeleton<?, ?> skeleton) {
		super();
		this.skeleton = (AbstractSkeleton<?,?>) skeleton;
		this.handler = new PlainHandler();
		this.running = false;
		this.listener = new SkeletonListener(handler);
	}

	public boolean start() {
		if (!running) {			
			running = skeleton.addGeneric(listener, Skeleton.class, null, null);
		}
		return running;
	}
	
	public boolean stop() {
		if (running) {
			running = !skeleton.removeGeneric(listener, Skeleton.class, null, null);
		}
		return !running;
	}
}
