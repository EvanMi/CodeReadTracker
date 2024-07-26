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
