package com.exgou.heqain.dart.helper.generate.json

import com.exgou.heqain.dart.helper.news.JsonToDartObject
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartClass
import org.jetbrains.annotations.NotNull

class AddClassByJsonAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT)
        val editor = event.getData(CommonDataKeys.EDITOR)
        val view = event.getData(LangDataKeys.IDE_VIEW)

        if (null != view && project != null) {
            JsonToDartObject.main(project) { name: String, text: String ->
                onSave(project, editor, name, text);
            }
        }
    }

    private fun onSave(project: Project, editor: Editor?, name: String, text: String) {
        val templateManager = TemplateManager.getInstance(project)
        val template = templateManager.createTemplate(name, "Dart")

        template.isToReformat = true
        template.addTextSegment(text)
        templateManager.startTemplate(editor!!, template);
    }


    override fun update(e: AnActionEvent) {
        val editorAndPsiFile = getEditorAndPsiFile(e)
        val editor = editorAndPsiFile.first as Editor
        val psiFile = editorAndPsiFile.second as PsiFile
        val caretOffset = editor?.caretModel?.offset ?: -1
        val enable = psiFile != null && this.doEnable(PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartClass::class.java))
        e.presentation.isEnabledAndVisible = enable
    }

    protected fun doEnable(dartClass: DartClass?): Boolean {
        return dartClass == null
    }

    private fun getEditorAndPsiFile(e: AnActionEvent): Pair<Any?, Any?> {
        return if (e.getData(CommonDataKeys.PROJECT) == null) {
            Pair.create(null as Any?, null as Any?)
        } else {
            Pair.create(e.getData(CommonDataKeys.EDITOR), e.getData(CommonDataKeys.PSI_FILE))
        }
    }
}

