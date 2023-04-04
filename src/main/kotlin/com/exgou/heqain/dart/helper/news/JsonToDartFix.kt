package com.exgou.heqain.dart.helper.news

import com.exgou.heqain.dart.helper.translate.Translate
import com.intellij.json.psi.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

class FieldType(var type: Int, var name: String) {
    override fun toString(): String {
        return "FieldType(name='$name')"
    }
}

class JsonToDartFix(var mProject: Project) {
    internal var _Int: Int = 1
    internal var _Double: Int = 2 or _Int
    internal var _Bool: Int = 4
    internal var _DateTime: Int = 8
    internal var _String: Int = 16 or _Bool or _Double or _DateTime
    internal var _List: Int = 32
    internal var _Class: Int = 64
    internal var _Dynamic: Int = 128 or _String or _DateTime or _List or _Class
    internal val types = intArrayOf(_Int, _Double, _Bool, _DateTime, _String, _List, _Class, _Dynamic)

    var listCalss = HashMap<String, HashMap<String, FieldType>>()
    var _temp = 0;
    fun toDart(document: Document, name: String?) {
        val file = PsiDocumentManager.getInstance(mProject).getPsiFile(document) as? JsonFile ?: return;
        val value = file.topLevelValue as? JsonObject ?: return;
        name ?: return

        var temp = toClassName(toName(name))
        val classs = HashMap<String, FieldType>()
        formJsonObject(value, temp, classs);
        listCalss[temp] = classs
    }

    override fun toString(): String {
        var ret: StringBuffer = StringBuffer();
        listCalss.forEach {
            ret.append("class ").append(it.key).append("{\n")

            var temp: StringBuffer = StringBuffer();
            it.value.forEach { key ->
                ret.append("//JsonName:").append(key.key).append("\n");
                var name = toFieldName(toName(key.key))
                ret.append(key.value.name).append(" ").append(name).append(";\n\n");
                temp.append("this.").append(name).append(",\n");
            }

            if (0 == it.value.size) {
                ret.append("${it.key}(")
            } else {
                ret.append("${it.key}({")
            }

            ret.append(temp)

            if (0 == it.value.size) {
                ret.append(");\n")
            } else {
                ret.append("});\n")
            }
            ret.append("}\n\n")
        }

        return ret.toString();
    }

    private fun toFieldName(name: String): String {
        var temp = toClassName(name)
        if (temp.isNotEmpty()) {
            return temp.subSequence(0, 1).toString().toLowerCase() + temp.subSequence(1, temp.length)
        }
        return ""
    }

    private fun toName(name: String): String {
        var ret: String;
        var reg = Regex("[^\\da-zA-Z_]+")
        if (reg.containsMatchIn(name)) {
            ret = Translate.`interface`.toEnglish(name);
        } else {
            ret = name
        }

        if (reg.containsMatchIn(ret)) {
            ret = ret.replace(reg, "${_temp}");
            _temp++;
            if (ret.matches(Regex("(^[\\d]\\w+)|^[\\d]"))) {
                ret = "t${ret}"
            }
        } else {
            if (name.matches(Regex("(^[\\d]\\w+)|^[\\d]"))) {
                ret = "t${_temp}${name}"
            }
        }

        return ret;
    }

    private fun toClassName(name: String): String {
        var temp: String = "";
        name.split("_").forEach {
            if (it.isNotEmpty()) {
                temp += it.subSequence(0, 1).toString().toUpperCase() + it.subSequence(1, it.length)
            }
        };
        return temp
    }

    private fun formJsonObject(json: JsonObject, name: String?, classs: HashMap<String, FieldType>) {
        json.propertyList.forEach {
            var temp = classs[it.name]
            var type = fomrJsonValue(it.value, name + toClassName(toName(it.name)))
            if (null == temp) {
                classs[it.name] = type
            } else {
                if (!(type.type == _Class && temp?.type == _Class)) {
                    var end = types.indexOf(temp?.type!!);
                    for (i in end..types.size) {
                        var item = types[i];
                        if (0 != (item and type.type) && 0 != (item and temp?.type!!)) {
                            classs[it.name] = if (item > type.type) {
                                FieldType(item, type.name)
                            } else {
                                type
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private fun fomrJsonValue(value: JsonValue?, name: String?): FieldType {
        if (value is JsonObject) {
            if (null != name) {
                val temp = listCalss[name] ?: HashMap<String, FieldType>()
                formJsonObject(value, name, temp);
                listCalss[name] = temp;
                return FieldType(_Class, name);
            }
        } else if (value is JsonArray) {
            if (null != name) {
                var ret: FieldType? = null;
                value.valueList.forEach {
                    var type = fomrJsonValue(it, name);
                    if (null == ret) {
                        ret = type;
                    }
                    if (!(type.type == _Class && ret?.type == _Class)) {
                        var end = types.indexOf(ret?.type!!);
                        for (i in end..types.size) {
                            var item = types[i];
                            if (0 != (item and type.type) && 0 != (item and ret?.type!!)) {
                                ret = if (item > type.type) {
                                    FieldType(item, type.name)
                                } else {
                                    type
                                }
                                break;
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
                return ret!!;
            }
            return FieldType(_List, "List");
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
        if (Regex("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s+(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d\$").matches(value)) {
            return true
        }

        return false
    }
}
