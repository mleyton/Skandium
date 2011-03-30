package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

interface NoParamListener extends EventListener {

	public boolean guard(Skeleton<?,?>[] strace);

	public void handler(Skeleton<?,?>[] strace);

}
