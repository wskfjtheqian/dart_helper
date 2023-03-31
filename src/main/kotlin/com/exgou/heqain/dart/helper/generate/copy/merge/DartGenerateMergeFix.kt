package com.exgou.heqain.dart.helper.generate.copy.merge

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

open class DartGenerateMergeFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

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
        val template = templateManager.createTemplate(this.javaClass.name, "merge")
        template.isToReformat = true
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(" ")
        template.addVariable(TextExpression("merge"), true)
        template.addTextSegment("(")
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment("? other){")
        template.addTextSegment("if (other == null) {")
        template.addTextSegment("return this;")
        template.addTextSegment("}")

        template.addTextSegment("return copyWith(")

        elementsToProcess.forEach {
            template.addTextSegment(it.name!!)
            template.addTextSegment(":other.")
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
        val method = myDartClass.findMethodByName("merge");
        method?.delete()
        super.evalAnchor(editor)
    }
}
