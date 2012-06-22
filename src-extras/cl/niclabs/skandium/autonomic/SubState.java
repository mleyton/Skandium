package cl.niclabs.skandium.autonomic;

class SubState extends State {
	
	protected State subState;
	
	SubState(int index) {
		super(index);
	}

	@Override
	void childSetter(State child) {
		subState = child;
	}
	
	@Override
	int threadsCalculator() {
		if (isFinished) return 0;
		if (subState == null) return 1;
		return subState.threadsCalculator();
	}
}
