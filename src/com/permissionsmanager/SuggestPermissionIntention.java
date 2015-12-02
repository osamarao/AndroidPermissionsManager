package com.permissionsmanager;

import com.google.gson.Gson;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.permissionsmanager.graphicalinterface.MainWindow;
import com.permissionsmanager.graphicalinterface.PermissionRadioButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by Osama Rao on 17-Nov-15.
 */
public class SuggestPermissionIntention implements IntentionAction {
    @Nls
    @NotNull
    @Override
    public String getText() {
        return "Analyze type and suggest a permission";
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
            PermissionMap[] permissionsMaps = null;

            String fullyQualifiedType = ((PsiJavaCodeReferenceElement) ((PsiIdentifier) referenceAt).getContext()).getQualifiedName();
            // Serialize json
            try {
                Scanner scanner = new Scanner(getClass().getResourceAsStream("mapping.json"));
                while (scanner.hasNextLine()) {
                    permissionsMaps = new Gson().fromJson(scanner.nextLine(), PermissionMap[].class);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                editor.getDocument().insertString(editor.getCaretModel().getOffset(), e.getLocalizedMessage());
            }


            // Match Api to Permission
            ArrayList<PermissionMap> matchingPermissions = new ArrayList<>();
            for (PermissionMap permissionMap : permissionsMaps) {
                if (permissionMap.getApi().contains(fullyQualifiedType)) {
                    matchingPermissions.add(permissionMap);
                }
            }

            // Sort by confidence
            /*
                a negative integer, zero, or a positive integer as the first argument is
                less than, equal to, or greater than the second.
             */
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

            // List matching permissions
            JFrame mDialog = new JFrame("App Name");
            mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
           // mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());

            MainWindow mainWindow = new MainWindow(matchingPermissions, e -> {
                PermissionRadioButton permissionButton = (PermissionRadioButton) e.getSource();
                Messages.showMessageDialog(project, permissionButton.getPermissionMap().getXMLPermissionTag(), "Message", Messages.getInformationIcon());
            });
            mDialog.getContentPane().add(mainWindow);
            mDialog.pack();
            mDialog.setLocationRelativeTo(null);
            mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
            mDialog.setVisible(true);

            // In GUI


            PsiFile[] files = Utils.findLayoutResourceByNames(psiFile, project, "AndroidManifest");
//            StringBuilder stringBuilder = new StringBuilder();
//            for (PsiFile file : files) {
//                stringBuilder.append("File: " + file.getContainingDirectory().getName());
//            }

            ClipboardOwner clipboardOwner = (clipboard, contents) -> {

            };

            //editor.getDocument().insertString(editor.getCaretModel().getOffset(), stringBuilder.toString());
            // Look for the Android Manifest
            PsiFile androidManifest = Utils.findLayoutResourceByName(psiFile, project, "AndroidManifest");

            // Create XML element
            // <uses-permission android:name="android.permission.INTERNET" />

            String xmlTag = " <uses-permission android:name=\"android.permission." + permissionsMaps[2].getPermission() + "\" />";
            StringSelection stringSelection = new StringSelection(xmlTag);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, clipboardOwner);

            Messages.showMessageDialog(project, "Permission has been copied to clipboard", "Message", Messages.getInformationIcon());

            //JFrame mDialog = new JFrame();


            XmlTag tagFromText = XmlElementFactory.getInstance(project).createTagFromText(xmlTag);
            XmlFile containingFile = (XmlFile) androidManifest.getContainingFile();
            ReadonlyStatusHandler.ensureFilesWritable(project, containingFile.getVirtualFile());

//            containingFile.getRootTag().addSubTag(tagFromText, false);

        });

    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private String getJsonPermissions() {


        return null;
    }
}
