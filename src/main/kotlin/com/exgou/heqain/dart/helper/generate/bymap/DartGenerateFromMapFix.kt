package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.utils.DartUtils
import com.exgou.heqain.dart.helper.utils.DartUtils.getJsonName
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

class DartGenerateFromMapFix(dartClass: DartClass) : BaseCreateMethodsFix<DartComponent>(dartClass) {
    override fun processElements(project: Project, editor: Editor, elementsToProcess: MutableSet<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        val toMap = myDartClass.findNamedConstructor("fromMap")
        val template = this.buildFunctionsText(templateManager, elementsToProcess, editor)
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

    private fun buildFunctionsText(
        templateManager: TemplateManager,
        dartComponent: MutableSet<DartComponent>,
        editor: Editor
    ): Template? {
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
        var ret = "static  ${myDartClass.name}? fromMap(dynamic map"
        myDartClass.typeParameters?.typeParameterList?.forEach {
            ret += "," + it.text + " Function(dynamic map) call" + it.text
        }

        return "$ret) {";
    }

    private fun addItem(field: DartComponent, isOne: Boolean, editor: Editor): String? {
        val jsonName: String? = getJsonName(field)
        val fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java)
        return fromItem(fieldType, "map['${jsonName ?: field.name}']", editor, false)
    }

    private fun fromItem(type: DartType?, key: String, editor: Editor, isValue: Boolean): String {
        val value = if (isValue) key else "(temp = $key)"
        val temp = if (isValue) key else "temp"

        val expression: DartReferenceExpression? = type?.referenceExpression
        if (DartUtils.isDartEnum(type!!, editor)) {
            return "null == $value ? null : ($temp is num ? ${expression?.text}.values[$temp.toInt()] : ${expression?.text}.values[int.tryParse($temp)])"
        }
        if (isParameters(expression?.text)) {
            return "call${expression?.text}($key)"
        }

        when (expression?.text) {
            "int" -> return if (DartUtils.isNullPointer(type)) {
                "null == $value ? null : ($temp is num ? $temp.toInt() : num.tryParse($temp)?.toInt())"
            } else {
                "null == $value ? 0 : ($temp is num ? $temp.toInt() : (num.tryParse($temp)?.toInt() ?? 0 ))"
            }
            "double" -> return if (DartUtils.isNullPointer(type)) {
                "null ==$value ? null : ($temp is num ? $temp.toDouble() : num.tryParse($temp)?.toDouble())"
            } else {
                "null ==$value ? 0.0 : ($temp is num ? $temp.toDouble() : (num.tryParse($temp)?.toDouble() ?? 0.0))"
            }
            "bool" -> return if (DartUtils.isNullPointer(type)) {
                "null == $value ? null : ($temp is bool ? $temp : ($temp is num ? 0 != $temp.toInt():('true' == temp.toString())))"
            } else {
                "null == $value ? false : ($temp is bool ? $temp : ($temp is num ? 0 != $temp.toInt():('true' == temp.toString())))"
            }
            "String" -> return if (DartUtils.isNullPointer(type)) {
                "$key?.toString()"
            } else {
                "$key?.toString() ?? \"\""
            }
            "DateTime" -> return if (DartUtils.isNullPointer(type)) {
                "null == $value ? null : ($temp is DateTime ? $temp : DateTime.tryParse($temp))"
            } else {
                "null == $value ? DateTime.now() : ($temp is DateTime ? $temp : DateTime.tryParse($temp) ?? DateTime.now())"
            }
            "List" -> {
                val typeList = type.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? [] : ($temp is List ? $temp : [])"
                } else {
                    "null == $value ? [] : ($temp is List ? $temp.map((map)=>${
                        fromItem(
                            typeList[0],
                            "map",
                            editor,
                            true
                        )
                    }).toList() : [])"
                }
            }
            "Map" -> {
                val typeList = type.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? {} : ($temp is Map ? $temp : {})"
                } else {
                    "null == $value ? {} : ($temp is Map ? $temp.map((key, map) => MapEntry(${
                        fromItem(
                            typeList[0],
                            "key",
                            editor,
                            true
                        )
                    }, ${fromItem(typeList[1], "map", editor, true)})):{})"
                }
            }
            "dynamic" -> {
                return key
            }
        }
        val typeList = type.typeArguments?.typeList?.typeList
        var ret = ""
        typeList?.forEach {
            ret += "," + "(map) =>" + fromItem(it, "map", editor, true)
        }


        return if(DartUtils.isNullPointer(type)){
            "${expression?.text}.fromMap($key $ret)"
        }else{
            "${expression?.text}.fromMap($key $ret) ?? ${expression?.text}()"
        }
    }


    private fun isParameters(param: String?): Boolean {
        myDartClass.typeParameters?.typeParameterList?.forEach {
            if (it.text == param) {
                return true
            }
        }
        return false
    }
}
