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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.instructions.*;

/**
 * Using the visitor pattern, this class navigates a skeleton structure (ie nested skeletons),
 * and transforms the Skeletons into their Instructions representations.
 * 
 * @author mleyton
 */
public class StackBuilder implements SkeletonVisitor {

	public Stack<Instruction> stack;
	public Stack<StackTraceElement> strace;
	
	public StackBuilder() {
		super();
		this.stack = new Stack<Instruction>();
		this.strace = new Stack<StackTraceElement>();
	}

	public StackBuilder(Stack<StackTraceElement> trace){
		this();
		this.strace.addAll(trace);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		
		strace.add(skeleton.trace);
		
		skeleton.subskel.accept(this);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		
		//mark the trace of the pipe skeleton
		strace.add(skeleton.trace);

		//create a new stack builder for each stage
		StackBuilder stage1 = new StackBuilder(strace);
		StackBuilder stage2 = new StackBuilder(strace);
		
		//construct the stack
		skeleton.stage1.accept(stage1);
		skeleton.stage2.accept(stage2);
		
		//add the results to this stack (as there is no pipe instruction)
		stack.addAll(stage2.stack);  //second stage first
		stack.addAll(stage1.stack);  //first stage last
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {	
		strace.add(skeleton.trace);
		
		stack.push(new SeqInst(skeleton.execute, getStraceAsArray()));
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		
		strace.add(skeleton.trace);
		
		StackBuilder trueCaseStackBuilder = new StackBuilder(strace);
		StackBuilder falseCaseStackBuilder = new StackBuilder(strace);
		
		skeleton.trueCase.accept(trueCaseStackBuilder);
		skeleton.falseCase.accept(falseCaseStackBuilder);
		
		stack.push(new IfInst(skeleton.condition, trueCaseStackBuilder.stack, falseCaseStackBuilder.stack, getStraceAsArray()));
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		
		strace.add(skeleton.trace);
		StackBuilder subStackBuilder = new StackBuilder(strace);

		skeleton.subskel.accept(subStackBuilder);

		stack.push(new WhileInst(skeleton.condition, subStackBuilder.stack, getStraceAsArray())); 
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		
		strace.add(skeleton.trace);
		StackBuilder subStackBuilder = new StackBuilder(strace);

		skeleton.subskel.accept(subStackBuilder);

		stack.push(new ForInst(subStackBuilder.stack, skeleton.times, getStraceAsArray())); 
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		
		strace.add(skeleton.trace);
		StackBuilder subStackBuilder = new StackBuilder(strace);
		
		skeleton.skeleton.accept(subStackBuilder);
		
		stack.push(new MapInst(skeleton.split, subStackBuilder.stack ,skeleton.merge, getStraceAsArray()));
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		
		List <Stack<Instruction>> stacks= new ArrayList<Stack<Instruction>>();

		strace.add(skeleton.trace);
		for(Skeleton s:skeleton.skeletons){	
			StackBuilder subStackBuilder = new StackBuilder(strace);
			s.accept(subStackBuilder);
			stacks.add(subStackBuilder.stack);
		}
		stack.push(new ForkInst(skeleton.split, stacks ,skeleton.merge, getStraceAsArray()));
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		
		strace.add(skeleton.trace);
		StackBuilder subStackBuilder = new StackBuilder(strace);
		
		skeleton.skeleton.accept(subStackBuilder);
		
		stack.push(new DaCInst(skeleton.condition, skeleton.split, subStackBuilder.stack, skeleton.merge, getStraceAsArray()));
	}
	
	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}
	
	private Stack<StackTraceElement> copyStackTrace(){
		Stack<StackTraceElement> strace = new Stack<StackTraceElement>();
		strace.addAll(this.strace);
		
		return strace;
	}
	
	private StackTraceElement[] getStraceAsArray(){
		return strace.toArray(new StackTraceElement[strace.size()]);
	}
}
