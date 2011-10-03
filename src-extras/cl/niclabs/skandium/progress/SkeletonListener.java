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
		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Seq<?,?> curr = (Seq<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.seqm(curr.getExecute());
				return param;
			}
			
		}, Seq.class, When.AFTER, Where.SKELETON);
		
		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				If<?,?> curr = (If<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.ifm(curr.getCondition(), (Boolean) params[0]);
				return param;
			}
			
		}, If.class, When.AFTER, Where.CONDITION);

		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				While<?> curr = (While<?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.whilem(curr.getCondition(), (Integer) params[0], (Boolean) params[1]);
				return param;
			}
			
		}, While.class, When.AFTER, Where.CONDITION);

		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Map<?,?> curr = (Map<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.mapm(curr.getSplit());
				return param;
			}
			
		}, Map.class, When.AFTER, Where.SPLIT);

		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Map<?,?> curr = (Map<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.mapm(curr.getMerge());
				return param;
			}
			
		}, Map.class, When.AFTER, Where.MERGE);

		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Fork<?,?> curr = (Fork<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.forkm(curr.getSplit());
				return param;
			}
			
		}, Fork.class, When.AFTER, Where.SPLIT);

		root.addListener(new GenericListener() {

			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				Fork<?,?> curr = (Fork<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.forkm(curr.getMerge());
				return param;
			}
			
		}, Fork.class, When.AFTER, Where.MERGE);

		root.addListener(new GenericListener() {

			@SuppressWarnings("unchecked")
			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.dacm(curr.getCondition(), (Stack<Integer>) params[0], (Boolean) params[1]);
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.CONDITION);

		root.addListener(new GenericListener() {

			@SuppressWarnings("unchecked")
			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.dacm(curr.getSplit(), (Stack<Integer>) params[0], ((Object[]) param).length);
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.SPLIT);

		root.addListener(new GenericListener() {

			@SuppressWarnings("unchecked")
			@Override
			public Object handler(Object param, SkeletonTraceElement[] strace, 
					When when, Where where, Object... params) {
				DaC<?,?> curr = (DaC<?,?>)strace[strace.length - 1].getSkel();
				for (SkeletonTraceElement s:strace) {
					System.out.println(s.getSkel().getClass().getSimpleName() + ": " + s.getSkel() + " id: " + s.getId());
				}
				c.dacm(curr.getMerge(), (Stack<Integer>) params[0]);
				return param;
			}
			
		}, DaC.class, When.AFTER, Where.MERGE);

	}

}
