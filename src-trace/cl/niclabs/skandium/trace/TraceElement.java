package cl.niclabs.skandium.trace;

import com.mxgraph.model.mxCell;

public class TraceElement {
	private mxCell traceVert;
	private long invokes;
	private long execTime;
	private long startTime;
	
	
	
	TraceElement(mxCell traceVert, long invokes, long execTime, long startTime) {
		super();
		this.traceVert = traceVert;
		this.invokes = invokes;
		this.execTime = execTime;
		this.startTime = startTime;
	}
	
	mxCell getTraceVert() {
		return traceVert;
	}	
	void setTraceVert(mxCell traceVert) {
		this.traceVert = traceVert;
	}
	
	long getInvokes() {
		return invokes;
	}
	void setInvokes(long invokes) {
		this.invokes = invokes;
	}
	
	long getExecTime() {
		return execTime;
	}
	void setExecTime(long execTime) {
		this.execTime = execTime;
	}
	
	long getStartTime() {
		return startTime;
	}
	void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
