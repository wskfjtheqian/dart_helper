package com.exgou.heqain.dart.helper.news

import com.exgou.heqain.dart.helper.generate.bymap.DartGenerateFromMapFix
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartClassDefinition
import com.jetbrains.lang.dart.psi.DartFile

class AddFromMapAndToMapFix(var mProject: Project) {

    fun add(dartFile: DartFile, editor: Editor) {
        var array = dartFile.findChildrenByClass(DartClassDefinition::class.java);

        array?.forEach {
            val templateManager = TemplateManager.getInstance(mProject)
            DartGenerateFromMapFix(it as DartClass).buildFunctionsText(templateManager, it.fields)

            println(it.name)

        }

    }
}