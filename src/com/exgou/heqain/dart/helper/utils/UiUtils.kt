package com.exgou.heqain.dart.helper.utils

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.PsiSearchHelper
import com.jetbrains.lang.dart.psi.*
import java.awt.Toolkit
import javax.swing.JDialog

object UiUtils {
    fun setJDialogToCenter(dialog: JDialog) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize //获取屏幕的尺寸
        val screenWidth = screenSize.width //获取屏幕的宽
        val screenHeight = screenSize.height //获取屏幕的高
        dialog.setLocation(screenWidth / 2 - dialog.width / 2, screenHeight / 2 - dialog.height / 2)//设置窗口居中显示
    }

    fun isDartEnum(fieldType: DartType): Boolean {
        var clazz = fieldType.resolveReference()?.parent;
        if (null != clazz && clazz is DartClass && clazz.isEnum) {
            return true;
        }
        return false;
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
