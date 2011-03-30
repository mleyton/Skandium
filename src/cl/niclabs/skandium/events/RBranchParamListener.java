package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface RBranchParamListener extends EventListener {

	public <P> boolean guard(P param, Skeleton<?,?>[] strace, Integer[] rbranch);

	public <P> void handler(P param, Skeleton<?,?>[] strace, Integer[] rbranch);

}
