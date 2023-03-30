package com.exgou.heqain.dart.helper.generate.bymap

import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler

class DartGenerateToMapAction : BaseDartGenerateAction() {
    override fun getGenerateHandler(): BaseDartGenerateHandler {
        return DartGenerateFromMapAndToMapHandler(true)
    }
}