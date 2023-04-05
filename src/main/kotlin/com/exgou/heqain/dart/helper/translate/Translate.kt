package com.exgou.heqain.dart.helper.translate

import java.util.*

abstract class Translate {
    abstract fun toEnglish(text: String): String

    fun toClassName(name: String): String {
        var temp: String = "";
        Regex("[^\\da-zA-Z_]+").replace(name, ",").split(",").forEach {
            if (it.isNotEmpty()) {
                temp += it.subSequence(0, 1).toString().uppercase(Locale.getDefault()) + it.subSequence(1, it.length)
            }
        };
        return temp.ifEmpty {
            name
        }
    }

    companion object {
        private var translate: Translate? = null
        val `interface`: Translate
            get() = YouDao().also { translate = it }
    }


}
