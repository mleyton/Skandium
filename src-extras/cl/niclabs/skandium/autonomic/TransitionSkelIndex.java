package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class TransitionSkelIndex {
	static final int UDEF = -1;
	@SuppressWarnings("rawtypes")
	private Skeleton[] strace;
	private int index;
	@SuppressWarnings("rawtypes")
	TransitionSkelIndex(Skeleton[] strace) {
		this.strace = strace;
		index = UDEF;
	}
	void setIndex(int index) {
		this.index = index;
	}
	@SuppressWarnings("rawtypes")
	Skeleton[] getStrace() {
		return strace;
	}
	int getIndex() {
		return index;
	}
}
