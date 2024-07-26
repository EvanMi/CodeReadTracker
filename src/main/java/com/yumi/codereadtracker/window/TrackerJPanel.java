package com.yumi.codereadtracker.window;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;

public class TrackerJPanel extends JPanel {
    private mxGraphComponent mxGraphComponent;
    private mxGraph mxGraph;

    public TrackerJPanel() {
        mxGraph = new mxGraph();
        mxGraphComponent = new mxGraphComponent(mxGraph);
        mxGraph.setCellsEditable(false); // 禁止编辑单元格内容
        //mxGraph.setCellsMovable(false);  // 禁止移动单元格
        mxGraph.setCellsResizable(false); // 禁止改变单元格大小
        mxGraphComponent.setConnectable(false); // 禁止连接操作
        add(mxGraphComponent);
    }

    public com.mxgraph.swing.mxGraphComponent getMxGraphComponent() {
        return mxGraphComponent;
    }

    public com.mxgraph.view.mxGraph getMxGraph() {
        return mxGraph;
    }
}
