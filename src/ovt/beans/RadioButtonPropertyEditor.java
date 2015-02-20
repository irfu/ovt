/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/RadioButtonPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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
 * RadioButtonPropertyEditor.java
 *
 * Created on July 17, 2001, 1:34 AM
 */

package ovt.beans;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *
 * @author  root
 * @version 
 */

public class RadioButtonPropertyEditor extends GUIPropertyEditor
                        implements ActionListener {
    private JRadioButton[] buttons = null;

  /** Creates new RadioButtonPropertyEditor */
  public RadioButtonPropertyEditor(BasicPropertyDescriptor pd, Object[] values, String[] tags) {
    super(pd, values, tags);
  }

/** Creates new ComponentPropertyEditor */
  public RadioButtonPropertyEditor(BasicPropertyDescriptor pd, int[] values, String[] tags) {
    super(pd, values, tags);
  }
  
  public JRadioButton[] getButtons() {
      if (buttons == null) {
        int size = getTags().length;
        buttons = new JRadioButton[size];
        ButtonGroup group = new ButtonGroup();
        Object value = getValue();
        for (int i=0; i<size; i++) {
            OVTRadioButton rb = new OVTRadioButton(getTags()[i], getValues()[i]);
            if (getValues()[i].equals(value)) rb.setSelected(true);
            rb.addActionListener(this);
            group.add(rb);
            buttons[i] = rb;
        }
      }
      return buttons;
  }
  
  public void actionPerformed(ActionEvent evt) {
      OVTRadioButton rb = (OVTRadioButton)evt.getSource();
      try {
          setValue(rb.getUserObject());
      } catch (PropertyVetoException e2) {e2.printStackTrace(); } 
  }
  
  //public JRadioButton getButton(Object value) {  }
  public void propertyChange(PropertyChangeEvent evt) {
      //System.out.println("Recieved change of : " + evt.getPropertyName());
      super.propertySupport.firePropertyChange(evt);
      if (evt.getPropertyName().equals(getPropertyName()) && buttons != null) {
        // refresh
         Object value = getValue();
         for (int i=0; i<buttons.length; i++) {
            OVTRadioButton rb = (OVTRadioButton)buttons[i];
            if (rb.getUserObject().equals(value)) {
                rb.setSelected(true);
                return;
            }
        } 
      }
     
  }
}

class OVTRadioButton extends JRadioButton {

    private Object userObject;
    
    public OVTRadioButton(String text, Object userObject) {
        super(text);
        this.userObject = userObject;
    }
    
    public Object getUserObject() {
        return userObject;
    }
}
