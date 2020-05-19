package com.exgou.heqain.dart.helper.generate.copy.with

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

open class DartGenerateCopyFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

    override fun processElements(project: Project, editor: Editor, elementsToProcess: Set<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        this.anchor = this.doAddMethodsForOne(editor, templateManager, this.buildFunctionsText(templateManager, elementsToProcess), this.anchor)
    }

    override fun getNothingFoundMessage(): String {
        return ""
    }

    protected fun buildFunctionsText(templateManager: TemplateManager, elementsToProcess: Set<DartComponent>): Template {
        val template = templateManager.createTemplate(this.javaClass.name, "copyWith")
        template.isToReformat = true
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(" ")
        template.addVariable(TextExpression("copyWith"), true)
        template.addTextSegment(if (elementsToProcess.isEmpty()) "(" else "({")

        elementsToProcess.forEach {
            template.addTextSegment(it.text)
            template.addTextSegment(",")
        }

        template.addTextSegment(if (elementsToProcess.isEmpty()) ");" else "}){")
        template.addTextSegment("return ")
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment("(")

        elementsToProcess.forEach {
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
}
