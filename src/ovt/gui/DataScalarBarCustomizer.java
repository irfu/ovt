/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/DataScalarBarCustomizer.java,v $
  Date:      $Date: 2003/09/28 17:52:40 $
  Version:   $Revision: 1.3 $


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
 * DataScalarBarCustomizer.java
 *
 * Created on August 7, 2001, 12:14 AM
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
 * @author  root
 * @version 
 */
public class DataScalarBarCustomizer extends JDialog {
    
    private DataScalarBar dataScalarBar;

    /** Creates new DataScalarBarCustomizer */
    public DataScalarBarCustomizer(DataScalarBar dataScalarBar, JFrame owner) {
        super(owner, "Scalar Bar Customizer", false);
        this.dataScalarBar = dataScalarBar;
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // direct method this.setVisible should not be used
                DataScalarBarCustomizer.this.dataScalarBar.setCustomizerVisible(false);
            }
        });
        makeInterior();
    }
    
    private void makeInterior() {
        Container cont = getContentPane();
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Position"));
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        addPropertyEditor(panel, "x");
        addPropertyEditor(panel, "y");
        cont.add(panel, BorderLayout.NORTH);
        
        panel = new JPanel();
        panel.setBorder(new TitledBorder(new EtchedBorder(), "Size"));
        panel.setLayout(new GridLayout(4, 1, 10, 10));
        addPropertyEditor(panel, "width");
        addPropertyEditor(panel, "height");
        cont.add(panel, BorderLayout.CENTER);

        pack();
    }

    private void addPropertyEditor(JPanel panel, String propertyName) {
        BasicPropertyDescriptor desc = dataScalarBar.getDescriptors().getDescriptor(propertyName);
        JComponent c = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        panel.add(new JLabel(desc.getLabel()));
        panel.add(c);
    }
}
