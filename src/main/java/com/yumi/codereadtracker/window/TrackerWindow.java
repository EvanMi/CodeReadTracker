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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.yumi.codereadtracker.service.TrackerWindowManageService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TrackerWindow {
    private static final String NOT_IN_TRACING = "Not In Tracing ...";
    private static final String IN_TRACING = "In tracing ...";
    private JPanel jcontent;
    private JScrollPane image;
    private JPanel buttons;
    private JButton startTrace;
    private JButton stopTrace;
    private JButton reset;
    private JButton close;
    private JPanel imagePanel;
    private JLabel myLabel;
    private TrackerJPanel trackerJPanel;
    private final Project project;
    private volatile boolean isTrackEnabled = false;
    private String lastAddedVertex = null;
    private volatile int index = 1;

    private volatile int y = 0;

    private final ConcurrentHashMap<String, VirtualFile> trackedLine2FileMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> trackedLine2VertexMap = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();

    public TrackerWindow(Project project, ToolWindow toolWindow) {
        this.project = project;
        startTrace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isTrackEnabled = true;
                myLabel.setText(IN_TRACING);
            }
        });
        stopTrace.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isTrackEnabled = false;
                myLabel.setText(NOT_IN_TRACING);
            }
        });
        reset.addActionListener(e -> {
            lock.lock();
            try {
                trackerJPanel.getMxGraph().removeCells(trackedLine2VertexMap.values().toArray(), true);
                trackerJPanel.getMxGraph().refresh();
                trackedLine2VertexMap.clear();
                trackedLine2FileMap.clear();
                index = 1;
                lastAddedVertex = null;
                y = 0;
            } catch (Exception ex) {
                throw ex;
            }  finally {
                lock.unlock();
            }
        });
        close.addActionListener(e -> toolWindow.hide());
        myLabel.setText(NOT_IN_TRACING);
        mxGraph graph = trackerJPanel.getMxGraph();
        // 创建图形组件并添加到 JFrame
        mxGraphComponent graphComponent = trackerJPanel.getMxGraphComponent();

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 获取鼠标点击位置的单元格（可以是节点或边）
                Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                // 检查是否选中了一个节点
                if (cell != null && graph.getModel().isVertex(cell)) {
                    // 执行点击节点后的操作
                    mxCell mxCell = (mxCell) cell;
                    String codeAndLineNumber = (String)mxCell.getValue();
                    VirtualFile virtualFile = trackedLine2FileMap.get(codeAndLineNumber);
                    if (null != virtualFile) {
                        ApplicationManager.getApplication().runReadAction(() -> {
                            String[] split = codeAndLineNumber.split("#");
                            new OpenFileDescriptor(project, virtualFile, Integer.valueOf(split[split.length - 1]) - 1, 0)
                                    .navigate(true);
                        });
                    }
                }
            }
        });

        ApplicationManager.getApplication().getService(TrackerWindowManageService.class).add(project.getName(), this);
    }

    public void trackAndAddVertex(String codeAndLineNumber, VirtualFile virtualFile) {
        if (!isTrackEnabled) {
            return;
        }
        lock.lock();
        mxGraph graph = trackerJPanel.getMxGraph();
        Object parent = graph.getDefaultParent();
        try {
            trackedLine2FileMap.put(codeAndLineNumber, virtualFile);
            // 开始更新图形的模型
            graph.getModel().beginUpdate();
            int curY = y;
            Object curVertex = trackedLine2VertexMap.computeIfAbsent(codeAndLineNumber, key ->
                    graph.insertVertex(parent, codeAndLineNumber, codeAndLineNumber, 0, y, codeAndLineNumber.length() * 10, 30));
            y = curY + 70;
            if (null != lastAddedVertex) {
                Object lastVertex = trackedLine2VertexMap.get(lastAddedVertex);
                if (null == lastVertex) {
                    throw new IllegalStateException("last vertex not exist");
                }
                Object[] edgesBetween = graph.getEdgesBetween(lastVertex, curVertex, true);
                int curIndex = index;
                if (edgesBetween.length > 0) {

                    String value = (String)graph.getModel().getValue(edgesBetween[0]);
                    value = value + "," + curIndex;
                    graph.getModel().setValue(edgesBetween[0], value);
                } else {
                    graph.insertEdge(parent, null, String.valueOf("      " + curIndex), lastVertex, curVertex);
                }
                index = curIndex + 1;
            }
            mxIGraphLayout layout = new mxHierarchicalLayout(graph);
            layout.execute(graph.getDefaultParent());
            lastAddedVertex = codeAndLineNumber;
        } catch (Exception ex) {
            throw ex;
        } finally {
            // 完成更新
            graph.getModel().endUpdate();
            lock.unlock();
        }
    }

    public JPanel getJcontent() {
        return jcontent;
    }

    public Project getProject() {
        return project;
    }

    private void createUIComponents(){
        this.trackerJPanel = new TrackerJPanel();
    }
}
