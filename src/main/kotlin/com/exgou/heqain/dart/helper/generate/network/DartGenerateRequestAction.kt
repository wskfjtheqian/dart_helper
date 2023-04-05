package com.exgou.heqain.dart.helper.generate.network

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartMethodDeclaration


class DartGenerateRequestAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(CommonDataKeys.PROJECT)
        val editor = event.getData(CommonDataKeys.EDITOR)
        val psiFile = event.getData(CommonDataKeys.PSI_FILE)

        if (psiFile != null && project != null && editor != null) {
            val caretOffset = editor?.caretModel?.offset ?: -1
            val method = PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartMethodDeclaration::class.java)
            if (null != method) {
                val fix = DartGenerateRequestFix(project, editor, method);
                fix.process();
            }
        }

    }

    override fun update(e: AnActionEvent) {
        val editorAndPsiFile = getEditorAndPsiFile(e)
        val editor = editorAndPsiFile.first as Editor
        val psiFile = editorAndPsiFile.second as PsiFile
        val caretOffset = editor.caretModel.offset
        val enable = this.doEnable(PsiTreeUtil.getParentOfType(psiFile.findElementAt(caretOffset), DartComponent::class.java))
        e.presentation.isEnabledAndVisible = enable
    }

    protected fun doEnable(component: DartComponent?): Boolean {
        if (null != component && component is DartMethodDeclaration) {
            return true;
        }
        return false;
    }

    private fun getEditorAndPsiFile(e: AnActionEvent): Pair<Any?, Any?> {
        return if (e.getData(CommonDataKeys.PROJECT) == null) {
            Pair.create(null as Any?, null as Any?)
        } else {
            Pair.create(e.getData(CommonDataKeys.EDITOR), e.getData(CommonDataKeys.PSI_FILE))
        }
    }
}


