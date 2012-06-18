package cl.niclabs.skandium.autonomic;

import java.util.List;

class SplitState extends SkeletonState {
	private boolean isBeforeMerge;
	private boolean isBeforeSplit;
	private List<Integer> subIndexes;
}
