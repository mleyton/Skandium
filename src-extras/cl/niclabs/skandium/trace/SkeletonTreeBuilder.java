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
 *   
 *   This class uses JGraphx library by JGraph Ltd under the following 
 *   license:
 *   
 *   Copyright (c) 2001-2009, JGraph Ltd
 *   All rights reserved.
 *   
 *   Redistribution and use in source and binary forms, with or without modification, 
 *   are permitted provided that the following conditions are met:
 *   
 *   Redistributions of source code must retain the above copyright notice, this list 
 *   of conditions and the following disclaimer.
 *   Redistributions in binary form must reproduce the above copyright notice, this 
 *   list of conditions and the following disclaimer in the documentation and/or 
 *   other materials provided with the distribution.
 *   Neither the name of JGraph Ltd nor the names of its contributors may be used 
 *   to endorse or promote products derived from this software without specific prior written permission.
 *   Termination for Patent Action. This License shall terminate
 *   automatically, as will all licenses assigned to you by the copyright
 *   holders of this software, and you may no longer exercise any of the
 *   rights granted to you by this license as of the date you commence an
 *   action, including a cross-claim or counterclaim, against the
 *   copyright holders of this software or any licensee of this software
 *   alleging that any part of the JGraph, JGraphX and/or mxGraph software
 *   libraries infringe a patent. This termination provision shall not
 *   apply for an action alleging patent infringement by combinations of
 *   this software with other software or hardware.
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 *   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 *   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 *   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 *   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *   
 */


package cl.niclabs.skandium.trace;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;

import cl.niclabs.skandium.events.Where;
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

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

class SkeletonTreeBuilder implements SkeletonVisitor {
	
	private static final double SKEL_SIDE = 50;
	private static final double MUS_SIDE = SKEL_SIDE/2;
	private static final String STYLE_SKEL = "defaultVertex;shape=triangle;direction=north;verticalAlign=bottom";
	private static final String STYLE_MUS = "defaultVertex;shape=ellipse;direction=north;verticalAlign=bottom;fillColor=white";
	private static final String STYLE_EDGE = "MYEDGE";
	static final String STYLE_NOSHAPE = "shape=none;foldable=0;verticalAlign=top;align=right;fontSize=9;fontColor=black";
	private static final String CONDITION = "C";
	private static final String SPLIT = "S";
	private static final String MERGE = "M";
	private static final String EXECUTE = "E";

	private mxGraph graph;
	private mxCell parent;
	private mxCell skelVert;
	
	public Stack<Skeleton<?, ?>> strace;
	private Controller controller;
	
