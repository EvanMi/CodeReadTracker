package com.yumi.codereadtracker.window;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class TrackerGraphComponent extends mxGraphComponent {
    private final mxGraph mxGraph;

    public TrackerGraphComponent(mxGraph graph) {
        super(graph);
        this.mxGraph = graph;
    }


    public com.mxgraph.view.mxGraph getMxGraph() {
        return mxGraph;
    }
}
