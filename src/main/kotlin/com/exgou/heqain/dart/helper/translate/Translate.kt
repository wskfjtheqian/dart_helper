package com.exgou.heqain.dart.helper.generate.translate

import com.exgou.heqain.dart.helper.translate.YouDao

abstract class Translate {
    abstract fun toEnglish(text: String): String

    fun toClassName(name: String): String {
        var temp: String = "";
        Regex("[^\\da-zA-Z_]+").replace(name, ",").split(",").forEach {
            if (it.isNotEmpty()) {
                temp += it.subSequence(0, 1).toString().toUpperCase() + it.subSequence(1, it.length)
            }
        };
        return if (temp.isEmpty()) {
            name
        } else {
            temp
        }
    }

    companion object {
        var _interface: Translate? = null
        val `interface`: Translate
            get() = YouDao().also { _interface = it }
    }


}