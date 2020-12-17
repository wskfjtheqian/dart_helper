package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.utils.UiUtils
import com.exgou.heqain.dart.helper.utils.UiUtils.getJsonName
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.*

class DartGenerateFromMapFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {
    override fun processElements(project: Project, editor: Editor, elementsToProcess: MutableSet<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        var toMap = myDartClass.findNamedConstructor("fromMap");
        var template = this.buildFunctionsText(templateManager, elementsToProcess, editor);
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

    fun buildFunctionsText(templateManager: TemplateManager, dartComponent: MutableSet<DartComponent>, editor: Editor): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
        template.isToReformat = true
        template.addTextSegment(careteFactory(myDartClass))
        template.addTextSegment("if (null == map) return null;")
        template.addTextSegment("var temp;")
        template.addTextSegment("return ${myDartClass.name}(")

        elementsToProcess.forEach {
            template.addTextSegment("${it.name!!}:")
            template.addTextSegment(addItem(it, true, editor) ?: "")
            template.addTextSegment(",\n")
        }

        template.addTextSegment(");")
        template.addTextSegment("}")
        return template
    }

    private fun careteFactory(myDartClass: DartClass): String {
        var ret = "factory ${myDartClass.name}.fromMap(dynamic map"
        myDartClass.typeParameters?.typeParameterList?.forEach {
            ret += "," + it.text + " Function(dynamic map) call" + it.text
        }

        return "$ret) {";
    }

    private fun addItem(field: DartComponent, isOne: Boolean, editor: Editor): String? {
        var jsonName: String? = getJsonName(field);
        var fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java)
        return fromItem(fieldType, "map['${jsonName ?: field?.name}']", editor, false)
    }

    private fun fromItem(type: DartType?, key: String, editor: Editor, isValue: Boolean): String {
        var value = if (isValue) key else "(temp = $key)"
        var temp = if (isValue) key else "temp"

        var expression: DartReferenceExpression? = type?.referenceExpression
        if (UiUtils.isDartEnum(type!!, editor)) {
            return "null == $value ? null : ($temp is num ? ${expression?.text}.values[$temp.toInt()] : ${expression?.text}.values[int.tryParse($temp)])"
        }
        if (isParameters(expression?.text)) {
            return "call${expression?.text}($key)"
        }
        when (expression?.text) {
            "int" -> return "null == $value ? null : ($temp is num ? $temp.toInt() : int.tryParse($temp))"
            "double" -> return "null ==$value ? null : ($temp is num ? $temp.toDouble() : double.tryParse($temp))"
            "bool" -> return "null == $value ? null : ($temp is bool ? $temp : ($temp is num ? 0 != $temp.toInt():('true'==temp.toString()))))"
            "String" -> return "$key?.toString()"
            "DateTime" -> return "null == $value ? null : ($temp is DateTime ? $temp : DateTime.tryParse($temp))"
            "List" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? [] : ($temp is List ? $temp : [])"
                } else {
                    "null == $value ? [] : ($temp is List ? $temp.map((map)=>${fromItem(typeList[0], "map", editor, true)}).toList() : [])"
                }
            }
            "Map" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? {} : ($temp is Map ? $temp : {})"
                } else {
                    "null == $value ? {} : ($temp is Map ? $temp.map((key, map) => MapEntry(${fromItem(typeList[0], "key", editor, true)}, ${fromItem(typeList[1], "map", editor, true)})):{})"
                }
            }
            "dynamic" -> {
                return key
            }
        }
        var typeList = type?.typeArguments?.typeList?.typeList
        var ret: String = ""
        typeList?.forEach {
            ret += "," + "(map) =>" + fromItem(it, "map", editor, true)
        }
        return "${expression?.text}.fromMap($key $ret)"
    }


    private fun isParameters(param: String?): Boolean {
        myDartClass.typeParameters?.typeParameterList?.forEach {
            if (it.text == param) {
                return true;
            }
        }
        return false;
    }
}
