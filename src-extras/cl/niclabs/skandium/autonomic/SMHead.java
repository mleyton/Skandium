package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class SMHead {
	static final int UDEF = -1;
	@SuppressWarnings("rawtypes")
	private Skeleton[] strace;
	private int index;
	private int parent;
	@SuppressWarnings("rawtypes")
	SMHead(Skeleton[] strace) {
		this.strace = strace;
		index = UDEF;
		parent = UDEF;
	}
	void setIndex(int index) {
		this.index = index;
	}
	void setParent(int parent) {
		this.parent = parent;
	}
	@SuppressWarnings("rawtypes")
	Skeleton[] getStrace() {
		return strace;
	}
	int getIndex() {
		return index;
	}
	int getParent() {
		return parent;
	}
}
