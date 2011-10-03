package cl.niclabs.skandium.system.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public class SkeletonTraceElement {
	
	private Skeleton<?,?> skel;
	private int id;
			
	public SkeletonTraceElement(Skeleton<?, ?> skel, int id) {
		super();
		this.skel = skel;
		this.id = id;
	}

	public Skeleton<?, ?> getSkel() {
		return skel;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		int sh = skel.hashCode();
		long n = id;
		n = n << Integer.SIZE;
		n = n + sh;
		return new Long(n).hashCode();
	}

}
