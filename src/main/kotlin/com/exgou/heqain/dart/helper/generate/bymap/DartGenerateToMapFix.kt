package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.utils.DartUtils
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
    override fun processElements(project: Project, editor: Editor, elementsToProcess: MutableSet<DartComponent>) {
        val templateManager = TemplateManager.getInstance(project)
        val toMap = myDartClass.findMemberByName("toMap");
        val template = this.buildFunctionsText(templateManager, elementsToProcess, editor);
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

    fun buildFunctionsText(
        templateManager: TemplateManager,
        dartComponent: MutableSet<DartComponent>,
        editor: Editor
    ): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Dart")
        template.isToReformat = true
        template.addTextSegment("Map<String, dynamic> toMap() {")
        template.addTextSegment("return {")
        var genericToMap = false
        elementsToProcess.forEach { it ->
            val jsonName: String? = DartUtils.getJsonName(it)
            template.addTextSegment("'${jsonName ?: it?.name}':")
            template.addTextSegment(
                addItem(it, editor) {
                    genericToMap = it
                }
            )
            template.addTextSegment(",")
        }

        template.addTextSegment("};")
        template.addTextSegment("}")

        if (genericToMap && null == myDartClass.findMemberByName("_genericToMap")) {
            template.addTextSegment(
                "  " +
                        "dynamic _genericToMap(dynamic val) {\n" +
                        "    if (null == val) {\n" +
                        "      return null;\n" +
                        "    } else if (val is num) {\n" +
                        "      return val;\n" +
                        "    } else if (val is String) {\n" +
                        "      return val;\n" +
                        "    } else if (val is bool) {\n" +
                        "      return val;\n" +
                        "    } else if (val is DateTime) {\n" +
                        "      return val.toString();\n" +
                        "    } else if (val is List) {\n" +
                        "      return val.map(_genericToMap).toList();\n" +
                        "    } else if (val is Map) {\n" +
                        "      return val.map((key, value) => MapEntry(_genericToMap(key), _genericToMap(value)));\n" +
                        "    } else if (val is Set) {\n" +
                        "      return val.map(_genericToMap).toSet();\n" +
                        "    }\n" +
                        "    try {\n" +
                        "      return val?.toMap();\n" +
                        "    } catch (e) {\n" +
                        "      return val?.index;\n" +
                        "    }\n" +
                        "  }"
            )
        }
        return template
    }

    private fun addItem(field: DartComponent, editor: Editor, onGenericToMap: (Boolean) -> Unit): String {
        val fieldType = PsiTreeUtil.getChildOfType(field, DartType::class.java)
        return fromItem(fieldType, field.name!!, editor, onGenericToMap)
    }

    private fun isParameters(param: String?): Boolean {
        myDartClass.typeParameters?.typeParameterList?.forEach {
            if (it.text == param) {
                return true
            }
        }
        return false
    }

    private fun fromItem(type: DartType?, key: String, editor: Editor, onGenericToMap: (Boolean) -> Unit): String {
        val expression: DartReferenceExpression? = type?.referenceExpression
        if (DartUtils.isDartEnum(type!!, editor)) {
            return "$key?.index"
        }
        if (isParameters(expression?.text)) {
            onGenericToMap(true)
            return "_genericToMap($key )"
        }
        when (expression?.text) {
            "int" -> return key
            "double" -> return key
            "bool" -> return key
            "String" -> return key
            "DateTime" -> {
                return if (DartUtils.isNullPointer(type)) {
                    "$key?.toString()"
                } else {
                    "$key.toString()"
                }
            }
            "List" -> {
                val typeList = type.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    key
                } else {
                    return if (DartUtils.isNullPointer(type)) {
                        "$key?.map((map)=>${fromItem(typeList[0], "map", editor, onGenericToMap)}).toList()??[]"
                    } else {
                        "$key.map((map)=>${fromItem(typeList[0], "map", editor, onGenericToMap)}).toList()"
                    }

                }
            }
            "Map" -> {
                val typeList = type.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    key
                } else {
                    return if (DartUtils.isNullPointer(type)) {
                        "$key?.map((key, map) => MapEntry(${
                            fromItem(
                                typeList[0],
                                "key",
                                editor,
                                onGenericToMap
                            )
                        }, ${fromItem(typeList[1], "map", editor, onGenericToMap)}))??{}"
                    } else {
                        "$key.map((key, map) => MapEntry(${
                            fromItem(
                                typeList[0],
                                "key",
                                editor,
                                onGenericToMap
                            )
                        }, ${fromItem(typeList[1], "map", editor, onGenericToMap)}))"
                    }
                }
            }
        }

        return if (DartUtils.isNullPointer(type)) {
            "$key?.toMap()"
        } else {
            "$key.toMap()"
        }
    }


}
