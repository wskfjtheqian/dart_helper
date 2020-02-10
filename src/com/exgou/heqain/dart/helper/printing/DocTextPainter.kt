//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.exgou.heqain.dart.helper.printing

import com.intellij.application.options.CodeStyle
import com.intellij.codeEditor.printing.FileSeparatorProvider
import com.intellij.codeEditor.printing.HtmlStyleManager
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.ide.highlighter.HighlighterFactory
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.ui.ColorUtil
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import org.jetbrains.annotations.NonNls
import java.awt.Color

import java.io.File
import java.io.IOException
import java.io.Writer
import java.util.*

class DocTextPainter(private val myPsiFile: PsiFile?, private val myProject: Project, private val htmlStyleManager: HtmlStyleManager, private val myPrintLineNumbers: Boolean, useMethodSeparators: Boolean) {
    private var myOffset: Int = 0
    private val myHighlighter: EditorHighlighter
    private val myText: String
    private val myFileName: String
    private var mySegmentEnd: Int = 0
    private val myDocument: Document?
    private var lineCount: Int = 0
    private var myFirstLineNumber: Int = 0
    private var myColumn: Int = 0
    private val myMethodSeparators: MutableList<LineMarkerInfo<PsiElement>>
    private var myCurrentMethodSeparator: Int = 0

    val psiFile: PsiFile
        get() {
            val var10000 = this.myPsiFile
            if (this.myPsiFile == null) {
                isNull(5)
            }

            return var10000!!
        }

    constructor(psiFile: PsiFile, project: Project, printLineNumbers: Boolean) : this(psiFile, project, HtmlStyleManager(false), printLineNumbers, true) {}

    private fun isNull(i: Int) {

    }

    init {
        if (myPsiFile == null) {
            isNull(2)
        }

        if (myProject == null) {
            isNull(3)
        }

        if (htmlStyleManager == null) {
            isNull(4)
        }

        this.myOffset = 0
        this.myMethodSeparators = ArrayList()
        this.myHighlighter = HighlighterFactory.createHighlighter(myProject, myPsiFile!!.virtualFile)
        this.myText = myPsiFile.text
        this.myHighlighter.setText(this.myText)
        this.mySegmentEnd = this.myText.length
        this.myFileName = myPsiFile.getVirtualFile().presentableUrl
        this.myDocument = PsiDocumentManager.getInstance(myProject).getDocument(myPsiFile)
        if (useMethodSeparators && this.myDocument != null) {
            this.myMethodSeparators.addAll(FileSeparatorProvider.getFileSeparators(myPsiFile, this.myDocument))
        }

        this.myCurrentMethodSeparator = 0
    }

    fun setSegment(segmentStart: Int, segmentEnd: Int, firstLineNumber: Int) {
        this.myOffset = segmentStart
        this.mySegmentEnd = segmentEnd
        this.myFirstLineNumber = firstLineNumber
    }

