package com.yumi.codereadtracker.listener;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.yumi.codereadtracker.service.TrackerWindowManageService;
import org.jetbrains.annotations.NotNull;

public class ProjectCloseListener implements ProjectManagerListener {

    @Override
    public void projectClosed(@NotNull Project project) {
        ApplicationManager.getApplication().getService(TrackerWindowManageService.class).del(project.getName());
    }
}
