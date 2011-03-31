package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface RBranchParamListener extends EventListener {

	@SuppressWarnings("unchecked")
	public boolean guard(Object param, Skeleton[] strace, Integer[] rbranch);

	@SuppressWarnings("unchecked")
	public void handler(Object param, Skeleton[] strace, Integer[] rbranch);

}
