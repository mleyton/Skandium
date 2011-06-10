package cl.niclabs.skandium.trace;

import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import cl.niclabs.skandium.skeletons.Skeleton;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

class SkelFrame  extends JFrame {

	private static final long serialVersionUID = -2707712944901661771L;
	private final Controller controller;
	mxGraph graph;

	SkelFrame(Controller cont) throws HeadlessException {
		super("Skandium Visualizer");
		this.controller = cont;
		graph = new mxGraph();
	}
	
	void initSkelFrame(Skeleton<?,?> skeleton) {
		Object parent = graph.getDefaultParent();
		
		mxCell mainCell;
		graph.getModel().beginUpdate();
		try {
			mainCell = (mxCell) graph.insertVertex(parent,null,"",10,10,0,0,SkeletonTreeBuilder.STYLE_NOSHAPE);
			mainCell.setConnectable(false);
			SkeletonTreeBuilder builder = new SkeletonTreeBuilder(controller, graph,mainCell);
			skeleton.accept(builder);
			graph.setCellsLocked(true);
			graph.setCellsSelectable(false);			
		}
		finally {
			graph.getModel().endUpdate();
		}

		
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter () {

			@Override
			public void windowClosing(WindowEvent e) {
				controller.close();
			}

		});
		setSize(400, 320);
		setVisible(true);

	}
	
	void updateTrace(mxCell traceVert, long invokes, long execTime) {
		if (invokes>0) {
			String value = new String();
			if (execTime > 0 ) {
				double segs = (float)execTime / 1000;
				value = String.format("%d\n%.2f", invokes, segs);				
			} else 
				value = String.format("%d", invokes);
			graph.getModel().beginUpdate();
			traceVert.setValue(value);
			graph.getModel().endUpdate();
			graph.refresh();
		}
	}
	
	void close() {
		dispose();
	}
	
}
