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
import java.util.Arrays;
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
import cl.niclabs.skandium.system.events.SkeletonTraceElement;

/**
 * Using the visitor pattern, this class navigates a skeleton structure (ie
 * nested skeletons), and transforms the Skeletons into their Instructions
 * representations.
 * 
 * @author mleyton
 */
public class StackBuilder implements SkeletonVisitor {

	public Stack<Instruction> stack;
	public Stack<SkeletonTraceElement> strace;

	public StackBuilder() {
		super();
		this.stack = new Stack<Instruction>();
		this.strace = new Stack<SkeletonTraceElement>();
	}

	public StackBuilder(Stack<SkeletonTraceElement> trace) {
		this();
		this.strace.addAll(trace);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {

		strace.add(new SkeletonTraceElement(skeleton,0));
		
		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		skeleton.subskel.accept(this);
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));

	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {

		// mark the trace of the pipe skeleton
		strace.add(new SkeletonTraceElement(skeleton,0));

		// create a new stack builder for each stage
		StackBuilder stage1 = new StackBuilder(strace);
		StackBuilder stage2 = new StackBuilder(strace);

		// construct the stack
		skeleton.stage1.accept(stage1);
		skeleton.stage2.accept(stage2);

		// add the results to this stack (as there is no pipe instruction)
		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new EventInst(When.AFTER, Where.NESTED_SKELETON, straceArray, 1));
		stack.addAll(stage2.stack); // second stage first
		stack.push(new EventInst(When.BEFORE, Where.NESTED_SKELETON, straceArray, 1));
		stack.push(new EventInst(When.AFTER, Where.NESTED_SKELETON, straceArray, 0));
		stack.addAll(stage1.stack); // first stage last
		stack.push(new EventInst(When.BEFORE, Where.NESTED_SKELETON, straceArray, 0));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(new SkeletonTraceElement(skeleton,0));

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new SeqInst(skeleton.execute, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {

		strace.add(new SkeletonTraceElement(skeleton,0));

		StackBuilder trueCaseStackBuilder = new StackBuilder(strace);
		StackBuilder falseCaseStackBuilder = new StackBuilder(strace);

		skeleton.trueCase.accept(trueCaseStackBuilder);
		skeleton.falseCase.accept(falseCaseStackBuilder);

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new IfInst(skeleton.condition, trueCaseStackBuilder.stack,
				falseCaseStackBuilder.stack, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.CONDITION, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P> void visit(While<P> skeleton) {

		strace.add(new SkeletonTraceElement(skeleton,0));
		StackBuilder subStackBuilder = new StackBuilder(strace);
		

		skeleton.subskel.accept(subStackBuilder);

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new WhileInst(skeleton.condition, subStackBuilder.stack, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.CONDITION, straceArray, 0));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P> void visit(For<P> skeleton) {

		strace.add(new SkeletonTraceElement(skeleton,0));
		StackBuilder subStackBuilder = new StackBuilder(strace);

		skeleton.subskel.accept(subStackBuilder);

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new ForInst(subStackBuilder.stack, skeleton.times, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {

		strace.add(new SkeletonTraceElement(skeleton,0));
		StackBuilder subStackBuilder = new StackBuilder(strace);

		skeleton.skeleton.accept(subStackBuilder);

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new MapInst(skeleton.split, subStackBuilder.stack, skeleton.merge, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SPLIT, straceArray));
		stack.push(new EventInst(When.BEFORE,Where.SKELETON, straceArray));
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {

		List<Stack<Instruction>> stacks = new ArrayList<Stack<Instruction>>();

		strace.add(new SkeletonTraceElement(skeleton,0));
		for (Skeleton<?, ?> s : skeleton.skeletons) {
			StackBuilder subStackBuilder = new StackBuilder(strace);
			s.accept(subStackBuilder);
			stacks.add(subStackBuilder.stack);
		}
		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray));
		stack.push(new ForkInst(skeleton.split, stacks, skeleton.merge, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SPLIT, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray));
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {

		Stack<Integer> rbranch = new Stack<Integer>();
		int id = Arrays.deepHashCode(rbranch.toArray(new Integer[rbranch.size()]));
		
		strace.add(new SkeletonTraceElement(skeleton,id));
		StackBuilder subStackBuilder = new StackBuilder(strace);

		skeleton.skeleton.accept(subStackBuilder);

		SkeletonTraceElement[] straceArray = getStraceAsArray();
		stack.push(new EventInst(When.AFTER, Where.SKELETON, straceArray, rbranch));
		stack.push(new DaCInst(skeleton.condition, skeleton.split, subStackBuilder.stack, skeleton.merge, rbranch, straceArray));
		stack.push(new EventInst(When.BEFORE, Where.CONDITION, straceArray, rbranch));
		stack.push(new EventInst(When.BEFORE, Where.SKELETON, straceArray, rbranch));
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	private SkeletonTraceElement[] getStraceAsArray() {
		return strace.toArray(new SkeletonTraceElement[strace.size()]);
	}
}
