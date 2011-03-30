package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

interface RBranchBooleanParamListener extends EventListener {

	public boolean guard(Skeleton<?,?>[] strace, Integer[] rbranch, boolean cresult);

	public void handler(Skeleton<?,?>[] strace, Integer[] rbranch, boolean cresult);

}
