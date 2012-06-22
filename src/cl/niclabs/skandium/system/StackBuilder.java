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
package cl.niclabs.skandium.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.events.When;
import cl.niclabs.skandium.events.Where;
import cl.niclabs.skandium.instructions.DaCInst;
import cl.niclabs.skandium.instructions.EventInst;
import cl.niclabs.skandium.instructions.ForInst;
import cl.niclabs.skandium.instructions.ForkInst;
import cl.niclabs.skandium.instructions.IfInst;
import cl.niclabs.skandium.instructions.Instruction;
import cl.niclabs.skandium.instructions.MapInst;
import cl.niclabs.skandium.instructions.SeqInst;
import cl.niclabs.skandium.instructions.WhileInst;
import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.Skeleton;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;
import cl.niclabs.skandium.system.events.EventIdGenerator;

/**
 * Using the visitor pattern, this class navigates a skeleton structure (ie
 * nested skeletons), and transforms the Skeletons into their Instructions
 * representations.
 * 
 * @author mleyton
 */
public class StackBuilder implements SkeletonVisitor {

	public Stack<Instruction> stack;
	@SuppressWarnings("rawtypes")
	public Stack<Skeleton> strace;
	int parent;

	@SuppressWarnings("rawtypes")
	public StackBuilder() {
		super();
		this.stack = new Stack<Instruction>();
		this.strace = new Stack<Skeleton>();
		this.parent = 0;
	}

	public StackBuilder(@SuppressWarnings("rawtypes") Stack<Skeleton> trace, int parent) {
		this();
		this.strace.addAll(trace);
		this.parent = parent;
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {

		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();
		int oldParent = parent;
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, oldParent));
		parent = id;
		skeleton.getSubskel().accept(this);
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, oldParent));

	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {

		// mark the trace of the pipe skeleton
		strace.add(skeleton);

		// event ids
		int id = EventIdGenerator.getSingleton().increment();

		// create a new stack builder for each stage
		StackBuilder stage1 = new StackBuilder(strace,id);
		StackBuilder stage2 = new StackBuilder(strace,id);

		// construct the stack
		skeleton.getStage1().accept(stage1);
		skeleton.getStage2().accept(stage2);
		
		// add the results to this stack (as there is no pipe instruction)
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.addAll(stage2.stack); // second stage first
		stack.addAll(stage1.stack); // first stage last
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new SeqInst(skeleton.getExecute(), straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {

		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();

		StackBuilder trueCaseStackBuilder = new StackBuilder(strace,id);
		StackBuilder falseCaseStackBuilder = new StackBuilder(strace,id);

		skeleton.getTrueCase().accept(trueCaseStackBuilder);
		skeleton.getFalseCase().accept(falseCaseStackBuilder);

		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new IfInst(skeleton.getCondition(), trueCaseStackBuilder.stack,
				falseCaseStackBuilder.stack, straceArray, id, parent));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P> void visit(While<P> skeleton) {

		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();
		StackBuilder subStackBuilder = new StackBuilder(strace,id);
		
		skeleton.getSubskel().accept(subStackBuilder);
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new WhileInst(skeleton.getCondition(), subStackBuilder.stack, straceArray, id, parent));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P> void visit(For<P> skeleton) {

		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();		
		StackBuilder subStackBuilder = new StackBuilder(strace,id);

		skeleton.getSubskel().accept(subStackBuilder);
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new ForInst(subStackBuilder.stack, skeleton.getTimes(), straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {

		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();
		StackBuilder subStackBuilder = new StackBuilder(strace,id);

		skeleton.getSkeleton().accept(subStackBuilder);
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new MapInst(skeleton.getSplit(), subStackBuilder.stack, skeleton.getMerge(), straceArray, id, parent));
		stack.push(new EventInst(When.BEFORE,Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		strace.add(skeleton);
		int id = EventIdGenerator.getSingleton().increment();

		List<Stack<Instruction>> stacks = new ArrayList<Stack<Instruction>>();

		for (Skeleton<?, ?> s : skeleton.getSkeletons()) {
			StackBuilder subStackBuilder = new StackBuilder(strace,id);
			s.accept(subStackBuilder);
			stacks.add(subStackBuilder.stack);
		}
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
				
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, id, false, parent));
		stack.push(new ForkInst(skeleton.getSplit(), stacks, skeleton.getMerge(), straceArray, id, parent));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, id, false, parent));
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {

		strace.add(skeleton);
		StackBuilder subStackBuilder = new StackBuilder(strace,parent);

		skeleton.getSkeleton().accept(subStackBuilder);
		
		@SuppressWarnings("rawtypes")
		Skeleton[] straceArray = getStraceAsArray();
		stack.push(new DaCInst(skeleton.getCondition(), skeleton.getSplit(), subStackBuilder.stack, skeleton.getMerge(), straceArray, parent));
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	@SuppressWarnings("rawtypes")
	private Skeleton[] getStraceAsArray() {
		return strace.toArray(new Skeleton[strace.size()]);
	}
}
