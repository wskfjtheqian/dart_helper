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

class DartGenerateRequestFix(val project: Project, val editor: Editor, private val method: DartMethodDeclaration) {

    fun process() {
        val templateManager = TemplateManager.getInstance(project);
        var template: Template = this.buildFunctionsText(templateManager, editor) ?: return;

        WriteCommandAction.runWriteCommandAction(project) {
            val anchor: PsiElement? = method.functionBody!!.firstChild.firstChild.nextSibling;
            editor.caretModel.moveToOffset(anchor!!.textRange.startOffset)
            method.functionBody?.deleteChildRange(
                    method.functionBody!!.firstChild.firstChild.nextSibling,
                    method.functionBody!!.lastChild.lastChild.prevSibling
            )
            templateManager.startTemplate(editor, template)
            val dartComponent: PsiElement? = PsiTreeUtil.getParentOfType(anchor!!.findElementAt(editor.caretModel.offset), DartComponent::class.java)
        }
    }

    fun getParameter(method: DartMethodDeclaration): Array<DartNormalFormalParameter> {
        val ret: MutableList<DartNormalFormalParameter> = java.util.ArrayList()
        var parameterList: DartFormalParameterList = method.formalParameterList
        parameterList.children.forEach { it ->
            if (it is DartNormalFormalParameter) {
                ret.add(it)
            } else if (it is DartOptionalFormalParameters) {
                it.children.forEach {
                    if (it is DartDefaultFormalNamedParameter) {
                        ret.add(it.normalFormalParameter)
                    }
                }
            }
        }
        return ret.toTypedArray()
    }


    fun buildFunctionsText(templateManager: TemplateManager, editor: Editor): Template? {
        val template = templateManager.createTemplate(this.javaClass.name, "Network Request")
        template.addTextSegment("\nvar request = ")
        template.addTextSegment("dio.")
        var requestMethod = getRequestMethod();
        template.addTextSegment(if (requestMethod.isEmpty()) "post" else requestMethod)
        template.addTextSegment("<Map>(")

        template.addVariable(TextExpression("\"${getUrl(requestMethod)}\""), true)
        var parameters = getParameter(method)
        if (parameters.isNotEmpty()) {
            template.addTextSegment(",")
            var temp = "data"
            if ("get" == requestMethod || "head" == requestMethod || "delete" == requestMethod) {
                temp = "queryParameters"
            }
            template.addTextSegment(temp)

            template.addTextSegment(":{")
            parameters.forEach {
                template.addTextSegment("'${it.simpleFormalParameter?.name}'")
                template.addTextSegment(":")
                template.addTextSegment("${createParame(it.simpleFormalParameter!!)}")
                template.addTextSegment(",")
            }
            template.addTextSegment("}")
        }
        template.addTextSegment(");\n")

        var returnType: DartReturnType? = method.returnType
        if (null == returnType) {
            template.addTextSegment("return onMap(request, (map) => map)")
        } else {
            var type = returnType.type!!
            var expression: DartReferenceExpression? = type.referenceExpression
            if ("Future" == expression?.text) {
                var typeList = type?.typeArguments?.typeList?.typeList;
                if (null == typeList || typeList.isEmpty()) {
                    template.addTextSegment("return onMap(request, (map) => map)")
                } else {
                    template.addTextSegment(createReturn(typeList[0]))
                }
            } else {
                template.addTextSegment(createReturn(type))
            }
        }
        template.addTextSegment(";")
        return template
    }

    private fun createParame(parameter: DartSimpleFormalParameter): Any? {
        return parameParse(parameter.type, parameter.name!!);
    }

    private fun parameParse(type: DartType?, key: String): Any? {
        var expression: DartReferenceExpression? = type?.referenceExpression
        if (UiUtils.isDartEnum(type!!, editor)) {
            return "$key.index"
        }
        when (expression?.text) {
            "int" -> return key
            "double" -> return key
            "bool" -> return key
            "String" -> return key
            "DateTime" -> return "$key?.toString()"
            "List" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    key
                } else {
                    "$key?.map((map)=>${parameParse(typeList[0], "map")})?.toList()??[]"
                }
            }
            "Map" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    key
                } else {
                    "$key?.map((key, map) => MapEntry(${parameParse(typeList[0], "key")}, ${parameParse(typeList[1], "map")}))??{}"
                }
            }
        }
        return "$key?.toMap()"
    }

    private fun createReturn(type: DartType): String {
        return "return onMap<${type?.text}>(request, (map) => ${returnParse(type, "map")})"
    }

    private fun returnParse(type: DartType, key: String): String {
        var expression: DartReferenceExpression? = type.referenceExpression
        if (UiUtils.isDartEnum(type!!, editor)) {
            return "null == $key ? null : ($key is num ? ${expression?.text}.values[$key.toInt()] : ${expression?.text}.values[int.tryParse($key)])"
        }
        when (expression?.text) {
            "int" -> return "null == $key ? null : ($key is num ? $key.toInt() : int.tryParse($key))"
            "double" -> return "null == $key ? null : ($key is num ? $key.toDouble() : double.tryParse($key))"
            "bool" -> return "null == $key ? null : ($key is bool ? $key : bool.fromEnvironment($key))"
            "String" -> return "$key?.toString()"
            "DateTime" -> return "null == $key ? null : DateTime.parse($key.toString())"
            "List" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $key ? [] : ($key is List ? $key : [])"
                } else {
                    "null == $key ? [] : ($key is List ? $key.map((map)=>${returnParse(typeList[0], "map")}).toList() : [])"
                }
            }
            "Map" -> {
                var typeList = type?.typeArguments?.typeList?.typeList
                return if (null == typeList || typeList.isEmpty()) {
                    "null == $key ? {} : ($key is Map ? $key : {})"
                } else {
                    "null == $key ? {} : ($key is Map ? $key.map((key, map) => MapEntry(${returnParse(typeList[0], "key")}, ${returnParse(typeList[1], "map")})):{})"
                }
            }
        }
        var typeList = type?.typeArguments?.typeList?.typeList
        var ret: String = ""
        typeList?.forEach {
            ret += "," + "(map) =>" + returnParse(it, "map")
        }
        return "${expression?.text}.fromMap($key $ret)"
    }

    private fun getRequestMethod(): String {
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

    private fun getUrl(requestMethod: String): String {
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
