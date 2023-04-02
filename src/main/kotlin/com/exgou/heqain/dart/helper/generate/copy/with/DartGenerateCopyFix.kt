package com.exgou.heqain.dart.helper.generate.copy.with

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartType
import com.jetbrains.lang.dart.psi.DartVarAccessDeclaration
import kotlin.streams.toList

open class DartGenerateCopyFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

    override fun processElements(project: Project, editor: Editor, elementsToProcess: Set<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        this.anchor = this.doAddMethodsForOne(
            editor,
            templateManager,
            this.buildFunctionsText(templateManager, elementsToProcess),
            this.anchor
        )
    }

    override fun getNothingFoundMessage(): String {
        return ""
    }

    protected fun buildFunctionsText(
        templateManager: TemplateManager,
        elementsToProcess: Set<DartComponent>
    ): Template {
        val list: List<DartComponent> = elementsToProcess.stream().dropWhile { false }.toList()
        val template = templateManager.createTemplate(this.javaClass.name, "copyWith")
        template.isToReformat = true
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(" ")
        template.addVariable(TextExpression("copyWith"), true)
        template.addTextSegment(if (list.isEmpty()) "(" else "({")

        list.forEach {
            if (it is DartVarAccessDeclaration) {
                val type: DartType? = it.type
                if (null == type) {
                    template.addTextSegment("var " + it.name!!)
                } else {
                    template.addTextSegment(it.type!!.text.replace("?", "") + "? " + it.name!!)
                }
                template.addTextSegment(",")
            }
        }

        template.addTextSegment(if (list.isEmpty()) "){" else "}){")
        template.addTextSegment("return ")
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment("(")

        list.forEach {
            template.addTextSegment(it.name!!)
            template.addTextSegment(":")
            template.addTextSegment(it.name!!)
            template.addTextSegment("??this.")
            template.addTextSegment(it.name!!)
            template.addTextSegment(",")
        }

        template.addTextSegment(");")
        template.addEndVariable()
        template.addTextSegment(" }")
        template.addTextSegment(" ")
        return template
    }

    override fun buildFunctionsText(templateManager: TemplateManager, e: DartComponent): Template? {
        return null
    }

    override fun evalAnchor(editor: Editor?) {
        val method = myDartClass.findMethodByName("copyWith");
        method?.delete()
        super.evalAnchor(editor)
    }
}
