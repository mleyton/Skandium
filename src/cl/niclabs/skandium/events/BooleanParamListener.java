package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface BooleanParamListener extends EventListener {

	public <P> boolean guard(P param, Skeleton<?,?>[] strace, boolean cresult);

	public <P> void handler(P param, Skeleton<?,?>[] strace, boolean cresult);

}
