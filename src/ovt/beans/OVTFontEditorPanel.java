/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/OVTFontEditorPanel.java,v $
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

package ovt.beans;

import ovt.datatype.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *
 * @author  oleg
 * @version 
 */
public class OVTFontEditorPanel extends JPanel {

    private OVTFont bean;
    
    //private JTextArea taText;
    
    //JCheckBox chBold, chItalic, chShadow;
    
    //JComboBox cbFontSize, cbFontFamily, cbHorizJustification, cbVertJustification;    // Ok
           
    /** Creates new OVTFontEditorPanel */
    public OVTFontEditorPanel(OVTFont font) {
        super();
        this.bean = font;  // bind with OVTFont object
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(new EtchedBorder(), "Font"));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,2,10,10));
        
        panel.add(getComponent("fontFamily"));
        panel.add(getComponent("fontSize"));
        add(panel);
        
        panel = new JPanel();
        panel.setLayout(new GridLayout(1,3,10,10));

        panel.add(getComponent("bold"));
        panel.add(getComponent("italic"));
        panel.add(getComponent("shadow"));
        add(panel);

    }

    private JComponent getComponent(String propertyName) {
        BasicPropertyDescriptor desc = bean.getDescriptors().getDescriptor(propertyName);
        return (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
    }
    
    /*
    private JPanel propertyEditor(String propertyName) {
        JPanel panel = new JPanel(false);   // doublebuffered = false
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        BasicPropertyDescriptor desc = fontedText.getDescriptors().getDescriptor(propertyName);
        panel.add(new JLabel(desc.getDisplayName()));
        JComponent c = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        //if (c instanceof JTextField)
        c.setPreferredSize(new Dimension(100, c.getPreferredSize().height));
        panel.add(c);
        return panel;
    }

    private void addPropertyEditor(JPanel panel, String propertyName) {
        BasicPropertyDescriptor desc = fontedText.getDescriptors().getDescriptor(propertyName);
        JComponent c = (JComponent)((ComponentPropertyEditor)(desc.getPropertyEditor())).getComponent();
        panel.add(new JLabel(desc.getDisplayName()));
        panel.add(c);
    }
     */
}
