package com.exgou.heqain.dart.helper.printing

import com.intellij.CommonBundle
import com.intellij.codeEditor.printing.CodeEditorBundle
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

import javax.swing.*
import java.awt.*
import java.io.FileNotFoundException

class ExportToDocAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dataContext = e.dataContext
        if (CommonDataKeys.PROJECT.getData(dataContext) != null) {
            try {
                ExportToDocManager.executeExport(dataContext)
            } catch (var5: FileNotFoundException) {
                JOptionPane.showMessageDialog(null as Component?, CodeEditorBundle.message("file.not.found", var5.message!!), CommonBundle.getErrorTitle(), 0)
            }

        }
    }

    override fun update(event: AnActionEvent) {
        val presentation = event!!.presentation
        val dataContext = event.dataContext
        if (CommonDataKeys.PSI_ELEMENT.getData(dataContext) is PsiDirectory) {
            presentation.isEnabled = true
        } else {
            val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext)
            presentation.isEnabled = psiFile != null && psiFile.containingDirectory != null
            presentation.isVisible = psiFile != null
        }
    }
}
