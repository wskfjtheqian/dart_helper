package com.exgou.heqain.dart.helper.generate.network

import com.exgou.heqain.dart.helper.utils.UiUtils
import com.exgou.heqain.dart.helper.utils.UiUtils.getRequestUrl
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.*

class DartGenerateResponseFix(val project: Project, val editor: Editor, private val dartClass: DartClass) {

    fun process() {
        val templateManager = TemplateManager.getInstance(project);
        var template: Template?;

        WriteCommandAction.runWriteCommandAction(project) {
            var anchor: PsiElement? = dartClass.findMethodByName("getHandlers")
            if (null != anchor) {
                template = this.buildFunctionsText(templateManager, editor, false)
                var method = anchor as DartMethodDeclaration;
                method.functionBody?.deleteChildRange(
                        method.functionBody!!.firstChild.firstChild.nextSibling,
                        method.functionBody!!.lastChild.lastChild.prevSibling
                )
                editor.caretModel.moveToOffset(method.functionBody!!.textRange.startOffset + 1);
            } else {
                template = this.buildFunctionsText(templateManager, editor, true)
                anchor = (dartClass as DartClassDefinition).classBody?.lastChild
                if (null != anchor) {
                    editor.caretModel.moveToOffset(anchor.textOffset);
                }
            }
            if (null != template) {
                templateManager.startTemplate(editor, template!!)
            }
        }
    }


    fun buildFunctionsText(templateManager: TemplateManager, editor: Editor, onlyBody: Boolean): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Network Response")
        if (onlyBody) template.addTextSegment("static Map<String, IHandler> getHandlers(GetRouter getRouter) {\n")
        template.addTextSegment("  return {\n")

        dartClass.methods.forEach {
            if (it is DartMethodDeclaration) {
                if (it.textRange.startOffset > dartClass.textRange.startOffset && it.textRange.endOffset < dartClass.textRange.endOffset) {
                    if(it.name != "getHandlers"){
                        addMethods(template, it)
                    }
                }
            }
        }
        template.addTextSegment("  };\n")
        if (onlyBody) template.addTextSegment("}\n")
        return template
    }

    private fun addMethods(template: Template, method: DartMethodDeclaration): Template? {
        var requestMethod = getRequestMethod(method);
        template.addVariable(TextExpression("\"${getUrl(requestMethod, method)}\""), true)
        template.addTextSegment(":")
        template.addTextSegment(if (requestMethod.isEmpty()) "post" else requestMethod)
        template.addTextSegment("Handler((Request request, map) {\n")
        template.addTextSegment("  var temp;\n")
        template.addTextSegment("  return getRouter(request).${method.name!!}(\n")
        var parameterList: DartFormalParameterList = method.formalParameterList
        parameterList.children.forEach {
            if (it is DartNormalFormalParameter) {
                var temp = it.simpleFormalParameter;
                template.addTextSegment("${paremParse(temp?.type!!, "map['${temp?.name}']", false)}")
                template.addTextSegment(",\n")
            } else if (it is DartOptionalFormalParameters) {
                it.children.forEach {
                    if (it is DartDefaultFormalNamedParameter) {
                        var temp = it.normalFormalParameter.simpleFormalParameter;
                        template.addTextSegment("${temp?.name}")
                        template.addTextSegment("${paremParse(temp?.type!!, "map['${temp?.name}']", false)}")
                        template.addTextSegment(",\n")
                    }
                }
            }
        }
        template.addTextSegment("  );\n")
        template.addTextSegment("}\n")
        template.addTextSegment("),\n")
        return template
    }

    private fun paremParse(type: DartType, key: String, isValue: Boolean): String {
        var value = if (isValue) key else "(temp = $key)"
        var temp = if (isValue) key else "temp"

        var expression: DartReferenceExpression? = type?.referenceExpression
        if (UiUtils.isDartEnum(type!!, editor)) {
            return "null == $value ? null : ($temp is num ? ${expression?.text}.values[$temp.toInt()] : ${expression?.text}.values[int.tryParse($temp)])"
        }
        when (expression?.text) {
            "int" -> return "null == $value ? null : ($temp is num ? $temp.toInt() : int.tryParse($temp))"
            "double" -> return "null ==$value ? null : ($temp is num ? $temp.toDouble() : double.tryParse($temp))"
            "bool" -> return "null == $value ? null : ($temp is bool ? $temp : bool.fromEnvironment($temp))"
            "String" -> return "$key?.toString()"
            "DateTime" -> return "null == $value ? null : ($temp is DateTime ? $temp : DateTime.tryParse($temp))"
            "List" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? [] : ($temp is List ? $temp : [])"
                } else {
                    "null == $value ? [] : ($temp is List ? $temp.map((map)=>${paremParse(typeList[0], "map", true)}).toList() : [])"
                }
            }
            "Map" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $value ? {} : ($temp is Map ? $temp : {})"
                } else {
                    "null == $value ? {} : ($temp is Map ? $temp.map((key, map) => MapEntry(${paremParse(typeList[0], "key", true)}, ${paremParse(typeList[1], "map", true)})):{})"
                }
            }
        }
        var typeList = type?.typeArguments?.typeList?.typeList
        var ret: String = ""
        typeList?.forEach {
            ret += "," + "(map) =>" + paremParse(it, "map", true)
        }
        return "${expression?.text}.fromMap($key $ret)"
    }

    private fun getRequestMethod(method: DartMethodDeclaration): String {
        var name = method.name!!;
        if (0 == name.indexOf("get")) {
            return "get";
        } else if (0 == name.indexOf("head")) {
            return "head";
        } else if (0 == name.indexOf("post")) {
            return "post";
        } else if (0 == name.indexOf("put")) {
            return "put";
        } else if (0 == name.indexOf("delete")) {
            return "delete";
        } else if (0 == name.indexOf("patch")) {
            return "patch";
        }
        return "";
    }

    private fun getUrl(requestMethod: String, method: DartMethodDeclaration): String {
        var url: String? = getRequestUrl(method);
        var isUrl = true;
        if (null == url) {
            isUrl = false;
            url = "/" + method.name.toString().substring(requestMethod.length).toLowerCase()
        }
        var temp: PsiElement = method;
        while (null != temp && temp !is DartClass) {
            temp = temp.parent;
        }
        if (null != temp && temp is DartClass) {
            var classUrl = getRequestUrl(temp);
            if (null == classUrl) {
                if (!isUrl) {
                    temp.implementsList?.forEach {
                        url = "/" + it.referenceExpression?.text.toString().toLowerCase() + url
                    }
                }
            } else {
                url = classUrl + url
            }
        }
        return url!!
    }
}
