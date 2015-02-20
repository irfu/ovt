/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/LabelsModuleCustomizer.java,v $
Date:      $Date: 2009/10/27 12:14:36 $
Version:   $Revision: 2.4 $


Copyright (c) 2000-2003 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
Yuri Khotyaintsev)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification is permitted provided that the following conditions are met:

 * No part of the software can be included in any commercial package without
written consent from the OVT team.

 * Redistributions of the source or binary code must retain the above
copyright notice, this list of conditions and the following disclaimer.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS
IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT OR
INDIRECT DAMAGES  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

package ovt.gui;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 *
 * @author  ko
 * @version 
 */
public class LabelsModuleCustomizer extends JFrame {
    
    protected LabelsModule bean;
    private Descriptors desc;
    
    public LabelsModuleCustomizer(LabelsModule labelsModule) {
        super();
        this.bean = labelsModule;
        
        setTitle(labelsModule.getSat().getName() + "'s labels properties");
        desc = labelsModule.getDescriptors();
        
        JComboBox gapField = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("gap").getPropertyEditor())).getComponent();
        
        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));       
        
        //cont.add(new OVTFontEditorPanel(bean.getFont()));

        cont.add(propertyEditor("gap"));
        addPropertyEditor(cont, "scale");

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bean.setCustomizerVisible(false);
            }
        });
        panel.add(closeButton);
        cont.add(panel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bean.setCustomizerVisible(false);
            }
        });
        
        pack();
        setResizable(false);
    }
    
    private void addPropertyEditor(Container cont, String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        Component propeptyEditField = ((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();

        JPanel panel = new JPanel(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(new JLabel(desc.getDisplayName()));
        cont.add(panel);

        panel = new JPanel(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));        
        panel.add(propeptyEditField);
        cont.add(panel);
    }
    
    private JPanel propertyEditor(String propertyName) {
        JPanel panel = new JPanel(false);   // doublebuffered = false
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        panel.add(new JLabel(desc.getDisplayName()));
        JComponent propeptyEditField = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        propeptyEditField.setPreferredSize(new Dimension(100, propeptyEditField.getPreferredSize().height));
        panel.add(propeptyEditField);
        return panel;
    }

}
