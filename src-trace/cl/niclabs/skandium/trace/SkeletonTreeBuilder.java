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
	
	private static final String STYLE_SKEL = "defaultVertex;shape=triangle;direction=north;verticalAlign=bottom";
	private static final String STYLE_MUS = "defaultVertex;shape=ellipse;direction=north;verticalAlign=bottom;fillColor=white";
	private static final String STYLE_EDGE = "endArrow=none";
	private static final String STYLE_NOSHAPE = "shape=none;foldable=0";
	private static final double SKEL_WIDTH = 50;
	private static final double SKEL_HEIGHT = 50;
	private static final double MUS_WIDTH = 23;
	private static final double MUS_HEIGHT = 23;
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
	public <P, R> void visit(Farm<P, R> skeleton) {
		mxCell farmVert = (mxCell) graph.insertVertex(parent,null,"Farm",0,0,SKEL_WIDTH,SKEL_HEIGHT,STYLE_SKEL);
		farmVert.setConnectable(false);
		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",0,70,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", farmVert, subSkelVert, STYLE_EDGE);
		
		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSubskel().accept(subSkelBulder);		
	}

	@Override
	public <P, R> void visit(Pipe<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P, R> void visit(Seq<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P, R> void visit(If<P, R> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <P> void visit(For<P> skeleton) {
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
