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
import cl.niclabs.skandium.skeletons.Skeleton;
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
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell trueCaseVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		trueCaseVert.setConnectable(false);

		SkeletonTreeBuilder trueCaseBulder = new SkeletonTreeBuilder(graph,trueCaseVert);
		skeleton.getTrueCase().accept(trueCaseBulder);		
		
		double wT = trueCaseVert.getGeometry().getWidth();
		
		mxCell falseCaseVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+wT,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		falseCaseVert.setConnectable(false);

		SkeletonTreeBuilder falseCaseBulder = new SkeletonTreeBuilder(graph,falseCaseVert);
		skeleton.getFalseCase().accept(falseCaseBulder);		
		
		double wF = falseCaseVert.getGeometry().getWidth();
		double x = ((MUS_SIDE + wT + wF)/2) - (SKEL_SIDE/2);
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"If",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, trueCaseVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, falseCaseVert, STYLE_EDGE);		
	}

	@Override
	public <P> void visit(While<P> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,CONDITION,0,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSubskel().accept(subSkelBulder);		

		double w = subSkelVert.getGeometry().getWidth();
		double x = ((MUS_SIDE + w)/2) - (SKEL_SIDE/2);
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"While",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, conVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(Map<P, R> skeleton) {
		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSkeleton().accept(subSkelBulder);		
		
		double w = subSkelVert.getGeometry().getWidth();
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,MUS_SIDE+w,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		merVert.setConnectable(false);

		double x = ((2*MUS_SIDE + w)/2) - (SKEL_SIDE/2);
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"Map",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);
		graph.insertEdge(parent, null, "", skelVert, splVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, merVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(Fork<P, R> skeleton) {
		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		splVert.setConnectable(false);

		double w = 0;
		Skeleton<?,?>[] skels = skeleton.getSkeletons();

		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"Fork",0,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
		skelVert.setConnectable(false);

		
		for (int i=0; i<skels.length; i++) {
			mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE+w,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
			subSkelVert.setConnectable(false);
			graph.insertEdge(parent, null, "", skelVert, subSkelVert, STYLE_EDGE);		

			SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
			skels[i].accept(subSkelBulder);
			
			w += subSkelVert.getGeometry().getWidth();
		}
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,MUS_SIDE+w,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		merVert.setConnectable(false);

		double x = ((2*MUS_SIDE + w)/2) - (SKEL_SIDE/2);
		skelVert.getGeometry().setX(x);
		
		graph.insertEdge(parent, null, "", skelVert, splVert, STYLE_EDGE);		
		graph.insertEdge(parent, null, "", skelVert, merVert, STYLE_EDGE);		
	}

	@Override
	public <P, R> void visit(DaC<P, R> skeleton) {
		mxCell conVert = (mxCell) graph.insertVertex(parent,null,SPLIT,0,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		conVert.setConnectable(false);

		mxCell splVert = (mxCell) graph.insertVertex(parent,null,SPLIT,MUS_SIDE,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		splVert.setConnectable(false);

		mxCell subSkelVert = (mxCell) graph.insertVertex(parent,null,"",MUS_SIDE*2,SKEL_SIDE*1.4,0,0,STYLE_NOSHAPE);
		subSkelVert.setConnectable(false);

		SkeletonTreeBuilder subSkelBulder = new SkeletonTreeBuilder(graph,subSkelVert);
		skeleton.getSkeleton().accept(subSkelBulder);		
		
		double w = subSkelVert.getGeometry().getWidth();
		
		mxCell merVert = (mxCell) graph.insertVertex(parent,null,MERGE,(MUS_SIDE*2)+w,SKEL_SIDE*1.4,MUS_SIDE*0.92,MUS_SIDE*0.92,STYLE_MUS);
		merVert.setConnectable(false);

		double x = ((3*MUS_SIDE + w)/2) - (SKEL_SIDE/2);
		
		mxCell skelVert = (mxCell) graph.insertVertex(parent,null,"D&C",x,0,SKEL_SIDE*0.92,SKEL_SIDE*0.92,STYLE_SKEL);
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
