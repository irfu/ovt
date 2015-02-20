/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/GroundStationCustomizer.java,v $
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
 * GroundStationCustomizer.java
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
import javax.swing.event.*;


/**
 *
 * @author  oleg
 * @version
 */

public class GroundStationCustomizer extends JFrame
implements CoreSource
{
    private GroundStation groundStation;
    
    /** Creates new GroundStationCustomizer */
    public GroundStationCustomizer(GroundStation gStation) {
        super();

        this.groundStation = gStation;
        setTitle("Ground station properties");
        
        addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) 
          {
//DBG*/     System.out.println("GroundStationCustomizer: Window closing");
            groundStation.setCustomizerVisible(false);
          }
        });
        
        Container cont = getContentPane();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        
        cont.add(propertyEditor("name"));
        cont.add(propertyEditor("type"));
        cont.add(propertyEditor("latitude"));
        cont.add(propertyEditor("longitude"));
        
        // ------------------- close button -------------------
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton button = new JButton("    OK    ");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                groundStation.setCustomizerVisible(false);
            }
        });
        panel.add(button);
        cont.add(panel);
        
        pack();
        setResizable(false);
        Point xyz_loc = getCore().getXYZWin().getLocation();
        setLocation(xyz_loc.x+16, xyz_loc.y+16);
    }
    
    private JPanel propertyEditor(String propertyName) {
        JPanel panel = new JPanel(false);   // doublebuffered = false
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        BasicPropertyDescriptor desc = groundStation.getDescriptors().getDescriptor(propertyName);
        panel.add(new JLabel(desc.getDisplayName()));
        JComponent propeptyEditField = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        propeptyEditField.setPreferredSize(new Dimension(100, propeptyEditField.getPreferredSize().height));
        panel.add(propeptyEditField);
        return panel;
    }

    public OVTCore getCore() {
        return groundStation.getCore();
    }
}
