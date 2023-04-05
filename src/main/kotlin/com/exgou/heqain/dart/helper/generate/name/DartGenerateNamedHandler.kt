package com.exgou.heqain.dart.helper.generate.name

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler


import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass

class DartGenerateNamedHandler : DartHelperGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        return DartGenerateNamedFix(dartClass)
    }
}
