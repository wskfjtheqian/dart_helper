package com.exgou.heqain.dart.helper.news

import com.exgou.heqain.dart.helper.translate.Translate
import com.intellij.json.psi.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import java.util.*

class FieldType(var type: Int, var name: String) {
    override fun toString(): String {
        return "FieldType(name='$name')"
    }
}

class JsonToDartFix(var mProject: Project, val toMap: Boolean, val formMap: Boolean) {
    private var _Int: Int = 1
    private var _Double: Int = 2 or _Int
    private var _Bool: Int = 4
    private var _DateTime: Int = 8
    private var _String: Int = 16 or _Bool or _Double or _DateTime
    private var _List: Int = 32
    private var _Class: Int = 64
    private var _Dynamic: Int = 128 or _String or _DateTime or _List or _Class
    private val types = intArrayOf(_Int, _Double, _Bool, _DateTime, _String, _List, _Class, _Dynamic)

    var listCalss = HashMap<String, HashMap<String, FieldType>>()
    var _temp = 0
    fun toDart(document: Document, name: String?) {
        val file = PsiDocumentManager.getInstance(mProject).getPsiFile(document) as? JsonFile ?: return
        val value = file.topLevelValue as? JsonObject ?: return
        name ?: return

        val temp = toClassName(toName(name))
        val clazz = HashMap<String, FieldType>()
        formJsonObject(value, temp, clazz)
        listCalss[temp] = clazz
    }

    override fun toString(): String {
        val ret = StringBuffer()
        listCalss.forEach {
            ret.append("class ").append(it.key).append("{\n")

            it.value.forEach { key ->
                ret.append("//JsonName:").append(key.key).append("\n")
                val name = toFieldName(toName(key.key))
                ret.append(key.value.name).append(" ").append(name).append(";\n\n")
            }

            ret.append("${it.key}(").append(if (0 == it.value.size) "" else "{")
            it.value.forEach { key ->
                val name = toFieldName(toName(key.key))
                ret.append("this.").append(name).append(",\n")
            }
            ret.append(if (0 == it.value.size) "" else "}").append(");\n")

            if (toMap) {
                ret.append("Map<String, dynamic> toMap() {\n")
                ret.append("return {\n")
                it.value.forEach { key ->
                    val name = toFieldName(toName(key.key))
                    ret.append("\"").append(name).append("\":")
                    ret.append(toMap(name, key.value.name))
                    ret.append(",\n")
                }
                ret.append("};\n")
                ret.append("}\n")
            }


            ret.append("}\n\n")
        }

        return ret.toString()
    }

    private fun toMap(name: String, key: String): String {
        return if (0 == key.indexOf("DateTime")) {
            "${name}.toString()"
        } else if (0 == key.indexOf("bool")) {
            name
        } else if (0 == key.indexOf("double")) {
            name
        } else if (0 == key.indexOf("int")) {
            name
        } else if (0 == key.indexOf("String")) {
            name
        } else if (0 == key.indexOf("List<")) {
            val key = key.substring(5, key.length - 2)
            "${name}?.map((e)=>${toMap(if ('?' == key.last()) "e?" else "e", key)}).toList()"
        } else {
            "$name.toMap()"
        }
    }

    private fun toFieldName(name: String): String {
        val temp = toClassName(name)
        if (temp.isNotEmpty()) {
            return temp.subSequence(0, 1).toString().lowercase(Locale.getDefault()) + temp.subSequence(1, temp.length)
        }
        return ""
    }

    private fun toName(name: String): String {
        var ret: String
        val reg = Regex("[^\\da-zA-Z_]+")
        if (reg.containsMatchIn(name)) {
            ret = Translate.`interface`.toEnglish(name)
        } else {
            ret = name
        }

        if (reg.containsMatchIn(ret)) {
            ret = ret.replace(reg, "${_temp}")
            _temp++
            if (ret.matches(Regex("(^[\\d]\\w+)|^[\\d]"))) {
                ret = "t${ret}"
            }
        } else {
            if (name.matches(Regex("(^[\\d]\\w+)|^[\\d]"))) {
                ret = "t${_temp}${name}"
            }
        }

        return ret
    }

