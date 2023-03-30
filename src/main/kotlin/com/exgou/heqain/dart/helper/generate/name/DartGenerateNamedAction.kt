package com.exgou.heqain.dart.helper.generate.name

import com.exgou.heqain.dart.helper.generate.copy.with.DartGenerateCopyHandler
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler


class DartGenerateNamedAction : BaseDartGenerateAction() {

    override fun getGenerateHandler(): BaseDartGenerateHandler {
        return DartGenerateNamedHandler()
    }
}


