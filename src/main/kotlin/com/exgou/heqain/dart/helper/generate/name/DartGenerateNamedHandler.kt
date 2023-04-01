package com.exgou.heqain.dart.helper.generate.name

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler
import com.exgou.heqain.dart.helper.utils.DartUtils

import com.intellij.openapi.editor.Editor


import com.jetbrains.lang.dart.DartComponentType
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.util.DartResolveUtil

class DartGenerateNamedHandler : DartHelperGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        if (dartClass == null) {
            //            $$$reportNull$$$0(1);
        }

        val var10000 = DartGenerateNamedFix(dartClass)
        if (var10000 == null) {
            //            $$$reportNull$$$0(2);
        }

        return var10000
    }
}
