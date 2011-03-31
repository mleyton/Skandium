package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface IntegerParamListener extends EventListener {

	@SuppressWarnings("unchecked")
	public boolean guard(Object param, Skeleton[] strace, int index);

	@SuppressWarnings("unchecked")
	public void handler(Object param, Skeleton[] strace, int index);

}
