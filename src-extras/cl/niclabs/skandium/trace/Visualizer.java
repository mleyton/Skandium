package cl.niclabs.skandium.trace;


import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Visualizer {
	
	private Controller controller;
	
	public Visualizer(Skeleton<?, ?> skeleton) {
		super();
		controller = new Controller((AbstractSkeleton<?,?>) skeleton);
	}

	public boolean open() {
		return controller.open();
	}
	
}
