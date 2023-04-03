package com.exgou.heqain.dart.helper.generate.bymap

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass

class DartGenerateFromMapAndToMapHandler(private val isToMap: Boolean) : DartHelperGenerateHandler() {


    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        return if (isToMap) {
            DartGenerateToMapFix(dartClass)
        } else {
            DartGenerateFromMapFix(dartClass)
        }
    }

    override fun getTitle(): String {
        return if (!isToMap) "Add fromMap" else "Add toMap"
    }
}
