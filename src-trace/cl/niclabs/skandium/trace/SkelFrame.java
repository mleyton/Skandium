package cl.niclabs.skandium.trace;

import java.awt.HeadlessException;

import javax.swing.JFrame;

import cl.niclabs.skandium.skeletons.Skeleton;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

class SkelFrame  extends JFrame {

	private static final long serialVersionUID = -2707712944901661771L;
	private static final String STYLE_NOSHAPE = "shape=none;foldable=0";

	SkelFrame(Skeleton<?,?> skeleton) throws HeadlessException {
		super("Skandium Visualizer");
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		graph.getModel().beginUpdate();
		try {
			mxCell mainCell = (mxCell) graph.insertVertex(parent,null,"",0,0,0,0,STYLE_NOSHAPE);
			mainCell.setConnectable(false);
			SkeletonTreeBuilder builder = new SkeletonTreeBuilder(graph,mainCell);
			skeleton.accept(builder);
			graph.setCellsLocked(true);
			graph.setCellsSelectable(false);
			
		}
		finally {
			graph.getModel().endUpdate();
		}
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
	}
}
