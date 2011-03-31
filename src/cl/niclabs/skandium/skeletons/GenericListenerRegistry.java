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
