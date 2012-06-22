package cl.niclabs.skandium.autonomic;

import java.util.HashMap;

class ChildrenState extends State {
	private HashMap<Integer,State> children;

	ChildrenState(int index) {
		super(index);
		children = new HashMap<Integer,State>();
	}

	private void addChild(State child) {
		children.put(child.getIndex(),child);
	}

	private boolean hasChild(State child) {
		return children.containsKey(child.getIndex());
	}

	@Override
	void childSetter(State child) {
		if (!hasChild(child)) {
			addChild(child);
		}
	}
	
	@Override
	int threadsCalculator() {
		if (isFinished) return 0;
		int threads = 1;
		for (State child : children.values()) {
			threads += child.threadsCalculator();
		}
		return threads;
	}
}
