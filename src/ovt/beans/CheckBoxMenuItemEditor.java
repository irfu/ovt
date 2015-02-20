/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/CheckBoxMenuItemEditor.java,v $
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
 * OVTCheckBoxMenuItemEditor.java
 *
 * Created on March 6, 2000, 1:10 PM
 */
 
package ovt.beans;

import ovt.gui.Style;

import java.beans.*;
import javax.swing.*;
import java.awt.event.*;


/** 
 * Is used for properties which can be true/false
 * @author  root
 * @version 
 */
public class CheckBoxMenuItemEditor extends MenuItemEditor implements ItemListener {

  private JMenuItem menuItem = null;
  
  /** Creates new OVTCheckBoxMenuItemEditor */
  public CheckBoxMenuItemEditor(GUIPropertyEditor editor) {
    super(editor);
  }
  
  public JMenuItem[] getMenuItems() {
    if (menuItem == null) {
      // create menu items
      String value = getEditor().getAsText();
      menuItem = new JCheckBoxMenuItem(getEditor().getPropertyLabel());
      menuItem.setFont(Style.getMenuFont());
      menuItem.setSelected(((Boolean)(getEditor().getValue())).booleanValue());
      menuItem.addItemListener(this);
      menuItem.setEnabled(getEditor().isEnabled());
    }
    return new JMenuItem[]{menuItem};
  }
  
  
  
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals(getEditor().getPropertyName())) {
      //
      JMenuItem menuItem = getMenuItems()[0];
      boolean newState = ((Boolean)(getEditor().getValue())).booleanValue();
      menuItem.removeItemListener(this);
      menuItem.setSelected(newState); 
      menuItem.addItemListener(this);
    }
    super.propertyChange(event);
  }
  
  public void itemStateChanged(ItemEvent evt) {
    try {
      getEditor().setValue(new Boolean(((JCheckBoxMenuItem)(evt.getSource())).isSelected()));
      fireEditingFinished();
    } catch (PropertyVetoException e2) {
      System.out.println(getClass().getName() + "->" + e2);
    }
  }
  
}
