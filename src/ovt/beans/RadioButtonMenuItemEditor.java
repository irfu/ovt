/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/RadioButtonMenuItemEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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
 * OVTRadioButtonMenuItemEditor.java
 *
 * Created on March 5, 2000, 7:21 PM
 */
 
package ovt.beans;

import ovt.gui.Style;

import java.beans.*;
import javax.swing.*;
import java.awt.event.*;

/** 
 *
 * @author  root
 * @version 
 */
public class RadioButtonMenuItemEditor extends MenuItemEditor {

  private JMenuItem[] mItems = null;
  /** Creates new OVTRadioButtonMenuItemEditor */
  public RadioButtonMenuItemEditor(MenuPropertyEditor ed) {
    super(ed);
  }
  
  public JMenuItem[] getMenuItems() {
    if (mItems == null) {
      // create menu items
      String[] tags = getEditor().getTags();
      mItems = new JMenuItem[tags.length];
      ButtonGroup group = new ButtonGroup();
      JRadioButtonMenuItem menuItem;
      String selectedValue = getEditor().getAsText();
      for (int i=0; i<tags.length; i++) {
        menuItem = new JRadioButtonMenuItem(tags[i]);
        menuItem.setFont(Style.getMenuFont());
        group.add(menuItem);
        if (tags[i].equals(selectedValue)) menuItem.setSelected(true);
        menuItem.addActionListener(this);
        menuItem.setEnabled(getEditor().isEnabled());
        mItems[i] = menuItem;
      }
      
    }
    return mItems;
  }
  
  public void actionPerformed(ActionEvent ae) {
    try {
      JMenuItem mItem = (JMenuItem)(ae.getSource());
      getEditor().setAsText(mItem.getText());
      fireEditingFinished();
    } catch (PropertyVetoException e2) {
      System.out.println(getClass().getName() + "->" + e2);
    }
  }
  
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals(getEditor().getPropertyName())) {
      JMenuItem menuItem;
      String selectedValue = getEditor().getAsText();
      for (int i=0; i<getMenuItems().length; i++) {
        menuItem = getMenuItems()[i];
        menuItem.removeActionListener(this);
        if (menuItem.getText().equals(selectedValue)) menuItem.setSelected(true);
        menuItem.addActionListener(this);
      }
    }
    super.propertyChange(event);
  }
  
}
