package com.exgou.heqain.dart.helper.news;

import com.exgou.heqain.dart.helper.utils.DartUtils;
import com.intellij.codeInsight.actions.*;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.EditorTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JsonToDartObject extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField objectName;
    private JPanel jsonPanel;
    private EditorTextField editJson;
    private JButton reformatJsonButton;
    private JPanel bottom;
    private JButton buttonEscape;
    private Project mProject;
    private OnSave onSave;


    public JsonToDartObject(Project project) {
        this.mProject = project;
        setContentPane(contentPane);
        setMinimumSize(new Dimension(900, 800));
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        reformatJsonButton.addActionListener(e -> reFormat(editJson.getEditor()));
        buttonEscape.addActionListener(this::onButtonEscape);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onButtonEscape(ActionEvent actionEvent) {
        String value = editJson.getText().replaceAll("\\\\\\\\", "\\\\").replaceAll("\\\\\"", "\\\"");
        editJson.setText(value);
    }

    private void onCancel() {
        dispose();
    }

    public static void main(Project project, OnSave onSave) {
        JsonToDartObject dialog = new JsonToDartObject(project);
        dialog.onSave = onSave;
        dialog.pack();
        DartUtils.INSTANCE.setJDialogToCenter(dialog);
        dialog.setVisible(true);
    }

    private void createUIComponents() {
        PsiFile psiFile = PsiFileFactory.getInstance(mProject).createFileFromText(JsonLanguage.INSTANCE, "");
        editJson = new EditorTextField(PsiDocumentManager.getInstance(mProject).getDocument(psiFile), mProject, JsonFileType.INSTANCE);
        editJson.setOneLineMode(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setEditor((EditorEx) editJson.getEditor());
    }

    private void setEditor(EditorEx editor) {
        if (null != editor) {
            editor.setEmbeddedIntoDialogWrapper(true);
            final EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            settings.setFoldingOutlineShown(true);
            settings.setRightMarginShown(true);
            settings.setLineMarkerAreaShown(true);
            settings.setIndentGuidesShown(true);
            editor.setHorizontalScrollbarVisible(true);
            editor.setVerticalScrollbarVisible(true);
            editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
            editor.setContextMenuGroupId("EditorPopupMenu");
        }
    }

    private void reFormat(Editor editor) {
        PsiFile file = PsiDocumentManager.getInstance(mProject).getPsiFile(editor.getDocument());
        LastRunReformatCodeOptionsProvider provider = new LastRunReformatCodeOptionsProvider(PropertiesComponent.getInstance());
        ReformatCodeRunOptions currentRunOptions = provider.getLastRunOptions(file);
        TextRangeType processingScope = currentRunOptions.getTextRangeType();
        if (editor.getSelectionModel().hasSelection()) {
            processingScope = TextRangeType.SELECTED_TEXT;
        } else if (processingScope == TextRangeType.VCS_CHANGED_TEXT) {
            if (FormatChangedTextUtil.getInstance().isChangeNotTrackedForFile(mProject, file)) {
                processingScope = TextRangeType.WHOLE_FILE;
            }
        } else {
            processingScope = TextRangeType.WHOLE_FILE;
        }

        currentRunOptions.setProcessingScope(processingScope);
        (new FileInEditorProcessor(file, editor, currentRunOptions)).processCode();
    }


    private void onOK() {
        if (objectName.getText().isEmpty()) {
            return;
        }

        JsonToDartFix fix = new JsonToDartFix(mProject);
        fix.toDart(editJson.getEditor().getDocument(), objectName.getText());
        onSave.onSave(objectName.getText(), fix.toString());
        onCancel();
    }

    public interface OnSave {
        void onSave(String name, String text);
    }
}
