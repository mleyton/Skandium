package cl.niclabs.skandium.autonomic;

// TODO borrar el public de la clase y de los metodos
public class Box<T> {
	private T var;
	public Box(T var) {
		this.var = var;
	}
	public void set(T var) {
		this.var = var;
	}
	public T get() {
		return var;
	}
}
