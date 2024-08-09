package com.exgou.heqain.dart.helper.generate.copy.fill

import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler


class DartGenerateCopyAction : BaseDartGenerateAction() {

    override fun getGenerateHandler(): BaseDartGenerateHandler {
        return DartGenerateCopyHandler()
    }
}


