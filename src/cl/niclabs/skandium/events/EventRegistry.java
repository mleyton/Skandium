package cl.niclabs.skandium.events;

import java.util.concurrent.PriorityBlockingQueue;


public class EventRegistry {
	private PriorityBlockingQueue<EventListener>[] listeners;

	
	@SuppressWarnings("unchecked")
	public EventRegistry() {
		listeners = new PriorityBlockingQueue[Where.values().length * When.values().length];
	}

	public boolean addListener(NonGenericListener e) throws BadListenerException {
		int index = getIndex(getWhen(e), getWhere(e));
		if (listeners[index] == null) listeners[index] = new PriorityBlockingQueue<EventListener>();
		return listeners[index].add(e);
	}
	
	public boolean addListener(When when, Where where, GenericListener e) {
		int index = getIndex(when, where);
		if (listeners[index] == null) listeners[index] = new PriorityBlockingQueue<EventListener>();
		return listeners[index].add(e);
	}
	
	public boolean removeListener(NonGenericListener e) throws BadListenerException {
		return listeners[getIndex(getWhen(e), getWhere(e))].remove(e);
	}

	public boolean removeListener(When when, Where where, GenericListener e) {
		return listeners[getIndex(when, where)].remove(e);
	}

	public EventListener[] getListeners(When when, Where where) {
		PriorityBlockingQueue<EventListener> l = listeners[getIndex(when, where)];
		if (l == null) return null;
		return l.toArray(new EventListener[l.size()]);
	}

	private int getIndex(When when, Where where) {
		return where.ordinal() + Where.values().length * when.ordinal();
	}
	
	private Where getWhere(EventListener e) throws BadListenerException {
		if (e instanceof SkeletonListener) return Where.SKELETON;
		if (e instanceof NestedSkelListener) return Where.NESTED_SKELETON;
		if (e instanceof ConditionListener) return Where.CONDITION;
		if (e instanceof SplitListener) return Where.SPLIT;
		if (e instanceof MergeListener) return Where.MERGE;
		throw new BadListenerException();
	}

	private When getWhen(EventListener e) throws BadListenerException {
		if (e instanceof BeforeListener) return When.BEFORE;
		if (e instanceof AfterListener) return When.AFTER;
		throw new BadListenerException();
	}
}