    private fun toClassName(name: String): String {
        var temp = ""
        name.split("_").forEach {
            if (it.isNotEmpty()) {
                temp += it.subSequence(0, 1).toString().uppercase(Locale.getDefault()) + it.subSequence(1, it.length)
            }
        }
        return temp
    }

    private fun formJsonObject(json: JsonObject, name: String?, classs: HashMap<String, FieldType>) {
        json.propertyList.forEach {
            val temp = classs[it.name]
            val type = fomrJsonValue(it.value, name + toClassName(toName(it.name)))
            if (null == temp) {
                classs[it.name] = type
            } else {
                if (!(type.type == _Class && temp.type == _Class)) {
                    val end = types.indexOf(temp.type)
                    for (i in end..types.size) {
                        val item = types[i]
                        if (0 != (item and type.type) && 0 != (item and temp.type)) {
                            classs[it.name] = if (item > type.type) {
                                FieldType(item, type.name)
                            } else {
                                type
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    private fun fomrJsonValue(value: JsonValue?, name: String?): FieldType {
        if (value is JsonObject) {
            if (null != name) {
                val temp = listCalss[name] ?: HashMap()
                formJsonObject(value, name, temp)
                listCalss[name] = temp
                return FieldType(_Class, "$name?")
            }
        } else if (value is JsonArray) {
            if (null != name) {
                var ret: FieldType? = null
                value.valueList.forEach {
                    val type = fomrJsonValue(it, name)
                    if (null == ret) {
                        ret = type
                    }
                    if (!(type.type == _Class && ret?.type == _Class)) {
                        val end = types.indexOf(ret?.type!!)
                        for (i in end..types.size) {
                            val item = types[i]
                            if (0 != (item and type.type) && 0 != (item and ret?.type!!)) {
                                ret = if (item > type.type) {
                                    FieldType(item, type.name + "?")
                                } else {
                                    type
                                }
                                break
                            }
                        }
                    }
                }
                if (null == ret) {
                    ret = FieldType(_Dynamic, "dynamic")
                }
                when (ret?.type) {
                    _Int -> {
                        ret?.name = "int?"
                    }
                    _Double -> {
                        ret?.name = "double?"
                    }
                    _Bool -> {
                        ret?.name = "bool?"
                    }
                    _String -> {
                        ret?.name = "String?"
                    }
                    _List -> {
                        ret?.name = "List?"
                    }
                    _DateTime -> {
                        ret?.name = "DateTime?"
                    }
                    _Dynamic -> {
                        ret?.name = "dynamic"
                    }
                }
                ret?.type = _List
                ret?.name = "List<${ret?.name}>?"
                return ret!!
            }
            return FieldType(_List, "List")
        } else if (value is JsonStringLiteral) {
            return if (isDateTime(value.value)) {
                FieldType(_DateTime, "DateTime?")
            } else {
                FieldType(_String, "String?")
            }

        } else if (value is JsonBooleanLiteral) {
            return FieldType(_Bool, "bool?")


        } else if (value is JsonNumberLiteral) {
            return if (-1 == value.text.indexOf('.')) {
                FieldType(_Int, "int?")
            } else {
                FieldType(_Double, "double?")
            }

        } else if (value is JsonNullLiteral) {
            return FieldType(_Dynamic, "dynamic")
        }
        return FieldType(_Dynamic, "dynamic")
    }

    private fun isDateTime(value: String): Boolean {
        if (Regex("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\$").matches(value)) {
            return true
        }

        if (Regex("^(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\$").matches(value)) {
            return true
        }
        if (Regex("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\$").matches(
                value
            )
        ) {
            return true
        }

        return false
    }
}
