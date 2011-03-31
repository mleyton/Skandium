package cl.niclabs.skandium.events;

import cl.niclabs.skandium.skeletons.Skeleton;

public interface UndefinedParamListener extends EventListener {

	@SuppressWarnings("unchecked")
	public boolean guard(Object param, Skeleton[] strace, When when, Where where, Object... params);

	@SuppressWarnings("unchecked")
	public void handler(Object param, Skeleton[] strace,  When when, Where where, Object... params);

}
