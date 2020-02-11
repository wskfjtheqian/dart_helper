package com.exgou.heqain.dart.helper.news

import com.intellij.ide.util.DirectoryChooserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler

class AddClassByJsonAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT)
        val view = event.getData(LangDataKeys.IDE_VIEW)
        if (null != view && project != null) {
            JsonToDartObject.main(project) { name: String, text: String ->
//                onSave(project, directory, name, text);
            }
        }
    }

}

