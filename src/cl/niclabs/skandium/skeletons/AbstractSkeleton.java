/*   Skandium: A Java(TM) based parallel skeleton library.
 *   
 *   Copyright (C) 2009 NIC Labs, Universidad de Chile.
 * 
 *   Skandium is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Skandium is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with Skandium.  If not, see <http://www.gnu.org/licenses/>.
 */
package cl.niclabs.skandium.skeletons;

import java.util.concurrent.Future;

import cl.niclabs.skandium.Skandium;
import cl.niclabs.skandium.Stream;
import cl.niclabs.skandium.events.GenericListener;
import cl.niclabs.skandium.events.IndexListener;
import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.system.events.GenericListenerRegistry;
import cl.niclabs.skandium.system.events.PatternEventRegistry;
import cl.niclabs.skandium.system.events.SkandiumEventListener;

/**
 * Abstract skeleton class from which all skeletons extends.
 *  
 * @author mleyton
 *
 * @param <P> The input type of the skeleton.
 * @param <R> The output type of the skeleton.
 */
public abstract class AbstractSkeleton<P,R> implements Skeleton<P,R> {
	
	//holds reference to source code instantiation, for skeleton logical exceptions.
	StackTraceElement trace;
	PatternEventRegistry eregis;
	
	protected AbstractSkeleton(){		
		trace  = getInitStackElement();
		eregis = new PatternEventRegistry();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<R> input(P param){
		
		Skandium skandium = Skandium.getSingleton();
		
		Stream<P,R> stream = skandium.newStream(this);
		
		return stream.input(param);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Future<R>[] input(P param[]){
		Skandium skandium = Skandium.getSingleton();
		
		Stream<P,R> stream = skandium.newStream(this);
		
		return stream.input(param);
	}
	
	/**
	 * @return A representation of the line where this skeleton was instantiated.
	 */
	private StackTraceElement getInitStackElement(){
		
		StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
		
		boolean start = false;
		
		/* Loop until we find the end of the first exception block of this class.
		  	cl.niclabs.skandium.skeletons.DaC
			java.lang.Thread.getStackTrace(Thread.java:1452)
			cl.niclabs.skandium.skeletons.AbstractSkeleton.<init>(AbstractSkeleton.java:44)
			cl.niclabs.skandium.skeletons.DaC.<init>(DaC.java:51)
			cl.niclabs.skandium.skeletons.DaC.<init>(DaC.java:68)   
			cl.niclabs.skandium.examples.nqueensnaive.NQueens.main(NQueens.java:53) <== This is the line we are looking for
		 */
		int i;
		for(i=0;i<stackElements.length;i++){
			if(stackElements[i].getClassName().equals(this.getClass().getCanonicalName())){
				start = true;
			}else if(start){
				break;
			}
		}
		
		//We mix the calling stackElement with the instantiation stackElement
		String className = stackElements[i-1].getClassName();  //cl.niclabs.skandium.skeletons.DaC
		String method    = stackElements[i-1].getMethodName(); //<init>
		String file      = stackElements[i].getFileName();     //NQueens.java
		int line         = stackElements[i].getLineNumber();   //53
			
		return new StackTraceElement(className, method, file, line);
	}
	
	public StackTraceElement getTrace() {
		return trace;
	}
	
	public PatternEventRegistry getEregis() {
		return eregis;
	}

	/**
	 * Returns the listeners related the event: this {@link cl.niclabs.skandium.skeletons.Skeleton}, {@link When} and {@link Where}.  
	 * An event is uniquely identified by this tree parameters. 
	 * @param when Defines the event {@link When} dimension, it could be {@link When#BEFORE} or {@link When#AFTER} 
	 * @param where Defines the event {@link Where} dimension, it could be {@link Where#SKELETON}, {@link Where#CONDITION}, {@link Where#SPLIT}, {@link Where#NESTED_SKELETON} or {@link Where#MERGE}
	 * @return Array of {@link cl.niclabs.skandium.system.events.SkandiumEventListener}s related to the event.
	 */
    public SkandiumEventListener[] getListeners(When when, Where where) {
		return eregis.getListeners(when, where);
    }
    
    /**
     * Register a {@link GenericListener} l
     * @param l Listener to be registered
     * @param pattern If its value is <code>Skeleton.class</code>, this listener is registered to 
     * all nested skeletons from this current skeleton (included).  If <code>pattern</code> is an 
     * specific skeleton class, the listener is registered to all nested skeleton instances of 
     * such class (i.e. <code>Seq.class</code>)  
     * @param when If <code>null</code> is passed as value, the listener is registered to all 
     * {@link When#BEFORE} and {@link When#AFTER} events.  If <code>when</code> has a specific value 
     * of {@link When} enumeration the listener is registered to all events related to that value. 
     * (i.e. All {@link When#AFTER} events) 
     * @param where If <code>null</code> is passed as value, the listener is registered to all 
     * {@link Where} events.  If <code>where</code> has a specific value 
     * of {@link Where} enumeration the listener is registered to all events related to that value. 
     * (i.e. All {@link Where#CONDITION} related events)
     * @return true if listener <code>l</code> has been registered successfully, false otherwise
     */
    @SuppressWarnings("rawtypes")
	public boolean addGeneric(GenericListener l, Class pattern, When when, Where where) {
    	GenericListenerRegistry gres = new GenericListenerRegistry(false, pattern, when, where, l);
    	this.accept(gres);
    	return gres.getR();
    }

    /**
     * Removes a {@link GenericListener} l
     * @param l Listener to be removed
     * @param pattern If its value is <code>Skeleton.class</code>, the listener related to 
     * all nested skeletons from this current skeleton (included) is removed.  
     * If <code>pattern</code> is an specific skeleton class, the listener related to all nested 
     * skeleton instances of such class (i.e. <code>Seq.class</code>) is removed   
     * @param when If <code>null</code> is passed as value, the listener related to all 
     * {@link When#BEFORE} and {@link When#AFTER} events is removed.  If <code>when</code> has a 
     * specific value of {@link When} enumeration, the listener related to all events related to 
     * that value (i.e. All {@link When#AFTER} events) is removed. 
     * @param where If <code>null</code> is passed as value, the listener related to all 
     * {@link Where} events is removed.  If <code>where</code> has a specific value 
     * of {@link Where} enumeration, the listener related to that value is removed. 
     * @return true if listener <code>l</code> has been removed successfully, false otherwise
     */
    @SuppressWarnings("rawtypes")
	public boolean removeGeneric(GenericListener l, Class pattern, When when, Where where) {
    	GenericListenerRegistry gres = new GenericListenerRegistry(true, pattern, when, where, l);
    	this.accept(gres);
    	return gres.getR();
    }
    
    public boolean addBefore(IndexListener<P> l) {
    	return eregis.addListener(When.BEFORE, Where.SKELETON, l);
    }

    public boolean removeBefore(IndexListener<P> l) {
    	return eregis.removeListener(When.BEFORE, Where.SKELETON, l);
    }

    public boolean addAfter(IndexListener<R> l) {
    	return eregis.addListener(When.AFTER, Where.SKELETON, l);
    }

    public boolean removeAfter(IndexListener<R> l) {
    	return eregis.removeListener(When.AFTER, Where.SKELETON, l);
    }
}