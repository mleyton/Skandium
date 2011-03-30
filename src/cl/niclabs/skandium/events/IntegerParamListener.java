package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

interface IntegerParamListener extends EventListener {

	public boolean guard(Skeleton<?,?>[] strace, int index);

	public void handler(Skeleton<?,?>[] strace, int index);

}
