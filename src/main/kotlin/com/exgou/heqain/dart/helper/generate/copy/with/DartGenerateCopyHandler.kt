package com.exgou.heqain.dart.helper.generate.copy.with

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass

class DartGenerateCopyHandler : DartHelperGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }

    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        if (dartClass == null) {
            //            $$$reportNull$$$0(1);
        }

        val var10000 = DartGenerateCopyFix(dartClass)
        if (var10000 == null) {
            //            $$$reportNull$$$0(2);
        }

        return var10000
    }


}
