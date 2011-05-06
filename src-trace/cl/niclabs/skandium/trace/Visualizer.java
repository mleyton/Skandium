package cl.niclabs.skandium.trace;


import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.Skeleton;

public class Visualizer {
	
	private boolean running;
	private Controller controller;
	
	public Visualizer(Skandium skandium, Skeleton<?, ?> skeleton) {
		super();
		controller = new Controller(skandium, (AbstractSkeleton<?,?>)skeleton);
		this.running = false;
	}

	public boolean open() {
		if (!running) {			
			running = controller.open();
		}
		return running;
	}
	
	public boolean stop() {
		if (running) {
			running = !controller.close();
		}
		return !running;
	}
}
