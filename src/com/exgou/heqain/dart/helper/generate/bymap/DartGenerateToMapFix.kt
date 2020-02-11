package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.utils.UiUtils
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartReferenceExpression
import com.jetbrains.lang.dart.psi.DartType

class DartGenerateToMapFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {
    var isDataVerify: Boolean = true;

    fun getMyDartClass(): DartClass {
        return myDartClass;
    }

    override fun processElements(project: Project, editor: Editor, elementsToProcess: MutableSet<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        var toMap = myDartClass.findMemberByName("toMap");
        var template = this.buildFunctionsText(templateManager, elementsToProcess);
        if (null != toMap) {
            toMap.delete()
            this.anchor = this.doAddMethodsForOne(editor, templateManager, template, toMap.firstChild)
        } else {
            this.anchor = this.doAddMethodsForOne(editor, templateManager, template, this.anchor)
        }
    }

    override fun getNothingFoundMessage(): String {
        return "null"
    }

    override fun buildFunctionsText(templateManager: TemplateManager, dartComponent: DartComponent): Template? {
        return null
    }

    private fun buildFunctionsText(templateManager: TemplateManager, dartComponent: MutableSet<DartComponent>): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
        template.isToReformat = true
        template.addTextSegment("Map<String, dynamic> toMap() {")
        template.addTextSegment("return {")

        elementsToProcess.forEach {
            var jsonName: String? = UiUtils.getJsonName(it);
            template.addTextSegment("'${jsonName ?: it?.name}':")
            template.addTextSegment(addItem(it))
            template.addTextSegment(",")
        }

        template.addTextSegment("};")
        template.addTextSegment("}")
        return template
    }

    private fun addItem(field: DartComponent): String {
        var fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java);
        return fromItem(field?.name, fieldType);
    }

    private fun fromItem(name: String?, fieldType: DartType?): String {
        if (null != fieldType) {
            var expression: DartReferenceExpression? = fieldType.referenceExpression
            if (UiUtils.isDartEnum(fieldType!!)) {
                return fromEnum(name);
            } else {
                when (expression?.text) {
                    "int" -> return "$name";
                    "double" -> return "$name";
                    "String" -> return "$name";
                    "DateTime" -> return fromDateTime(name);
                    "List" -> return fromList(name, fieldType);
                    "Map" -> return fromMap(name, fieldType);
                }

                return "$name?.toMap()";
            }

        }
        return "$name";
    }

    private fun fromEnum(name: String?): String {
        return "$name?.index"
    }

    private fun fromList(name: String?, fieldType: DartType): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList;
        if (null == typeList || 0 == typeList.size) {
            return "$name??[]";
        }
        return "$name?.map((value)=>${fromItem("value", typeList?.get(0))})?.toList()??[]";
    }

    private fun fromMap(name: String?, fieldType: DartType): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList;
        if (null == typeList || 0 == typeList.size) {
            return "$name??{}";
        }
        return "$name?.map((key, value) => MapEntry(${fromItem("key", typeList?.get(0))}, ${fromItem("value", typeList?.get(1))}))??{}";
    }

    private fun fromDateTime(name: String?): String {
        return " '-\${$name.month.toString().padLeft(2, '0')}'\n" +
                "'-\${$name.day.toString().padLeft(2, '0')}'\n" +
                "' \${$name.hour.toString().padLeft(2, '0')}'\n" +
                "':\${$name.minute.toString().padLeft(2, '0')}'\n" +
                "':\${$name.second.toString().padLeft(2, '0')}'\n" +
                "'.\${$name.millisecond.toString().padLeft(3, '0')}'";
    }

}
