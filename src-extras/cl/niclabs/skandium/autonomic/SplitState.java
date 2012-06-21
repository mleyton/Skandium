package cl.niclabs.skandium.autonomic;

import cl.niclabs.skandium.skeletons.Skeleton;

class SplitState extends ChildrenState {
	private boolean isBeforeMerge;
	private boolean isBeforeSplit;
	SplitState(Skeleton<?, ?> skel, int index) {
		super(skel, index);
	}
	boolean isBeforeMerge() {
		return isBeforeMerge;
	}
	boolean isBeforeSplit() {
		return isBeforeSplit;
	}
	void setBeforeMerge(boolean isBeforeMerge) {
		this.isBeforeMerge = isBeforeMerge;
	}
	void setBeforeSplit(boolean isBeforeSplit) {
		this.isBeforeSplit = isBeforeSplit;
	}
}
