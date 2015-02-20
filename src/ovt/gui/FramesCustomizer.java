/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/FramesCustomizer.java,v $
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
import ovt.beans.*;
import ovt.object.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;

/**
 *
 * @author  oleg
 * @version
 */
public class FramesCustomizer extends JFrame implements PropertyChangeListener {
    
    private Frames bean;
    private JLabel yozPosLabel = null;
    
    /** Creates new FramesCustomizer */
    public FramesCustomizer(Frames frames) {
        super();
        this.bean = frames;
        setTitle("Frames properties");
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bean.setCustomizerVisible(false);
            }
        });
        
        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        
        addPropertyEditor(cont, "cellsNumber");
        addPropertyEditor(cont, "cellSize");
        
        yozPosLabel = new JLabel();
        updateYozPosLabel();
        cont.add(panelWrap(yozPosLabel));
        cont.add(panelWrap(getComponent("YOZPosition")));
        
        JButton button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                bean.setCustomizerVisible(false);
            }
        });
        cont.add(panelWrap(button));

        pack();
        setResizable(false);
        Point xyz_loc = getCore().getXYZWin().getLocation();
        setLocation(xyz_loc.x+16, xyz_loc.y+16);
    }
    
    private void updateYozPosLabel() {
        yozPosLabel.setText(" YOZ position = " + bean.getCellSize()*bean.getYOZPosition() + " Re ");
    }
    
    private Component panelWrap(Component c) {
        JPanel panel = new JPanel(false);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(c);
        return panel;
    }
    
    private void addPropertyEditor(Container cont, String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        Component propeptyEditField = ((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        cont.add(panelWrap(new JLabel(desc.getDisplayName())));
        cont.add(panelWrap(propeptyEditField));
    }
    
    private JComponent getComponent(String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        return (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
    }

    public OVTCore getCore() {
        return bean.getCore();
    }
    
   public void propertyChange(PropertyChangeEvent evt) {
       updateYozPosLabel();
   }
}
