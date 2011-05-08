package cl.niclabs.skandium.trace;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

import cl.niclabs.skandium.skeletons.AbstractSkeleton;
import cl.niclabs.skandium.skeletons.DaC;
import cl.niclabs.skandium.skeletons.Farm;
import cl.niclabs.skandium.skeletons.For;
import cl.niclabs.skandium.skeletons.Fork;
import cl.niclabs.skandium.skeletons.If;
import cl.niclabs.skandium.skeletons.Map;
import cl.niclabs.skandium.skeletons.Pipe;
import cl.niclabs.skandium.skeletons.Seq;
import cl.niclabs.skandium.skeletons.SkeletonVisitor;
import cl.niclabs.skandium.skeletons.While;

class SkeletonTreeBuilder implements SkeletonVisitor {
	
	private static final double SKEL_SIDE = 50;
	private static final double MUS_SIDE = SKEL_SIDE/2;
	private static final String STYLE_SKEL = "defaultVertex;shape=triangle;direction=north;verticalAlign=bottom";
	private static final String STYLE_MUS = "defaultVertex;shape=ellipse;direction=north;verticalAlign=bottom;fillColor=white";
	private static final String STYLE_EDGE = "endArrow=none";
	private static final String STYLE_NOSHAPE = "shape=none;foldable=0";
	private static final String CONDITION = "C";
	private static final String SPLIT = "S";
	private static final String MERGE = "M";
	private static final String EXECUTE = "E";

	private mxGraph graph;
	private mxCell parent;
	
	SkeletonTreeBuilder(mxGraph graph, mxCell parent) {
		super();
		this.graph = graph;
		this.parent = parent;
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"Seq",0,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		mxCell exeVert = (mxCell) graph.insertVertex(parent,null,EXECUTE,MUS_SIDE/2,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		exeVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, exeVert, STYLE_EDGE);
	}

	@Override
	public <P, R> void visit(Farm<P, R> skeleton) {
		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSubskel().accept(subSkelBulder);		
		
		double w = subSkelVert.getGeometry().getWidth();
		double x = w>SKEL_SIDE? (w/2) - (SKEL_SIDE/2): 0;
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"Farm",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		mxCell stage1Vert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		stage1Vert.setConnectable(false);

		SkeletonTreeBuilder stage1Bulder = new SkeletonTreeBuilder(graph,stage1Vert);
		skeleton.getStage1().accept(stage1Bulder);		
		
		double w1 = stage1Vert.getGeometry().getWidth();
		
		mxCell stage2Vert = (mxCell) graph.insertVertex(parent,null,"",w1,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		stage2Vert.setConnectable(false);

		SkeletonTreeBuilder stage2Bulder = new SkeletonTreeBuilder(graph,stage2Vert);
		skeleton.getStage2().accept(stage2Bulder);		
		
		double w2 = stage2Vert.getGeometry().getWidth();
		
		double x = ((w1 + w2)/2) - (SKEL_SIDE/2);
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"Pipe",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, stage1Vert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, stage2Vert, STYLE_EDGE);		
	}

	@Override
	public <P> void visit(For<P> skeleton) {
		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",0,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSubskel().accept(subSkelBulder);		
		
		double w = subSkelVert.getGeometry().getWidth();
		double x = w>SKEL_SIDE? (w/2) - (SKEL_SIDE/2): 0;
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"For",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}

	
	
	
	
	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.4,MUS_SIDE,MUS_SIDE,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",25,70,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSubskel().accept(subSkelBulder);		
		
		double w = subSkelVert.getGeometry().getWidth();
		double x = w>50? (w/2) - 25: 0;
		
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		mxCell dacVert = (mxCell) graph.insertVertex(parent,null,"D&C",25,0,SKEL_WIDTH,SKEL_HEIGHT,STYLE_SKEL);
		dacVert.setConnectable(false);
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,70,MUS_WIDTH,MUS_HEIGHT,STYLE_MUS);
		conVert.setConnectable(false);
		graph.insertEdge(parent, null, "", dacVert, conVert, STYLE_EDGE);
		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,25,70,MUS_WIDTH,MUS_HEIGHT,STYLE_MUS);
		splVert.setConnectable(false);
		graph.insertEdge(parent, null, "", dacVert, splVert, STYLE_EDGE);
		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",12.5,70,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", dacVert, subSkelVert, STYLE_EDGE);
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,75,70,MUS_WIDTH,MUS_HEIGHT,STYLE_MUS);
		merVert.setConnectable(false);
		graph.insertEdge(parent, null, "", dacVert, merVert, STYLE_EDGE);
		
		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSkeleton().accept(subSkelBulder);		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P, R> void visit(AbstractSkeleton<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}
}
