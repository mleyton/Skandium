package cl.niclabs.skandium.autonomic;

class Box<T> {
	private T var;
	Box(T var) {
		this.var = var;
	}
	void set(T var) {
		this.var = var;
	}
	T get() {
		return var;
	}
}
