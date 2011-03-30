package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

interface BooleanParamListener extends EventListener {

	public boolean guard(Skeleton<?,?>[] strace, boolean cresult);

	public void handler(Skeleton<?,?>[] strace, boolean cresult);

}
