package cl.niclabs.skandium.progress;

import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.system.events.SkeletonTraceElement;
import cl.niclabs.skandium.skeletons.While;
import cl.niclabs.skandium.system.events.GenericListener;


class SkeletonListener {
	
	static void register(final Controller c, AbstractSkeleton<?,?> root) {
		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Seq<?,?> curr = (Seq<?,?>)strace[strace.length - 1].getSkel();
				c.seqm(strace, curr.getExecute());
				return param;
			}
			
		}, Seq.class, When.AFTER, Where.SKELETON);
		
		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				If<?,?> curr = (If<?,?>)strace[strace.length - 1].getSkel();
				c.ifm(strace, curr.getCondition(), (Boolean) params[0]);
				return param;
			}
			
		}, If.class, When.AFTER, Where.CONDITION);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				While<?> curr = (While<?>)strace[strace.length - 1].getSkel();
				c.whilem(strace, curr.getCondition(), (Integer) params[0], (Boolean) params[1], param);
				return param;
			}
			
		}, While.class, When.AFTER, Where.CONDITION);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Map<?,?> curr = (Map<?,?>)strace[strace.length - 1].getSkel();
				c.mapm(strace, curr.getSplit(), ((Object[]) param).length);
				return param;
			}
			
		}, Map.class, When.AFTER, Where.SPLIT);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Map<?,?> curr = (Map<?,?>)strace[strace.length - 1].getSkel();
				c.mapm(strace, curr.getMerge());
				return param;
			}
			
		}, Map.class, When.AFTER, Where.MERGE);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Fork<?,?> curr = (Fork<?,?>)strace[strace.length - 1].getSkel();
				c.forkm(strace, curr.getSplit());
				return param;
			}
			
		}, Fork.class, When.AFTER, Where.SPLIT);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Fork<?,?> curr = (Fork<?,?>)strace[strace.length - 1].getSkel();
				c.forkm(strace, curr.getMerge());
				return param;
			}
			
		}, Fork.class, When.AFTER, Where.MERGE);

		root.addGeneric(new GenericListener() {

			@SuppressWarnings("unchecked")
			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				c.dacm(strace, curr.getCondition(), (Stack<Integer>) params[0], (Boolean) params[1], param);
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.CONDITION);

		root.addGeneric(new GenericListener() {

			@SuppressWarnings("unchecked")
			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				c.dacm(strace, curr.getSplit(), (Stack<Integer>) params[0], (Object[]) param);
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.SPLIT);

		root.addGeneric(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				c.dacm(strace, curr.getMerge());
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.MERGE);

	}

}
