/*=========================================================================
 
Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/OutputLabelCustomizer.java,v $
Date:      $Date: 2003/09/28 17:52:41 $
Version:   $Revision: 2.3 $
 
 
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

/*
 * OutputLabelCustomizer.java
 *
 * Created on November 27, 2000, 7:29 PM
 */

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

import vtk.*;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


/**
 *
 * @author  oleg
 * @version
 */

public class OutputLabelCustomizer extends JDialog implements CoreSource
{
    private OutputLabel bean;
    
    /** Creates new OutputLabelCustomizer */
    public OutputLabelCustomizer(OutputLabel outputLabel, JFrame owner) {
        super(owner);
        this.bean = outputLabel;
        setTitle("Caption");
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bean.setCustomizerVisible(false);
            }
        });
        
        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        
        //JPanel hPanel = new JPanel();
        //hPanel.setLayout(new BorderLayout(10,10));
        
        //hPanel.add(new OVTFontEditorPanel(bean.getFont()), BorderLayout.WEST);
        cont.add(new OVTFontEditorPanel(bean.getFont()));
        
        JPanel panel = new JPanel();
        //panel.setBorder(new TitledBorder(new EtchedBorder(), "Justification"));
        panel.setBorder(new EtchedBorder());
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        addPropertyEditor(panel, "justification");
        //hPanel.add(panel, BorderLayout.CENTER);
        cont.add(panel);
        //cont.add(hPanel);
        
        panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Position"));
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        addPropertyEditor(panel, "x");
        addPropertyEditor(panel, "y");
        cont.add(panel);
        
        cont.add(new JScrollPane(getComponent("labelText")));
        
        // ------------------- close button -------------------
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton button = new JButton("Reset text");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bean.setLabelText(bean.getDefaultLabel());
            }
        });
        panel.add(button);

        button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bean.setCustomizerVisible(false);
            }
        });
        panel.add(button);

        cont.add(panel);

        pack();
        setResizable(false);
        Point xyz_loc = getCore().getXYZWin().getLocation();
        setLocation(xyz_loc.x+16, xyz_loc.y+16);
    }
    
    private JComponent getComponent(String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        return (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
    }
    
    private JPanel propertyEditor(String propertyName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10,10));
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        JComponent propeptyEditField = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        panel.add(new JLabel(desc.getLabel()), BorderLayout.WEST);
        panel.add(propeptyEditField, BorderLayout.CENTER);
        return panel;
    }

    private void addPropertyEditor(JPanel panel, String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        JComponent c = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        panel.add(new JLabel(desc.getLabel()));
        panel.add(c);
    }
    
    public OVTCore getCore() {
        return bean.getCore();
    }
    
}
