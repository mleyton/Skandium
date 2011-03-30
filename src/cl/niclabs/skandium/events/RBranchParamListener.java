package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

interface RBranchParamListener extends EventListener {

	public boolean guard(Skeleton<?,?>[] strace, Integer[] rbranch);

	public void handler(Skeleton<?,?>[] strace, Integer[] rbranch);

}
