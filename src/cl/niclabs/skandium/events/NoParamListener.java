package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface NoParamListener extends EventListener {

	@SuppressWarnings("unchecked")
	public boolean guard(Object param, Skeleton[] strace);

	@SuppressWarnings("unchecked")
	public void handler(Object param, Skeleton[] strace);

}
