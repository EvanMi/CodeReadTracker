/*
 * Copyright [2024] [EvanMi]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
