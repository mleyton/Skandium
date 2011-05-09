package cl.niclabs.skandium.trace;

import java.util.Hashtable;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

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

class SkeletonTreeBuilder implements SkeletonVisitor {
	
	private static final double SKEL_SIDE = 50;
	private static final double MUS_SIDE = SKEL_SIDE/2;
	private static final String STYLE_SKEL = "defaultVertex;shape=triangle;direction=north;verticalAlign=bottom";
	private static final String STYLE_MUS = "defaultVertex;shape=ellipse;direction=north;verticalAlign=bottom;fillColor=white";
	private static final String STYLE_EDGE = "MYEDGE";//"endArrow=none";
	private static final String STYLE_NOSHAPE = "shape=none;foldable=0";
	private static final String CONDITION = "C";
	private static final String SPLIT = "S";
	private static final String MERGE = "M";
	private static final String EXECUTE = "E";

	private mxGraph graph;
	private mxCell parent;
	private mxCell skelVert;
	
	SkeletonTreeBuilder(mxGraph graph, mxCell parent) {
		super();
		this.graph = graph;
		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> style = new Hashtable<String, Object>();
		style.put(mxConstants.STYLE_ENDARROW, "none");
		style.put(mxConstants.STYLE_DASHED, true);
		style.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_TOPTOBOTTOM);
		stylesheet.putCellStyle("MYEDGE", style);

		this.parent = parent;
	}
	
	mxCell getSkelVert() {
		return skelVert;
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		skelVert = (mxCell) graph.insertVertex(parent,null,"Seq",0,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		mxCell exeVert = (mxCell) graph.insertVertex(parent,null,EXECUTE,MUS_SIDE/2,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		exeVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, exeVert, STYLE_EDGE);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double x = subSkelVert.getGeometry().getX();
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"Farm",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		mxCell stage1Group = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		stage1Group.setConnectable(false);

		SkeletonTreeBuilder stage1Builder = new SkeletonTreeBuilder(graph,stage1Group);
		skeleton.getStage1().accept(stage1Builder);
		mxCell stage1Vert = stage1Builder.getSkelVert(); 
		
		double x1 = stage1Vert.getGeometry().getX();
		double w1 = stage1Group.getGeometry().getWidth();
		
		mxCell stage2Group = (mxCell) graph.insertVertex(parent,null,"",w1,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		stage2Group.setConnectable(false);

		SkeletonTreeBuilder stage2Builder = new SkeletonTreeBuilder(graph,stage2Group);
		skeleton.getStage2().accept(stage2Builder);		
		mxCell stage2Vert = stage2Builder.getSkelVert();
		
		double x2 = stage2Vert.getGeometry().getX();
		
		double x = (x1 + w1 + x2)/2;
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"Pipe",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, stage1Vert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, stage2Vert, STYLE_EDGE);		
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();
		
		double x = subSkelVert.getGeometry().getX();
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"For",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}
	
	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell trueCaseGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		trueCaseGroup.setConnectable(false);

		SkeletonTreeBuilder trueCaseBuilder = new SkeletonTreeBuilder(graph,trueCaseGroup);
		skeleton.getTrueCase().accept(trueCaseBuilder);
		mxCell trueCaseVert = trueCaseBuilder.getSkelVert();
		
		double wT = trueCaseGroup.getGeometry().getWidth();
		
		mxCell falseCaseGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+wT,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		falseCaseGroup.setConnectable(false);

		SkeletonTreeBuilder falseCaseBuilder = new SkeletonTreeBuilder(graph,falseCaseGroup);
		skeleton.getFalseCase().accept(falseCaseBuilder);
		mxCell falseCaseVert = falseCaseBuilder.getSkelVert();
		
		double xF = falseCaseVert.getGeometry().getX();
		double x = ((MUS_SIDE*1.5 + wT + xF + (SKEL_SIDE/2))/2) - (SKEL_SIDE/2);
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"If",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, trueCaseVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, falseCaseVert, STYLE_EDGE);		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
		skeleton.getSubskel().accept(subSkelBuilder);
		mxCell subSkelVert = subSkelBuilder.getSkelVert();

		double xS = subSkelVert.getGeometry().getX();
		double x = ((MUS_SIDE*1.5 + xS + (SKEL_SIDE/2))/2) - (SKEL_SIDE/2);
		
		skelVert = (mxCell) graph.insertVertex(parent,null,"While",x,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
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
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		double w = 0;
		Skeleton<?,?>[] skels = skeleton.getSkeletons();

		skelVert = (mxCell) graph.insertVertex(parent,null,"Fork",0,0,SKEL_SIDE,SKEL_SIDE,STYLE_SKEL);
		skelVert.setConnectable(false);

		
		for (int i=0; i<skels.length; i++) {
			mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+w,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
			subSkelGroup.setConnectable(false);
			SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
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
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,MUS_SIDE,SKEL_SIDE*1.5,MUS_SIDE-2,MUS_SIDE-2,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelGroup = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE*2,SKEL_SIDE*1.5,0,0,STYLE_NOSHAPE);
		subSkelGroup.setConnectable(false);

		SkeletonTreeBuilder subSkelBuilder = new SkeletonTreeBuilder(graph,subSkelGroup);
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

	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		throw new RuntimeException("Should not be here!");
	}
}
