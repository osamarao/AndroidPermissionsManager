package com.permissionsmanager.graphicalinterface;

import com.permissionsmanager.PermissionMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Osama Rao on 02-Dec-15.
 */
public class MainWindow extends JPanel{

    private ArrayList<PermissionMap> matchingPermissions;
    private ActionListener actionListener;

    public MainWindow(ArrayList<PermissionMap> matchingPermissions, ActionListener actionListener){
        this.matchingPermissions = matchingPermissions;
        this.actionListener = actionListener;

        setPreferredSize(new Dimension(640, 360));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setRadioGroup();
    }

    private void setRadioGroup() {
        ButtonGroup buttonGroup = new ButtonGroup();
        for (PermissionMap permissionMap : matchingPermissions){
            String label = String.format("Suggested Permission: %s, Confidence: %s", permissionMap.getPermission(), permissionMap.getConfidence().toString());
            PermissionRadioButton permissionRadioButton = new PermissionRadioButton(label, permissionMap);
            permissionRadioButton.addActionListener(actionListener);
            buttonGroup.add(permissionRadioButton);
            this.add(permissionRadioButton);
        }

    }
}