    @Throws(IOException::class)
    fun paint(refMap: TreeMap<*, *>?, writer: Writer, isStandalone: Boolean) {
        if (writer == null) {
            isNull(6)
        }

        val hIterator = this.myHighlighter.createIterator(this.myOffset)
        if (!hIterator.atEnd()) {
            this.lineCount = this.myFirstLineNumber
            var prevAttributes: TextAttributes? = null
            var refKeys: Iterator<*>? = null
            var refOffset = -1
            var ref: PsiReference? = null
            if (refMap != null) {
                refKeys = refMap.keys.iterator()
                if (refKeys.hasNext()) {
                    val key = refKeys.next() as Int
                    ref = refMap[key] as PsiReference
                    refOffset = key
                }
            }

            var referenceEnd = -1
            if (isStandalone) {
                this.writeHeader(writer, if (isStandalone) File(this.myFileName).name else null)
            } else {
                this.ensureStyles()
            }

            writer.write("<pre>")
            if (this.myFirstLineNumber == 0) {
                this.writeLineNumber(writer)
            }

            var closeTag: String? = null
            this.getMethodSeparator(hIterator.start)

            while (!hIterator.atEnd()) {
                var hStart = hIterator.start
                val hEnd = hIterator.end
                if (hEnd > this.mySegmentEnd) {
                    break
                }

                while (hStart < hEnd) {
                    val c = this.myText[hStart]
                    if (!Character.isWhitespace(c)) {
                        break
                    }

                    if (closeTag != null && c == '\n') {
                        writer.write(closeTag)
                        closeTag = null
                    }

                    writer.write(c.toInt())
                    if (c == '\n') {
                        writer.write(" ")
                        this.writeLineNumber(writer)
                    }
                    ++hStart
                }

                if (hStart == hEnd) {
                    hIterator.advance()
                } else {
                    if (refOffset > 0 && hStart <= refOffset && hEnd > refOffset) {
                        referenceEnd = this.writeReferenceTag(writer, ref!!)
                    }

                    var textAttributes: TextAttributes? = hIterator.textAttributes
                    if (this.htmlStyleManager.isDefaultAttributes(textAttributes!!)) {
                        textAttributes = null
                    }

                    if (!equals(prevAttributes, textAttributes) && referenceEnd < 0) {
                        if (closeTag != null) {
                            writer.write(closeTag)
                            closeTag = null
                        }

                        if (textAttributes != null) {
                            this.htmlStyleManager.writeTextStyle(writer, textAttributes)
                            closeTag = "</span>"
                        }

                        prevAttributes = textAttributes
                    }

                    this.writeString(writer, this.myText, hStart, hEnd - hStart, this.myPsiFile!!)
                    if (referenceEnd > 0 && hEnd >= referenceEnd) {
                        writer.write("</a>")
                        referenceEnd = -1
                        if (refKeys!!.hasNext()) {
                            val key = refKeys.next() as Int
                            ref = refMap!![key] as PsiReference
                            refOffset = key
                        }
                    }

                    hIterator.advance()
                }
            }

            if (closeTag != null) {
                writer.write(closeTag)
            }

            writer.write("</pre>\n")
            if (isStandalone) {
                writer.write("</body>\n")
                writer.write("</html>")
            }

        }
    }

    protected fun ensureStyles() {
        this.htmlStyleManager.ensureStyles(this.myHighlighter.createIterator(this.myOffset), this.myMethodSeparators)
    }

    private fun getMethodSeparator(offset: Int): LineMarkerInfo<*>? {
        if (this.myDocument == null) {
            return null
        } else {
            val line = this.myDocument.getLineNumber(Math.max(0, Math.min(this.myDocument.textLength, offset)))

            var marker: LineMarkerInfo<*>?
            var tmpMarker: LineMarkerInfo<*>
            marker = null
            while (this.myCurrentMethodSeparator < this.myMethodSeparators.size) {
                tmpMarker = this.myMethodSeparators[this.myCurrentMethodSeparator]
                if (tmpMarker == null || FileSeparatorProvider.getDisplayLine(tmpMarker, this.myDocument) > line) {
                    break
                }

                marker = tmpMarker
                ++this.myCurrentMethodSeparator
            }

            return marker
        }
    }

    @Throws(IOException::class)
    private fun writeReferenceTag(writer: Writer, ref: PsiReference): Int {
        val refFile = (Objects.requireNonNull<PsiElement>(ref.resolve()) as PsiElement).containingFile
        val psiDirectoryFactory = PsiDirectoryFactory.getInstance(this.myProject)
        val refPackageName = psiDirectoryFactory.getQualifiedName(refFile.containingDirectory, false)
        val psiPackageName = psiDirectoryFactory.getQualifiedName(this.myPsiFile!!.containingDirectory, false)
        val fileName = StringBuilder()
        if (psiPackageName != refPackageName) {
            val tokens = StringTokenizer(psiPackageName, ".")

            while (tokens.hasMoreTokens()) {
                tokens.nextToken()
                fileName.append("../")
            }

            val refTokens = StringTokenizer(refPackageName, ".")

            while (refTokens.hasMoreTokens()) {
                val token = refTokens.nextToken()
                fileName.append(token)
                fileName.append('/')
            }
        }

        fileName.append(ExportToDocManager.getHTMLFileName(refFile))
        writer.write("<a href=\"$fileName\">")
        return ref.element.textRange.endOffset
    }

