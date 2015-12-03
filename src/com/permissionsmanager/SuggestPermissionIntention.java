package com.permissionsmanager;

import com.google.gson.Gson;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.util.IncorrectOperationException;
import com.permissionsmanager.graphicalinterface.MainWindow;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by Osama Rao on 17-Nov-15.
 */
public class SuggestPermissionIntention implements IntentionAction {
    private PermissionMap[] permissionsMap;

    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Analyze Type and suggest permission(s)";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        // If the element at the caret is a Type then show the item
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        try {
            PsiJavaCodeReferenceElement context = (PsiJavaCodeReferenceElement) ((PsiIdentifier) referenceAt).getContext();
            return true;
        } catch (ClassCastException e) {
            return false;
        } catch (NullPointerException ne) {
            return false;
        }
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());

        WriteCommandAction.runWriteCommandAction(project, () -> {
            permissionsMap = getJsonPermissions(editor);
            ArrayList<PermissionMap> matchingPermissions = getMatchingPermissions((PsiIdentifier) referenceAt);
            sortByConfidence(matchingPermissions);
            prepareAndShowWindow(matchingPermissions, project);
        });

    }

    @NotNull
    private ArrayList<PermissionMap> getMatchingPermissions(PsiIdentifier referenceAt) {
        ArrayList<PermissionMap> matchingPermissions = new ArrayList<>();
        for (PermissionMap permissionMap : permissionsMap) {
            if (isContainsApi(getFullyQualifiedType(referenceAt), permissionMap)) {
                matchingPermissions.add(permissionMap);
            }
        }
        return matchingPermissions;
    }

    // XXX Is this operation costly?
    private String getFullyQualifiedType(PsiIdentifier referenceAt) {
        return ((PsiJavaCodeReferenceElement) referenceAt.getContext()).getQualifiedName();
    }

    private void copyPermissionToClipboard(String xmlTag) {
        StringSelection stringSelection = new StringSelection(xmlTag);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, (clipboard1, contents) -> {
        });
    }

    private void prepareAndShowWindow(ArrayList<PermissionMap> matchingPermissions, @NotNull Project project) {
        JFrame mDialog = new JFrame("");
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        MainWindow mainWindow = new MainWindow(matchingPermissions);
        mainWindow.setOnSelectionMadeListener(permissionMap -> {

            copyPermissionToClipboard(permissionMap.getXMLPermissionTag());
            Messages.showMessageDialog(project, "Permission has been copied to clipboard", "Message", Messages.getInformationIcon());
            mDialog.setVisible(false);
        });

        mDialog.getContentPane().add(mainWindow);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    private void sortByConfidence(ArrayList<PermissionMap> matchingPermissions) {
        Collections.sort(matchingPermissions, (first, second) -> {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;
            if (first.getConfidence() < second.getConfidence())
                return AFTER;
            else if (first.getConfidence().equals(second.getConfidence()))
                return EQUAL;
            else
                return BEFORE;
        });
    }

    private boolean isContainsApi(String fullyQualifiedType, PermissionMap permissionMap) {
        return permissionMap.getApi().contains(fullyQualifiedType);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private PermissionMap[] getJsonPermissions(Editor editor) {
        // Serialize json
        PermissionMap[] permissionsMaps = null;
        try {
            Scanner scanner = new Scanner(SuggestPermissionIntention.this.getClass().getResourceAsStream("mapping.json"));
            while (scanner.hasNextLine()) {
                permissionsMaps = new Gson().fromJson(scanner.nextLine(), PermissionMap[].class);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            editor.getDocument().insertString(editor.getCaretModel().getOffset(), e.getLocalizedMessage());
            return null;
        }
        return permissionsMaps;
    }
}
