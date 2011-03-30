package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface RBranchBooleanParamListener extends EventListener {

	public <P> boolean guard(P param, Skeleton<?,?>[] strace, Integer[] rbranch, boolean cresult);

	public <P> void handler(P param, Skeleton<?,?>[] strace, Integer[] rbranch, boolean cresult);

}
