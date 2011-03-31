package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface RBranchBooleanParamListener extends EventListener {

	@SuppressWarnings("unchecked")
	public boolean guard(Object param, Skeleton[] strace, Integer[] rbranch, boolean cresult);

	@SuppressWarnings("unchecked")
	public void handler(Object param, Skeleton[] strace, Integer[] rbranch, boolean cresult);

}
