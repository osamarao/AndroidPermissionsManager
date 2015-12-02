package com.permissionsmanager.graphicalinterface;

import com.permissionsmanager.PermissionMap;

import javax.swing.*;

/**
 * Created by Osama Rao on 02-Dec-15.
 */
public class PermissionRadioButton extends JRadioButton {

    private PermissionMap permissionMap;

    public PermissionRadioButton(String label, PermissionMap permissionMap){
        super(label);

        this.permissionMap = permissionMap;
    }


    public PermissionMap getPermissionMap() {
        return permissionMap;
    }
}
