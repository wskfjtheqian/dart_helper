package com.exgou.heqain.dart.helper.generate.copy.fill

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass

class DartGenerateCopyHandler : DartHelperGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        return DartGenerateCopyFix(dartClass)
    }

}


