package com.exgou.heqain.dart.helper.generate.bymap

import com.intellij.util.containers.ContainerUtil
import com.jetbrains.lang.dart.DartComponentType
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent

class DartGenerateFromMapAndToMapHandler(private val isToMap: Boolean) : BaseDartGenerateHandler() {


    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        return if (isToMap) {
            DartGenerateToMapFix(dartClass)
        } else {
            DartGenerateFromMapFix(dartClass)
        }
    }

    override fun getTitle(): String {
        return if (isToMap) "Add fromMap" else "Add toMap"
    }

    override fun collectCandidates(dartClass: DartClass, list: MutableList<DartComponent>) {
        list.addAll(ContainerUtil.findAll(this.computeClassMembersMap(dartClass, false).values) { component -> DartComponentType.typeOf(component) === DartComponentType.FIELD })
    }
}