	SkeletonTreeBuilder(Controller controller, mxGraph graph, mxCell parent) {
		super();
		this.graph = graph;
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, "none");
		style.put(mxConstants.STYLE_DASHED, true);
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_TOPTOBOTTOM);
		stylesheet.putCellStyle("MYEDGE", style);

		this.parent = parent;
		
		this.strace = new Stack<Skeleton<?, ?>>();
		this.controller = controller;
	}
	
	private SkeletonTreeBuilder(Controller c, mxGraph graph, mxCell parent, Stack<Skeleton<?, ?>> trace) {
		this(c, graph, parent);
		this.strace.addAll(trace);
	}

	mxCell getSkelVert() {
		return skelVert;
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		strace.add(skeleton);

		skelVert = (mxCell) graph.insertVertex(parent,null,"Seq",0,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		mxCell exeVert = (mxCell) graph.insertVertex(parent,null,EXECUTE,MUS_SIDE/2,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		exeVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, exeVert, STYLE_EDGE);

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

//		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
//		skelTraceVert.setConnectable(false);
//		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell exeTraceVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE/2,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		exeTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, exeTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		strace.add(skeleton);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller, graph,subSkelGroup,strace);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double x = subSkelVert.getGeometry().getX();
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"Farm",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		strace.add(skeleton);

		mxCell stage1Group = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		stage1Group.setConnectable(false);

		SkeletonTreeBuilder stage1Builder = new SkeletonTreeBuilder(controller, graph,stage1Group,strace);
		skeleton.getStage1().accept(stage1Builder);
		mxCell stage1Vert = stage1Builder.getSkelVert(); 
		
		double x1 = stage1Vert.getGeometry().getX();
		double w1 = stage1Group.getGeometry().getWidth();
		
		mxCell stage2Group = (mxCell) graph.insertVertex(parent,null,"",w1,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		stage2Group.setConnectable(false);

		SkeletonTreeBuilder stage2Builder = new SkeletonTreeBuilder(controller, graph,stage2Group,strace);
		skeleton.getStage2().accept(stage2Builder);		
		mxCell stage2Vert = stage2Builder.getSkelVert();
		
		double x2 = stage2Vert.getGeometry().getX();
		
		double x = (x1 + w1 + x2)/2;
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"Pipe",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, stage1Vert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, stage2Vert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		strace.add(skeleton);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller, graph,subSkelGroup,strace);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double x = subSkelVert.getGeometry().getX();
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"For",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		controller.initTrace(straceArray,traceMap);
	}
	
	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		strace.add(skeleton);

		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell trueCaseGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		trueCaseGroup.setConnectable(false);

		SkeletonTreeBuilder trueCaseBuilder = new SkeletonTreeBuilder(controller,graph,trueCaseGroup,strace);
		skeleton.getTrueCase().accept(trueCaseBuilder);
		mxCell trueCaseVert = trueCaseBuilder.getSkelVert();
		
		double wT = trueCaseGroup.getGeometry().getWidth();
		
		mxCell falseCaseGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+wT,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		falseCaseGroup.setConnectable(false);

		SkeletonTreeBuilder falseCaseBuilder = new SkeletonTreeBuilder(controller,graph,falseCaseGroup,strace);
		skeleton.getFalseCase().accept(falseCaseBuilder);
		mxCell falseCaseVert = falseCaseBuilder.getSkelVert();
		
		double xF = falseCaseVert.getGeometry().getX();
		double x = ((MUS_SIDE*1.5 + wT + xF + (SKEL_SIDE/2))/2) - (SKEL_SIDE/2);
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"If",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, trueCaseVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, falseCaseVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell condTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		condTraceVert.setConnectable(false);
		traceMap.put(Where.CONDITION, condTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		strace.add(skeleton);

		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller,graph,subSkelGroup,strace);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();

		double xS = subSkelVert.getGeometry().getX();
		double x = ((MUS_SIDE*1.5 + xS + (SKEL_SIDE/2))/2) - (SKEL_SIDE/2);
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"While",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell condTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		condTraceVert.setConnectable(false);
		traceMap.put(Where.CONDITION, condTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		strace.add(skeleton);

		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller,graph,subSkelGroup,strace);
		skeleton.getSkeleton().accept(subSkelBuilder);		
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double w = subSkelGroup.getGeometry().getWidth();
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,MUS_SIDE+w,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		merVert.setConnectable(false);

		double x = w/2;
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"Map",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, splVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, merVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell splTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		splTraceVert.setConnectable(false);
		traceMap.put(Where.SPLIT, splTraceVert);

		mxCell merTraceVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+w,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		merTraceVert.setConnectable(false);
		traceMap.put(Where.MERGE, merTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		strace.add(skeleton);

		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		double w = 0;
		Skeleton<?,?>[] skels = skeleton.getSkeletons();

		skelVert = (mxCell) graph.insertVertex(parent,null,"Fork",0,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);

		
		for (int i=0; i<skels.length; i++) {
			mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+w,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
			subSkelGroup.setConnectable(false);
			SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller,graph,subSkelGroup,strace);
			skels[i].accept(subSkelBuilder);
			mxCell subSkelVert = subSkelBuilder.getSkelVert();
			graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);					
			w += subSkelGroup.getGeometry().getWidth();
		}
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,MUS_SIDE+w,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		merVert.setConnectable(false);

		double x = w/2;
		skelVert.getGeometry().setX(x);
		
		graph.insertEdge(parent, null, "", skelVert, splVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, merVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell splTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		splTraceVert.setConnectable(false);
		traceMap.put(Where.SPLIT, splTraceVert);

		mxCell merTraceVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+w,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		merTraceVert.setConnectable(false);
		traceMap.put(Where.MERGE, merTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		strace.add(skeleton);

		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,MUS_SIDE,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE*2,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(controller,graph,subSkelGroup,strace);
		skeleton.getSkeleton().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double w = subSkelGroup.getGeometry().getWidth();
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,(MUS_SIDE*2)+w,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		merVert.setConnectable(false);

		double x = (MUS_SIDE + w)/2;
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"D&C",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, splVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, merVert, STYLE_EDGE);		

		Skeleton<?,?>[] straceArray = getStraceAsArray();
		HashMap<Where,mxCell> traceMap = new HashMap<Where,mxCell>();

		mxCell skelTraceVert = (mxCell) graph.insertVertex(parent,null,"",x,0,1.25*SKEL_SIDE,SKEL_SIDE,STYLE_NOSHAPE);
		skelTraceVert.setConnectable(false);
		traceMap.put(Where.SKELETON, skelTraceVert);
		
		mxCell conTraceVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		conTraceVert.setConnectable(false);
		traceMap.put(Where.CONDITION, conTraceVert);

		mxCell splTraceVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		splTraceVert.setConnectable(false);
		traceMap.put(Where.SPLIT, splTraceVert);

		mxCell merTraceVert = (mxCell) graph.insertVertex(parent,null,"",(MUS_SIDE*2)+w,SKEL_SIDE*1.5+MUS_SIDE,MUS_SIDE-2,SKEL_SIDE,STYLE_NOSHAPE);
		merTraceVert.setConnectable(false);
		traceMap.put(Where.MERGE, merTraceVert);

		controller.initTrace(straceArray,traceMap);
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}

	private Skeleton<?, ?>[] getStraceAsArray() {
		return strace.toArray(new Skeleton[strace.size()]);
	}
}
