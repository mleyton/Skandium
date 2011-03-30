package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface NoParamListener extends EventListener {

	public <P> boolean guard(P param, Skeleton<?,?>[] strace);

	public <P> void handler(P param, Skeleton<?,?>[] strace);

}
