package com.exgou.heqain.dart.helper.generate

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.lang.dart.ide.DartNamedElementNode
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartComponent

abstract class DartHelperGenerateHandler : BaseDartGenerateHandler() {

    override fun collectCandidates(p0: DartClass, p1: MutableList<DartComponent>) {

    }
    override fun invoke(project: Project, editor: Editor, file: PsiFile, offset: Int) {
        if (project == null) {
//            `$$$reportNull$$$0`(5)
        }

        if (editor == null) {
//            `$$$reportNull$$$0`(6)
        }

        if (file == null) {
//            `$$$reportNull$$$0`(7)
        }

        val dartClass = PsiTreeUtil.getParentOfType(
            file.findElementAt(offset),
            DartClassDefinition::class.java
        )
        if (dartClass != null) {
            val candidates: MutableList<DartComponent> = ArrayList()
            collectCandidates(editor, dartClass, candidates)
            var selectedElements: List<DartNamedElementNode>? = emptyList()
            if (ApplicationManager.getApplication().isUnitTestMode) {
                selectedElements = ContainerUtil.map(
                    candidates
                ) { dartComponent: DartComponent? ->
                    DartNamedElementNode(
                        dartComponent
                    )
                }
            } else if (!candidates.isEmpty()) {
                val chooser = createMemberChooserDialog(project, dartClass, candidates, this.title)
                chooser.show()
                if (chooser.exitCode != 0) {
                    return
                }
                selectedElements = chooser.selectedElements
            }
            doInvoke(project, editor, file, selectedElements!!, createFix(dartClass))
        }
    }

    abstract fun collectCandidates(editor: Editor, dartClass: DartClass, candidates: MutableList<DartComponent>);

    override fun doAllowEmptySelection(): Boolean {
        return true
    }
}