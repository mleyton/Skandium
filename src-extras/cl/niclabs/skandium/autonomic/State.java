package cl.niclabs.skandium.autonomic;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import cl.niclabs.skandium.skeletons.Skeleton;

class State {
	boolean isFinished(Stack<Skeleton<?,?>> strace, int index) {
		// visitor
		return false;
	}
	
	int getFarmSubIndex(Stack<Skeleton<?,?>> strace, int index) {
		// solo se usa en farm
		return 0;
	}
	
	boolean isPipeStage1Finished(Stack<Skeleton<?,?>> strace, int index) {
		return false;
	}
	
	int getPipeStage1Index(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}
	
	int getPipeStage2Index(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}
	
	boolean isIfConditionFinished(Stack<Skeleton<?,?>> strace, int index) {
		return false;
	}
	
	int getIfTrueIndex(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}
	
	int getIfFalseIndex(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}

	boolean ifCondition(Stack<Skeleton<?,?>> strace, int index) {
		return false;
	}
	
	int getWhileSubIndex(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}
	
	int getForSubIndex(Stack<Skeleton<?,?>> strace, int index) {
		return 0;
	}
	
	boolean isBeforeMerge(Stack<Skeleton<?,?>> strace, int index) {
		return false;
	}
	boolean isBeforeSplit(Stack<Skeleton<?,?>> strace, int index) {
		return false;
	}
	
	List<Integer> getSubIndexes(Stack<Skeleton<?,?>> strace, int index) {
		return new ArrayList<Integer>();
	}
	
	List<Skeleton<?,?>> getDaCChildren(Stack<Skeleton<?,?>> strace, int index) {
		return new ArrayList<Skeleton<?,?>>();
	}
}
