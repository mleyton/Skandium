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
	private long time;

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
		time = System.currentTimeMillis();
	}
	
	void updateTrace(mxCell traceVert, long invokes, long execTime) {
		if (invokes>0) {
			String value = new String();
			if (execTime > 0 ) {
				double segs = (float)execTime / 1000;
				value = String.format("%.0g\n%.2f", (float) invokes, segs);				
			} else 
				value = String.format("%.0g", (float) invokes);
			graph.getModel().beginUpdate();
			traceVert.setValue(value);
			graph.getModel().endUpdate();
			
			long currTime = System.currentTimeMillis(); 
			if (currTime - time > 1000) {
				graph.refresh();
				time = currTime;
			}
		}
	}
	
	void refresh() {
		graph.refresh();
	}
	
	void close() {
		dispose();
	}
	
}
