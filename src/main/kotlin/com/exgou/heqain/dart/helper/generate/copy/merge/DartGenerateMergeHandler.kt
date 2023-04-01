package com.exgou.heqain.dart.helper.generate.copy.merge

import com.exgou.heqain.dart.helper.generate.DartHelperGenerateHandler
import com.exgou.heqain.dart.helper.utils.DartUtils
import com.intellij.openapi.editor.Editor
import com.jetbrains.lang.dart.DartComponentType
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.util.DartResolveUtil

class DartGenerateMergeHandler : DartHelperGenerateHandler() {

    override fun getTitle(): String {
        return "Generate Name"
    }
    override fun createFix(dartClass: DartClass): BaseCreateMethodsFix<*> {
        if (dartClass == null) {
            //            $$$reportNull$$$0(1);
        }

        val var10000 = DartGenerateMergeFix(dartClass)
        if (var10000 == null) {
            //            $$$reportNull$$$0(2);
        }

        return var10000
    }
    override fun collectCandidates(editor: Editor, dartClass: DartClass, candidates: MutableList<DartComponent>) {
        val list = ArrayList<DartComponent>()
        DartUtils.getFields(editor, dartClass, list)
        list.forEach {
            if (DartComponentType.typeOf(it) == DartComponentType.FIELD &&
                (it.isPublic || DartResolveUtil.sameLibrary(dartClass, it))
            ) {
                candidates.add(it)
            }
        }
    }
}
