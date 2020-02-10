package com.exgou.heqain.dart.helper.utils

import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.search.PsiSearchHelper
import com.jetbrains.lang.dart.psi.DartClass
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartReferenceExpression
import java.awt.Toolkit
import javax.swing.JDialog

object UiUtils {
    fun setJDialogToCenter(dialog: JDialog) {
        val screenSize = Toolkit.getDefaultToolkit().screenSize //获取屏幕的尺寸
        val screenWidth = screenSize.width //获取屏幕的宽
        val screenHeight = screenSize.height //获取屏幕的高
        dialog.setLocation(screenWidth / 2 - dialog.width / 2, screenHeight / 2 - dialog.height / 2)//设置窗口居中显示
    }

    fun isDartEnum(expression: DartReferenceExpression): Boolean {
        var search = ServiceManager.getService(expression.project, PsiSearchHelper::class.java) as PsiSearchHelper
        var files = search.findFilesWithPlainTextWords(expression?.text!!);
        for (file in files) {
            if (file is DartFile) {
                var children = file.children;
                for (child in children) {
                    if (child is DartClass && child.isEnum) {
                        return true;
                    }
                }
            };
        };
        return false;
    }
}
