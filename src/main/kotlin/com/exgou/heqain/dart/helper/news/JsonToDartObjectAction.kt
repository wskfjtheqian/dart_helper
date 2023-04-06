package com.exgou.heqain.dart.helper.news

import com.intellij.ide.util.DirectoryChooserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.jetbrains.lang.dart.DartFileType
import java.util.*
import java.util.regex.Pattern


class JsonToDartObjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        val view = e.getData(LangDataKeys.IDE_VIEW)
        if (null != view && project != null) {
            val directory = DirectoryChooserUtil.getOrChooseDirectory(view)
            JsonToDartObject.main(project) { name: String, text: String ->
                onSave(project, directory, name, text)
            }
        }
    }

    private fun onSave(
        project: Project,
        directory: PsiDirectory?,
        name: String,
        text: String
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val file = PsiFileFactory.getInstance(project)
                .createFileFromText("${getName(name)}.dart", DartFileType.INSTANCE, text)
            directory?.add(file)
        }
    }

    private fun getName(name: String): String {
        val matcher = Pattern.compile("[A-Z]([a-z0-9_]+)").matcher(name)
        var ret = ""
        while (matcher.find()) {
            var word = matcher.group(0)
            if (0 == word.indexOf("_")) {
                word = word.substring(1)
            }
            if (word.length - 1 == word.indexOf("_")) {
                word = word.substring(0, word.length - 1)
            }
            ret += "_" + word.lowercase(Locale.getDefault())
        }
        return if (ret.isEmpty()) name.lowercase(Locale.getDefault()) else ret.substring(1)
    }
}
