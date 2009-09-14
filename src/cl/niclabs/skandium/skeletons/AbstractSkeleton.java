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
	
	protected AbstractSkeleton(){		
		trace  = getInitStackElement();
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
}