package com.exgou.heqain.dart.helper.utils

import com.intellij.codeInsight.CodeInsightBundle
import com.intellij.codeInsight.TargetElementUtil
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.featureStatistics.FeatureUsageTracker
import com.intellij.find.actions.ShowUsagesAction
import com.intellij.lang.LanguageNamesValidation
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Computable
import com.intellij.psi.*
import com.intellij.psi.search.PsiElementProcessor
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartEnumDefinition
import com.jetbrains.lang.dart.psi.DartType
import org.jetbrains.annotations.Nls
import java.awt.Toolkit
import javax.swing.JDialog

object UiUtils {
    fun setJDialogToCenter(dialog: JDialog) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize //获取屏幕的尺寸
        val screenWidth = screenSize.width //获取屏幕的宽
        val screenHeight = screenSize.height //获取屏幕的高
        dialog.setLocation(screenWidth / 2 - dialog.width / 2, screenHeight / 2 - dialog.height / 2)//设置窗口居中显示
    }

    fun isDartEnum(fieldType: DartType, editor: Editor?): Boolean {
        var project = editor?.project!!
        val offset: Int = fieldType.textOffset
        val elements = GotoDeclarationAction.findAllTargetElements(project, editor, offset);
        val element = if (elements.isEmpty()) null else elements[0];
        if (null == element || null == element.parent) {
            return false;
        }
        return element.parent is DartEnumDefinition;
    }


    fun getPsiComment(field: DartComponent): MutableList<PsiComment> {
        var comments = mutableListOf<PsiComment>();
        var element: PsiElement? = field;
        while (null != element && null == element.prevSibling) {
            element = element.parent;
        }
        if (null != element) {
            element = element.prevSibling;
        }
        while (null != element && (element is PsiWhiteSpace || element is PsiComment)) {
            if (element is PsiComment) {
                comments.add(element)
            }
            element = element.prevSibling;
        }
        return comments;
    }

    fun getJsonName(field: DartComponent): String? {
        val text = "//JsonName:"
        for (item in UiUtils.getPsiComment(field)) {
            if (null == item.text) {
                continue;
            }
            val index = item.text.indexOf(text)
            if (-1 != index) {
                return item.text.substring(index + text.length);
            }
        }
        return null;
    }
}
