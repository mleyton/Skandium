package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface IntegerParamListener extends EventListener {

	public <P> boolean guard(P param, Skeleton<?,?>[] strace, int index);

	public <P> void handler(P param, Skeleton<?,?>[] strace, int index);

}
