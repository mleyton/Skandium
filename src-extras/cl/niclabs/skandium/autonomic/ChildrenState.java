package cl.niclabs.skandium.autonomic;

import java.util.Collection;
import java.util.HashMap;

import cl.niclabs.skandium.skeletons.Skeleton;

class ChildrenState extends State {
	private HashMap<Integer,State> children;

	ChildrenState(Skeleton<?, ?> skel, int index) {
		super(skel, index);
		children = new HashMap<Integer,State>();
	}

	Collection<State> getChildren() {
		return children.values();
	}
	

	void addChildren(State child) {
		children.put(child.getIndex(),child);
	}

	boolean hasChild(State child) {
		return children.containsKey(child.getIndex());
	}
}
