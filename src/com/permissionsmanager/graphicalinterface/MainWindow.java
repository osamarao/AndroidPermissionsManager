package com.permissionsmanager.graphicalinterface;

import com.permissionsmanager.PermissionMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Osama Rao on 02-Dec-15.
 */
public class MainWindow extends JPanel{

    private ArrayList<PermissionMap> matchingPermissions;
    private JButton mConfirm;
    private ButtonGroup buttonGroup;
    private OnSelectionMadeListener onSelectionMadeListener;
    private PermissionMap mSelection;

    public MainWindow(ArrayList<PermissionMap> matchingPermissions){
        this.matchingPermissions = matchingPermissions;

        setPreferredSize(new Dimension(640, 360));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setRadioGroup();
        setButtons();

    }

    public void setOnSelectionMadeListener(OnSelectionMadeListener onSelectionMadeListener){
        this.onSelectionMadeListener = onSelectionMadeListener;
    }



    private void setButtons() {
        mConfirm = new JButton();
        mConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSelectionMadeListener.onSelectionMade(mSelection);
                MainWindow.this.setVisible(false);
            }
        });
        mConfirm.setPreferredSize(new Dimension(120, 26));
        mConfirm.setText("Confirm");
        mConfirm.setVisible(true);
        this.add(mConfirm, RIGHT_ALIGNMENT);
    }

    public JButton getConfirmButton(){
        return mConfirm;
    }


    private void setRadioGroup() {
        buttonGroup = new ButtonGroup();
        for (PermissionMap permissionMap : matchingPermissions){
            String label = String.format("Suggested Permission: %s, Confidence: %s", permissionMap.getPermission(), permissionMap.getConfidence().toString());
            PermissionRadioButton permissionRadioButton = new PermissionRadioButton(label, permissionMap);
            permissionRadioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mSelection = ((PermissionRadioButton) e.getSource()).getPermissionMap();
                }
            });
            buttonGroup.add(permissionRadioButton);
            this.add(permissionRadioButton);
        }

    }

    public interface OnSelectionMadeListener{
        public void onSelectionMade(PermissionMap permissionMap);
    }
}
