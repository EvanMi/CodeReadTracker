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
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
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

import static com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE;

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
    private TrackerGraphComponent trackerGraphComponent;
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
                trackerGraphComponent.getMxGraph().removeCells(trackedLine2VertexMap.values().toArray(), true);
                trackerGraphComponent.getMxGraph().refresh();
                trackedLine2VertexMap.clear();
                trackedLine2FileMap.clear();
                index = 1;
                lastAddedVertex = null;
                y = 0;
                this.trackerGraphComponent.setVisible(false);
            } catch (Exception ex) {
                throw ex;
            }  finally {
                lock.unlock();
            }
        });
        close.addActionListener(e -> toolWindow.hide());
        myLabel.setText(NOT_IN_TRACING);
        mxGraph graph = trackerGraphComponent.getMxGraph();
        // 创建图形组件并添加到 JFrame
        mxGraphComponent graphComponent = trackerGraphComponent;
        graphComponent.setVisible(false);


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
                            int logicalLine = Integer.valueOf(split[split.length - 1]) - 1;
                            int logicalColumn = 0;
                            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                            Editor editor = FileEditorManager.getInstance(project)
                                    .openTextEditor(openFileDescriptor, true);
                            CaretModel caretModel = editor.getCaretModel();
                            LogicalPosition logicalPosition = new LogicalPosition(logicalLine, logicalColumn);
                            caretModel.moveToLogicalPosition(logicalPosition);
                            editor.getScrollingModel().scrollToCaret(MAKE_VISIBLE);
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
        mxGraph graph = trackerGraphComponent.getMxGraph();
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
            if (!this.trackerGraphComponent.isVisible()) {
                this.trackerGraphComponent.setVisible(true);
            }
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
        mxGraph mxGraph = new TrackerGraph(this);
        TrackerGraphComponent component= new TrackerGraphComponent(mxGraph);
        mxGraph.setCellsEditable(false); // 禁止编辑单元格内容
        //mxGraph.setCellsMovable(false);  // 禁止移动单元格
        mxGraph.setCellsResizable(false); // 禁止改变单元格大小
        component.setConnectable(false); // 禁止连接操作
        this.trackerGraphComponent = component;
    }

    public static class TrackerGraph extends mxGraph {
        private final TrackerWindow trackerWindow;
        public TrackerGraph(TrackerWindow trackerWindow) {
            this.trackerWindow = trackerWindow;
        }

        @Override
        public String getToolTipForCell(Object cell) {
            String value = convertValueToString(cell);
            VirtualFile virtualFile = trackerWindow.trackedLine2FileMap.get(value);
            if (null != virtualFile) {
                return virtualFile.getCanonicalPath();
            }
            return value;
        }
    }
}
