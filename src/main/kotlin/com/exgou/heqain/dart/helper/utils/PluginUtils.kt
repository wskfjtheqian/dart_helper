package com.exgou.heqain.dart.helper.utils

import com.intellij.ide.actions.CreateDirectoryOrPackageHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.xml.XmlDocument


object PluginUtils {
    /**
     * 获取Java文件的Class类对象
     */
    fun getFileClass(file: PsiFile): PsiClass? {
        for (psiElement in file.children) {
            if (psiElement is PsiClass) {
                return psiElement
            }
        }
        return null
    }

    fun getPsiFile(clazz: PsiElement): PsiFile? {
        var parent = clazz
        while (null != parent && !(parent is PsiFile)) {
            parent = parent.parent
        }
        return (parent ?: null) as PsiFile
    }

    fun getDirectory(project: Project, path: String?): PsiDirectory? {
        val psiDirectory = PsiManager.getInstance(project).findDirectory(project.baseDir)
        if (null == path || 0 == path.toLowerCase().length) {
            return psiDirectory
        }
        var path = path
        path = path.replace('\\', '/')
        val handler = CreateDirectoryOrPackageHandler(project, psiDirectory!!, true, "\\/")
        if (handler.checkInput(path)) {
            handler.canClose(path)
        } else {
            var temp: VirtualFile?
            var virtualFile = project.baseDir
            for (item in path.split("/".toRegex()).dropLastWhile { it.isEmpty() }) {
                temp = virtualFile.findChild(item)
                if (null != temp) {
                    virtualFile = temp
                }
            }
            return PsiManager.getInstance(project).findDirectory(virtualFile);
        }
        return PsiManager.getInstance(project).findDirectory(handler.createdElement!!.virtualFile)
    }

    fun getXmlDocument(file: PsiFile): XmlDocument? {
        for (psiElement in file.children) {
            if (psiElement is XmlDocument) {
                return psiElement
            }
        }
        return null

    }
}
