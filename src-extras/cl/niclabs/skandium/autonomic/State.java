package cl.niclabs.skandium.autonomic;

class State {
	private int index;
	protected boolean isFinished;
	
	State(int index) {
		this.index = index;
		isFinished = false;
	}

	int getIndex() {
		return index;
	}

	void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
	void childSetter(State child) {
	}
	
	int threadsCalculator(){
		if (isFinished) return 0;
		return 1;
	}
	
}
