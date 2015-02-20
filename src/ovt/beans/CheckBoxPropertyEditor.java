/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/CheckBoxPropertyEditor.java,v $
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


/*
 * CheckBoxPropertyEditor.java
 *
 * Created on March 28, 2001, 6:35 PM
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
 * @author  ko
 * @version 
 */
public class CheckBoxPropertyEditor extends ComponentPropertyEditor {

/** Creates new CheckBoxPropertyEditor */
public CheckBoxPropertyEditor(BasicPropertyDescriptor pd) {
        super(pd);
        setTags(new String[]{"true", "false"});
        setValues(new Object[]{new Boolean(true), new Boolean(false)});
}

public Component getComponent() {
  if (component == null) {
      component = new CheckBoxEditorPanel(this);
      addPropertyChangeListener((PropertyChangeListener)component);
  }
  return component;
}

public boolean booleanValue() {
    return ((Boolean)getValue()).booleanValue();
}

}

class CheckBoxEditorPanel extends JCheckBox 
        implements PropertyChangeListener, ActionListener {
  CheckBoxPropertyEditor editor;
  
  /** Creates new CoordinateSystemEditorPanel */
  CheckBoxEditorPanel(CheckBoxPropertyEditor editor) {
    super(editor.getPropertyLabel());
    setSelected(editor.booleanValue());
    //setMinimumSize(getPreferredSize());
    //setToolTipText("Coordinate System");
    this.editor = editor;
    addActionListener(this);
  }

  public void refresh() {
    removeActionListener(this);
    setSelected(editor.booleanValue());
    addActionListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    try {
        editor.setValue(new Boolean(isSelected()));
        editor.fireEditingFinished();
    } catch (PropertyVetoException e2) {
        e2.printStackTrace();
    }
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    String prName = evt.getPropertyName();
    //System.out.println("Recieved change of : " + prName);
    //System.out.println("My prName is " + editor.getPropertyName());
    if (prName.equals(editor.getPropertyName())) 
        refresh();
    }
}
