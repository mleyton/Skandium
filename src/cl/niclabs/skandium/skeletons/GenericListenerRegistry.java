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
package cl.niclabs.skandium.skeletons;

import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;

class GenericListenerRegistry implements SkeletonVisitor {
	
	private boolean remove;
	@SuppressWarnings("unchecked")
	private Class pattern;
	private When when;
	private Where where;
	private GenericListener listener;
	private boolean r;
	
	@SuppressWarnings("unchecked")
	public GenericListenerRegistry(boolean remove, Class pattern, When when,
			Where where, GenericListener listener) {
		super();
		this.remove = remove;
		this.pattern = pattern;
		this.when = when;
		this.where = where;
		this.listener = listener;
		this.r = false;
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.subskel.accept(this);
	}
	
	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.stage1.accept(this);		
		skeleton.stage2.accept(this);		
	}


	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.trueCase.accept(this);		
		skeleton.falseCase.accept(this);		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.subskel.accept(this);		
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.subskel.accept(this);		
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.skeleton.accept(this);		
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		for (Skeleton<?,?> s: skeleton.skeletons) {
			s.accept(this);
		}		
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		boolean ret = listenerRegistry(skeleton); 
		r = r || ret;
		skeleton.skeleton.accept(this);		
	}

	@SuppressWarnings("unchecked")
	private boolean listenerRegistry(Skeleton skeleton) {
		boolean ret= false;
		if (pattern.isInstance(skeleton)) {
			When[] narray = { when };
			Where[] rarray = { where };
			if (when == null) narray = When.values();
			if (where == null) rarray = Where.values();
			for (When n: narray) {
				for (Where r: rarray) {
					if (remove) {
						boolean t = ((AbstractSkeleton) skeleton).eregis.removeListener(n, r, listener);
						ret = ret || t;
					}
					else {
						boolean t = ((AbstractSkeleton) skeleton).eregis.addListener(n, r, listener);
						ret = ret || t;
					}
				}
			}
		}
		return ret;
	}


	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	boolean getR() {
		return r;
	}
}
