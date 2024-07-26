package com.yumi.codereadtracker.listener;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.yumi.codereadtracker.service.TrackerWindowManageService;
import org.jetbrains.annotations.NotNull;

public class GoToDeclarationOrUsagesActionListener implements AnActionListener {

    @Override
    public void beforeActionPerformed(@NotNull AnAction action, @NotNull AnActionEvent event) {
        if ("Go to Declaration or Usages".equals(action.getTemplatePresentation().getText())) {
            VirtualFile virtualFile = event.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
            Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
            int line = editor.getCaretModel().getLogicalPosition().line + 1;
            String res = virtualFile.getNameWithoutExtension() + "#" + line;
           ApplicationManager.getApplication().getService(TrackerWindowManageService.class).get(editor.getProject().getName())
                   .ifPresent(myWindow -> myWindow.trackAndAddVertex(res, virtualFile));
        }

    }
}
