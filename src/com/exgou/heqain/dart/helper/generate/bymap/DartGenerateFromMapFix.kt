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
        template.addTextSegment("factory ${myDartClass.name}.fromMap(Map<String, dynamic> map) {")
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

    private fun addItem(field: DartComponent, isOne: Boolean, editor: Editor): String? {
        var jsonName: String? = getJsonName(field);
        var fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java)
        return fromItem("map['${jsonName ?: field?.name}']", fieldType, editor, isOne)
    }

    private fun fromItem(name: String?, fieldType: DartType?, editor: Editor, isOne: Boolean = false): String? {
        if (null != fieldType) {
            var expression: DartReferenceExpression? = fieldType.referenceExpression
            if (isDartEnum(fieldType!!, editor)) {
                return toEnum(name, expression?.text, isOne);
            } else {
                when (expression?.text) {
                    "int" -> return toInt(name, isOne)
                    "double" -> return toDouble(name, isOne)
                    "bool" -> return toBool(name, isOne)
                    "String" -> return toString(name, isOne)
                    "DateTime" -> return toDateTime(name, isOne)
                    "List" -> return toList(name, fieldType, editor, isOne)
                    "Map" -> return toMap(name, fieldType, editor, isOne)
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

    private fun toBool(name: String?, isOne: Boolean): String? {
        if (!isOne) {
            return "null == (value) ? null : (value is bool ? value :(value is num ? 0 != value : bool.fromEnvironment(value.toString())))"
        }
        return "null == (temp = $name) ? null : (temp is bool ? temp :(temp is num ? 0 != temp : bool.fromEnvironment(temp.toString())))"
    }

    private fun toEnum(name: String?, enumName: String?, isOne: Boolean): String? {
        if (!isOne) {
            return "null == (value) ? null : (value is num ? $enumName.values[value.toInt()] : $enumName.values[int.tryParse(value)])"
        }
        return "null == (temp = $name) ? null : (temp is num ? $enumName.values[temp.toInt()] : $enumName.values[int.tryParse(temp)])"
    }

    private fun toList(name: String?, fieldType: DartType, editor: Editor, isOne: Boolean): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList

        if (null == typeList || 0 == typeList.size) {
            return "$name??[]"
        } else if (!isOne) {
            return "$name?.map((value)=>${fromItem("value", typeList?.get(0), editor)})?.toList()??[]"
        }
        return "null == (temp = $name) ? [] : (temp is List ? temp.map((value)=>${fromItem("value", typeList?.get(0), editor)}).toList() : [])"
    }

    private fun toMap(name: String?, fieldType: DartType, editor: Editor, isOne: Boolean): String {
        var typeList = fieldType.typeArguments?.typeList?.typeList

        if (null == typeList || 0 == typeList.size) {
            return "$name??{}"
        } else if (!isOne) {
            return "$name?.map((key,value)=> MapEntry(${fromItem("key", typeList?.get(0), editor)},${fromItem("value", typeList?.get(1), editor)}))??{}"
        }
        return "null == (temp = $name) ? [] : (temp is Map ? temp.map((key,value)=> MapEntry(${fromItem("key", typeList?.get(0), editor)},${fromItem("value", typeList?.get(1), editor)})):[])"
    }

    private fun toInt(name: String?, isOne: Boolean): String {
        if (!isOne) {
            return "null == (value) ? null : (value is num ? value.toInt() : int.tryParse(value))"
        }
        return "null == (temp = $name) ? null : (temp is num ? temp.toInt() : int.tryParse(temp))"
    }

    private fun toDouble(name: String?, isOne: Boolean): String {
        if (!isOne) {
            return "null == (value) ? null : (value is num ? value.toDouble() : double.tryParse(value))"
        }
        return "null == (temp = $name) ? null : (temp is num ? temp.toDouble() : double.tryParse(temp))"

    }

    private fun toString(name: String?, isOne: Boolean): String {
        if (!isOne) {
            return "value?.toString()"
        }
        return "$name?.toString()"
    }

    private fun toDateTime(name: String?, isOne: Boolean): String {
        if (!isOne) {
            return "null == (value) ? null : DateTime.tryParse(value)"
        }
        return "null == (temp = $name) ? null : DateTime.tryParse(temp)"
    }


}
