package com.exgou.heqain.dart.helper.printing

import com.intellij.CommonBundle
import com.intellij.codeEditor.printing.*
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.ui.ColorUtil
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

object ExportToDocManager {
    private val LOG = Logger.getInstance("#" + ExportToDocManager::class.java.name)
    private var myLastException: FileNotFoundException? = null


    @Throws(FileNotFoundException::class)
    fun executeExport(dataContext: DataContext) {
        var psiDirectory: PsiDirectory? = null
        val psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext)
        if (psiElement is PsiDirectory) {
            psiDirectory = psiElement
        }

        val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext)
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        var shortFileName: String? = null
        var directoryName: String? = null
        if (psiFile != null || psiDirectory != null) {
            if (psiFile != null) {
                shortFileName = psiFile.virtualFile.name
                if (psiDirectory == null) {
                    psiDirectory = psiFile.containingDirectory
                }
            }

            if (psiDirectory != null) {
                directoryName = psiDirectory.virtualFile.presentableUrl
            }
        }

        val editor = CommonDataKeys.EDITOR.getData(dataContext)
        var isSelectedTextEnabled = false
        if (editor != null && editor.selectionModel.hasSelection()) {
            isSelectedTextEnabled = true
        }

        val exportToHTMLDialog = ExportToHTMLDialog(shortFileName, directoryName, isSelectedTextEnabled, project)
        exportToHTMLDialog.title = "Export To Doc"
        val exportToHTMLSettings = ExportToHTMLSettings.getInstance(project!!)
        var outputDirectoryName: String?
        if (exportToHTMLSettings.OUTPUT_DIRECTORY == null) {
            outputDirectoryName = (Objects.requireNonNull(project) as Project).basePath
            if (outputDirectoryName != null) {
                exportToHTMLSettings.OUTPUT_DIRECTORY = "$outputDirectoryName/exportToDoc"
            } else {
                exportToHTMLSettings.OUTPUT_DIRECTORY = ""
            }
        }

        exportToHTMLDialog.reset()
        if (exportToHTMLDialog.showAndGet()) {
            try {
                exportToHTMLDialog.apply()
            } catch (var16: ConfigurationException) {
                Messages.showErrorDialog(project, var16.message, CommonBundle.getErrorTitle())
            }

            PsiDocumentManager.getInstance(project).commitAllDocuments()
            outputDirectoryName = exportToHTMLSettings.OUTPUT_DIRECTORY

            if (exportToHTMLSettings.printScope != 4) {
                if (psiFile == null || psiFile.text == null) {
                    return
                }

                val dirName = constructOutputDirectory(psiFile, outputDirectoryName)
                val textPainter = DocTextPainter(psiFile, project, exportToHTMLSettings.PRINT_LINE_NUMBERS)
                if (exportToHTMLSettings.printScope == 2 && editor != null && editor.selectionModel.hasSelection()) {
                    val firstLine = editor.document.getLineNumber(editor.selectionModel.selectionStart)
                    textPainter.setSegment(editor.selectionModel.selectionStart, editor.selectionModel.selectionEnd, firstLine)
                }

                try {
                    val htmlFile = dirName + "/" + getHTMLFileName(textPainter.psiFile)
                    val writer = OutputStreamWriter(FileOutputStream(htmlFile), StandardCharsets.UTF_8)
                    var var5: Throwable? = null

                    try {
                        textPainter.paint(null, writer, true)
                    } catch (var14: Throwable) {
                        var5 = var14
                        throw var14
                    } finally {
                        if (writer != null) {
                            if (var5 != null) {
                                try {
                                    writer.close()
                                } catch (var13: Throwable) {
                                    var5.addSuppressed(var13)
                                }

                            } else {
                                writer.close()
                            }
                        }

                    }
                    if (exportToHTMLSettings.OPEN_IN_BROWSER) {
                        BrowserUtil.browse(htmlFile)
                    }
                } catch (var15: IOException) {
                    LOG.error(var15)
                }

            } else {
                myLastException = null
                val exportRunnable = ExportRunnable(exportToHTMLSettings, psiDirectory!!, outputDirectoryName, project)
                ProgressManager.getInstance().runProcessWithProgressSynchronously(exportRunnable, CodeEditorBundle.message("export.to.html.title", *arrayOfNulls(0)), true, project)
                if (myLastException != null) {
                    throw myLastException as Nothing
                }
            }

        }
    }


    private fun constructOutputDirectory(psiFile: PsiFile, outputDirectoryName: String): String {
        return constructOutputDirectory(psiFile.containingDirectory, outputDirectoryName)
    }

    private fun constructOutputDirectory(directory: PsiDirectory, outputDirectoryName: String): String {

        val qualifiedName = PsiDirectoryFactory.getInstance(directory.project).getQualifiedName(directory, false)
        var dirName = outputDirectoryName
        if (qualifiedName.isNotEmpty()) {
            dirName = outputDirectoryName + "/" + qualifiedName.replace('.', '/')
        }

        val dir = File(dirName)
        dir.mkdirs()
        return dirName
    }

    @Throws(FileNotFoundException::class)
    private fun addToPsiFileList(psiDirectory: PsiDirectory, filesList: List<PsiFile>, isRecursive: Boolean, outputDirectoryName: String) {
        if (psiDirectory.isValid) {
            val files = psiDirectory.files
            Collections.addAll(filesList as MutableCollection<in PsiFile>, *files)

            if (isRecursive) {
                val directories = psiDirectory.subdirectories
                val var7 = directories.size

                for (var8 in 0 until var7) {
                    val directory = directories[var8]
                    addToPsiFileList(directory, filesList, isRecursive, outputDirectoryName)
                }
            }

        }
    }

    private fun exportPsiFile(psiFile: PsiFile, project: Project, writer: OutputStreamWriter, htmlStyleManager: HtmlStyleManager, filesMap: HashMap<PsiFile, PsiFile>, exportToHTMLSettings: ExportToHTMLSettings): Boolean {

        if (psiFile is PsiBinaryFile) {
            return true
        } else {

            ApplicationManager.getApplication().runReadAction {
                if (psiFile.isValid) {
                    var refMap: TreeMap<Int, PsiReference>? = null
                    val var6 = PrintOption.EP_NAME.extensionList
                    val var7 = var6.size

                    for (var8 in 0 until var7) {
                        val printOption = var6[var8]
                        val map = printOption.collectReferences(psiFile, filesMap)
                        if (map != null) {
                            refMap = TreeMap(map)
                        }
                    }

                    try {
                        val textPainter = DocTextPainter(psiFile, project, htmlStyleManager, exportToHTMLSettings.PRINT_LINE_NUMBERS, true)
                        textPainter.paint(refMap, writer, false)
                    } catch (var11: FileNotFoundException) {
                        myLastException = var11
                    } catch (var12: IOException) {
                        LOG.error(var12)
                    }

                }
            }
            return myLastException == null
        }
    }

    internal fun getHTMLFileName(psiFile: PsiFile): String {
        return psiFile.virtualFile.name + ".doc"
    }

    private class ExportRunnable(private val myExportToHTMLSettings: ExportToHTMLSettings, private val myPsiDirectory: PsiDirectory, private val myOutputDirectoryName: String, private val myProject: Project) : Runnable {

        override fun run() {
            val progressIndicator = ProgressManager.getInstance().progressIndicator
            val filesList = ArrayList<PsiFile>()
            val isRecursive = this.myExportToHTMLSettings.isIncludeSubdirectories
            ApplicationManager.getApplication().runReadAction {
                try {
                    addToPsiFileList(this.myPsiDirectory, filesList, isRecursive, this.myOutputDirectoryName)
                } catch (var4: FileNotFoundException) {
                    myLastException = var4
                }


            }
            if (myLastException == null) {
                val filesMap = HashMap<PsiFile, PsiFile>()
                val var5 = filesList.iterator()

                var psiFile: PsiFile
                while (var5.hasNext()) {
                    psiFile = var5.next()
                    filesMap[psiFile] = psiFile
                }

                val docFile = wTempFile(progressIndicator, filesList, filesMap) ?: return

                if (this.myExportToHTMLSettings.OPEN_IN_BROWSER) {
                    BrowserUtil.browse(docFile)
                }

            }
        }

        private fun wTempFile(progressIndicator: ProgressIndicator, filesList: ArrayList<PsiFile>, filesMap: HashMap<PsiFile, PsiFile>): String? {
            val title = myProject.name
            val htmlFile = "$myOutputDirectoryName/$title.doc"
            val exportToHTMLSettings = ExportToHTMLSettings.getInstance(myProject)

            var psiFile: PsiFile
            var `var`: Throwable? = null
            var writer: OutputStreamWriter? = null
            try {
                val htmlStyleManager = HtmlStyleManager(false)
                writer = OutputStreamWriter(FileOutputStream(htmlFile), StandardCharsets.UTF_8)
                writer.write("<html>\n")
                writer.write("<head>\n")
                writer.write("    <style type=\"text/css\">\n")
                writer.write("        *{\n")
                writer.write("            font-size: 8pt;\n")
                writer.write("        }\n")
                writer.write("    </style>")
                writer.write("<title>$title</title>\n")
                writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n")
                writer.write("</head>\n")
                val scheme = EditorColorsManager.getInstance().globalScheme
                writer.write("<body bgcolor=\"" + ColorUtil.toHtmlColor(scheme.defaultBackground) + "\">\n")


                for (i in filesList.indices) {
                    psiFile = filesList[i]
                    if (progressIndicator.isCanceled) {
                        return null
                    }

                    progressIndicator.text = CodeEditorBundle.message("export.to.html.generating.file.progress", getHTMLFileName(psiFile))
                    progressIndicator.fraction = i.toDouble() / filesList.size.toDouble()


                    writer.write("<p style=\"border-bottom: 1pt #808080 solid;padding-top: 20pt;font-weight: bold;\" >")
                    writer.write(psiFile.name)
                    writer.write("</p>\n")

                    exportPsiFile(psiFile, myProject, writer, htmlStyleManager, filesMap, exportToHTMLSettings)
                }

                htmlStyleManager.writeStyleTag(writer, exportToHTMLSettings.PRINT_LINE_NUMBERS)
                writer.write("</body>\n")
                writer.write("</html>\n")

            } catch (var14: Throwable) {
                `var` = var14
            } finally {
                if (writer != null) {
                    if (`var` != null) {
                        try {
                            writer.close()
                        } catch (var13: Throwable) {
                            `var`.addSuppressed(var13)
                        }

                    } else {
                        try {
                            writer.close()
                        } catch (e: IOException) {
                            LOG.error(e)
                        }

                    }
                }

            }
            return htmlFile
        }
    }
}