    @Throws(IOException::class)
    private fun writeString(writer: Writer, charArray: CharSequence, start: Int, length: Int, psiFile: PsiFile) {
        if (psiFile == null) {
            isNull(7)
        }

        var i = start
        while (i < start + length) {
            val c = charArray[i]
            if (c == '<') {
                this.writeChar(writer, "&lt;")
            } else if (c == '>') {
                this.writeChar(writer, "&gt;")
            } else if (c == '&') {
                this.writeChar(writer, "&amp;")
            } else if (c == '"') {
                this.writeChar(writer, "&quot;")
            } else if (c == '\t') {
                var tabSize = CodeStyle.getIndentOptions(psiFile).TAB_SIZE
                if (tabSize <= 0) {
                    tabSize = 1
                }

                val nSpaces = tabSize - this.myColumn % tabSize

                for (j in 0 until nSpaces) {
                    this.writeChar(writer, " ")
                }
            } else if (c != '\n' && c != '\r') {
                this.writeChar(writer, c.toString())
            } else {
                if (c == '\r' && i + 1 < start + length && charArray[i + 1] == '\n') {
                    ++i
                } else if (c == '\n') {
                    this.writeChar(writer, " ")
                }

                val marker = this.getMethodSeparator(i + 1)
                if (marker == null) {
                    writer.write(10)
                } else {
                    writer.write("<hr class=\"" + this.htmlStyleManager.getSeparatorClassName(marker.separatorColor) + "\">")
                }

                this.writeLineNumber(writer)
            }
            ++i
        }

    }

    @Throws(IOException::class)
    private fun writeChar(writer: Writer, s: String) {
        writer.write(s)
        ++this.myColumn
    }

    @Throws(IOException::class)
    private fun writeLineNumber(@NonNls writer: Writer) {
        this.myColumn = 0
        ++this.lineCount
        if (this.myPrintLineNumbers) {
            writer.write("<a name=\"l" + this.lineCount + "\">")
            writer.write("<span class=\"ln\">")
            val s = Integer.toString(this.lineCount)
            writer.write(s)
            var var3 = 4 - s.length

            do {
                writer.write(32)
            } while (var3-- > 0)

            writer.write("</span></a>")
        }

    }

    @Throws(IOException::class)
    private fun writeHeader(@NonNls writer: Writer, title: String?) {
        writer.write("<html>\n")
        writer.write("<head>\n")
        writer.write("<title>$title</title>\n")
        writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n")
        this.ensureStyles()
        this.htmlStyleManager.writeStyleTag(writer, this.myPrintLineNumbers)
        writer.write("</head>\n")
        val scheme = EditorColorsManager.getInstance().globalScheme
        writer.write("<body bgcolor=\"" + ColorUtil.toHtmlColor(scheme.defaultBackground) + "\">\n")
        writer.write("<table CELLSPACING=0 CELLPADDING=5 COLS=1 WIDTH=\"100%\" BGCOLOR=\"#" + ColorUtil.toHex(JBColor(Gray.xC0, Gray.x60)) + "\" >\n")
        writer.write("<tr><td><center>\n")
        writer.write("<font face=\"Arial, Helvetica\" color=\"#000000\">\n")
        writer.write(title!! + "</font>\n")
        writer.write("</center></td></tr></table>\n")
    }

    private fun equals(attributes1: TextAttributes?, attributes2: TextAttributes?): Boolean {
        return if (attributes2 == null) {
            attributes1 == null
        } else if (attributes1 == null) {
            false
        } else if (!Comparing.equal(attributes1.foregroundColor, attributes2.foregroundColor)) {
            false
        } else if (attributes1.fontType != attributes2.fontType) {
            false
        } else if (!Comparing.equal(attributes1.backgroundColor, attributes2.backgroundColor)) {
            false
        } else {
            Comparing.equal(attributes1.effectColor, attributes2.effectColor)
        }
    }
}
