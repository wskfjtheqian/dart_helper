package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.utils.UiUtils
import com.exgou.heqain.dart.helper.utils.UiUtils.getJsonName
import com.exgou.heqain.dart.helper.utils.UiUtils.isDartEnum
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.*

class DartGenerateFromMapFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {
    var isDataVerify: Boolean = true

    override fun processElements(project: Project, editor: Editor, elementsToProcess: MutableSet<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        var toMap = myDartClass.findNamedConstructor("fromMap");
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

    fun buildFunctionsText(templateManager: TemplateManager, dartComponent: MutableSet<DartComponent>): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
        template.isToReformat = true
        template.addTextSegment("${myDartClass.name}.fromMap(Map<String, dynamic> map) {")
        template.addTextSegment("var temp;")

        elementsToProcess.forEach {
            template.addTextSegment("this.${it.name!!}=")
            template.addTextSegment(addItem(it, true) ?: "")
            template.addTextSegment(";")
        }

        template.addTextSegment("}")
        return template
    }

    private fun addItem(field: DartComponent, isOne: Boolean): String? {
        var jsonName: String? = getJsonName(field);
        var fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java)
        return fromItem("map['${jsonName ?: field?.name}']", fieldType, isOne)
    }

    private fun fromItem(name: String?, fieldType: DartType?, isOne: Boolean = false): String? {
        if (null != fieldType) {
            var expression: DartReferenceExpression? = fieldType.referenceExpression
            if (isDartEnum(fieldType!!)) {
                return toEnum(name, expression?.text, isOne);
            } else {
                when (expression?.text) {
                    "int" -> return toInt(name)
                    "double" -> return toDouble(name)
                    "String" -> return toString(name)
                    "DateTime" -> return toDateTime(name)
                    "List" -> return toList(name, fieldType)
                    "Map" -> return toMap(name, fieldType)
                    "dynamic" -> return name
                }
                if (isOne) {
                    return "${expression?.text}.fromMap($name)"
                } else {
                    return "${expression?.text}.fromMap(value)"
                }
            }
        }
        return name
    }

    private fun toEnum(name: String?, enumName: String?, isOne: Boolean): String? {
        if (isDataVerify) {
            if (!isOne) {
                return "null == (value) ? null : (value is num ? $enumName.values[value.toInt()] : $enumName.values[int.tryParse(value)])"
            }
            return "null == (temp = $name) ? null : (temp is num ? $enumName.values[temp.toInt()] : $enumName.values[int.tryParse(temp)])"
        }
        return "$enumName.values[$name]"
    }

    private fun toList(name: String?, fieldType: DartType): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList

        if (null == typeList || 0 == typeList.size) {
            return "$name??[]"
        } else if (isDataVerify) {
            return "null == (temp = $name) ? [] : (temp is List ? temp.map((value)=>${fromItem("value", typeList?.get(0))}).toList() : [])"
        }
        return "$name?.map((value)=>${fromItem("value", typeList?.get(0))})?.toList()??[]"
    }

    private fun toMap(name: String?, fieldType: DartType): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList

        if (null == typeList || 0 == typeList.size) {
            return "$name??{}"
        } else if (isDataVerify) {
            return "null == (temp = $name) ? [] : (temp is Map ? temp.map((key,value)=> MapEntry(${fromItem("key", typeList?.get(0))},${fromItem("value", typeList?.get(1))})):[])"
        }
        return "$name?.map((key,value)=> MapEntry(${fromItem("key", typeList?.get(0))},${fromItem("value", typeList?.get(1))}))??{}"
    }

    private fun toInt(name: String?): String {
        if (isDataVerify) {
            return "null == (temp = $name) ? null : (temp is num ? temp.toInt() : int.tryParse(temp))"
        }
        return "$name"
    }

    private fun toDouble(name: String?): String {
        if (isDataVerify) {
            return "null == (temp = $name) ? null : (temp is num ? temp.toDouble() : double.tryParse(temp))"
        }
        return "$name"
    }

    private fun toString(name: String?): String {
        if (isDataVerify) {
            return "$name?.toString()"
        }
        return "$name"
    }

    private fun toDateTime(name: String?): String {
        if (isDataVerify) {
            return "null == (temp = $name) ? null : DateTime.tryParse(temp)"
        }
        return "DateTime.tryParse($name)"
    }

    fun buildFunctionsText(templateManager: TemplateManager?, fields: List<DartComponent>) {

    }

}
