package com.exgou.heqain.dart.helper.utils

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartComponent
import com.jetbrains.lang.dart.psi.DartEnumDefinition
import com.jetbrains.lang.dart.psi.DartType
import java.awt.Toolkit
import javax.swing.JDialog

object DartUtils {
    fun setJDialogToCenter(dialog: JDialog) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize //获取屏幕的尺寸
        val screenWidth = screenSize.width //获取屏幕的宽
        val screenHeight = screenSize.height //获取屏幕的高
        dialog.setLocation(screenWidth / 2 - dialog.width / 2, screenHeight / 2 - dialog.height / 2)//设置窗口居中显示
    }

    fun isDartEnum(fieldType: DartType, editor: Editor?): Boolean {
        val project = editor?.project!!
        val offset: Int = fieldType.textOffset
        val elements = GotoDeclarationAction.findAllTargetElements(project, editor, offset)
        val element = if (elements.isEmpty()) null else elements[0]
        if (null == element || null == element.parent) {
            return false;
        }
        return element.parent is DartEnumDefinition;
    }


    fun getPsiComment(psiElement: PsiElement): MutableList<PsiComment> {
        var comments = mutableListOf<PsiComment>();
        var element: PsiElement? = psiElement;
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

    fun getJsonName(psiElement: PsiElement): String? {
        val text = "//JsonName:"
        for (item in getPsiComment(psiElement)) {
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

    fun getRequestUrl(psiElement: PsiElement): String? {
        val text = "//RequestUrl:"
        for (item in getPsiComment(psiElement)) {
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

    fun getFields(editor: Editor, dartClass: DartClass, fields: MutableList<DartComponent>) {
        val superClass = dartClass.superClass
        if (null != superClass) {
            val project = editor.project!!
            val offset: Int = superClass.textOffset
            val elements = GotoDeclarationAction.findAllTargetElements(project, editor, offset)
            for (item in elements) {
                getFields(editor, item.parent as DartClass, fields)
            }
        }
        fields.addAll(dartClass.fields)
    }

    fun isNullPointer(type: DartType?): Boolean {
        if (null == type) {
            return true
        }
        val text = type.text
        return "?" == text.substring(text.length - 1)
    }

}
