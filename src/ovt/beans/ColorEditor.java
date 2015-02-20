/*=========================================================================
 
Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/ColorEditor.java,v $
Date:      $Date: 2003/09/28 17:52:33 $
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

package ovt.beans;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.colorchooser.*;

/**
 *
 * @author  ko
 * @version
 */
public class ColorEditor extends ComponentPropertyEditor {
    
    /** Creates new ColorEditor */
    public ColorEditor(BasicPropertyDescriptor pd) {
        super(pd);
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new ColorChooserPanel(this);
            addPropertyChangeListener((PropertyChangeListener)component);
        }
        return component;
    }
    
    public String getAsText() {
        Color c = (Color) getValue();
        return new Integer(c.getRGB()).toString();
    }

    public void setAsText(String value) throws PropertyVetoException {
        try {
            int argb = new Integer(value).intValue();
            setValue(new Color(argb));
        } catch (NumberFormatException e) {
            throw new PropertyVetoException("Error: " + value, null);
        }
    }
}

class ColorChooserPanel extends JPanel implements PropertyChangeListener {
    private ColorEditor ed;
    private JColorChooser cc;
    
    ColorChooserPanel(ColorEditor en_ed) {
        cc = new JColorChooser();
        cc.setPreviewPanel(new JPanel());
        this.ed = en_ed;
        cc.getSelectionModel().addChangeListener(
        new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Color newColor = cc.getColor();
                try {
                    ed.setValue(newColor);
                    ed.fireEditingFinished();
                } catch (PropertyVetoException ignore) {} // bad colour?? Ha ha ha... .-)
            }
        });
        setLayout(new BorderLayout());
        add(cc, BorderLayout.NORTH);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Got evt " + evt.getPropertyName());
    }
}
