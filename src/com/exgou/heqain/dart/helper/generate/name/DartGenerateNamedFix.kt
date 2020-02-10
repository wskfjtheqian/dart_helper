package com.exgou.heqain.dart.helper.generate.name
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

class DartGenerateNamedFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {

    override fun processElements(project: Project, editor: Editor, elementsToProcess: Set<DartComponent>) {
        if (project == null) {
            //            $$$reportNull$$$0(1);
        }

        if (editor == null) {
            //            $$$reportNull$$$0(2);
        }

        if (elementsToProcess == null) {
            //            $$$reportNull$$$0(3);
        }



        val templateManager = TemplateManager.getInstance(project)
        this.anchor = this.doAddMethodsForOne(editor, templateManager, this.buildFunctionsText(templateManager, elementsToProcess), this.anchor)
    }

    override fun getNothingFoundMessage(): String {
        if ("" == null) {
            //            $$$reportNull$$$0(4);
        }
        return ""
    }

    protected fun buildFunctionsText(templateManager: TemplateManager, elementsToProcess: Set<DartComponent>): Template {
        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
        template.isToReformat = true
        template.addTextSegment(this.myDartClass.name!!)
        template.addTextSegment(".")
        template.addVariable(TextExpression("name"), true)
        template.addTextSegment(if (0 == elementsToProcess.size) "(" else "({")
        val iterator = elementsToProcess.iterator()

        while (iterator.hasNext()) {
            template.addTextSegment("this.")
            template.addTextSegment(iterator.next().name!!)
            template.addTextSegment(",")
        }

        template.addTextSegment(if (0 == elementsToProcess.size) ");" else "});")
        template.addEndVariable()
        template.addTextSegment(" ")
        return template
    }

    override fun buildFunctionsText(templateManager: TemplateManager, e: DartComponent): Template? {
        return null
    }
}
