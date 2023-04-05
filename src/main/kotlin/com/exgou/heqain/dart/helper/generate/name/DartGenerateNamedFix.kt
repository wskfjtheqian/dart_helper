package com.exgou.heqain.dart.helper.generate.name

import com.exgou.heqain.dart.helper.utils.DartUtils
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

class DartGenerateNamedFix(val dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

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
        val template = templateManager.createTemplate(this.javaClass.name, "DartName")
        template.isToReformat = true
        template.addTextSegment("const ")
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(".")
        template.addVariable(TextExpression("name"), true)
        template.addTextSegment(if (elementsToProcess.isEmpty()) "(" else "({")

        elementsToProcess.forEach {
            if (it is DartVarAccessDeclaration) {
                val type: DartType? = it.type
                if (it.parent.parent.parent.parent != dartClass) {
                    template.addTextSegment("super.")
                } else {
                    template.addTextSegment("this.")
                }
                template.addTextSegment(it.name!!)
                if (!DartUtils.isNullPointer(type)) {
                    template.addTextSegment(" = ")
                    when (val name = type!!.referenceExpression!!.text) {
                        "int" -> template.addTextSegment("0")
                        "double" -> template.addTextSegment("0.0")
                        "String" -> template.addTextSegment("\"\"")
                        "bool" -> template.addTextSegment("false")
                        "DateTime" -> template.addTextSegment("DateTime.now()")
                        "List" -> template.addTextSegment("const[]")
                        "Map" -> template.addTextSegment("const{}")
                        else -> template.addTextSegment("const ${name}()")
                    }
                }
                template.addTextSegment(",")
            }
        }
        template.addTextSegment(if (elementsToProcess.isEmpty()) ");" else "});")
        template.addEndVariable()
        template.addTextSegment(" ")
        return template
    }

    override fun buildFunctionsText(templateManager: TemplateManager, e: DartComponent): Template? {
        return null
    }
}
