/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/SwitchMenuItemEditor.java,v $
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
 * SwitchMenuItemEditor.java
 *
 * Created on March 5, 2000, 9:18 PM
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
public class SwitchMenuItemEditor extends MenuItemEditor {
  protected JMenuItem menuItem = null;
  /** Creates new SwitchMenuItemEditor */
  public SwitchMenuItemEditor(MenuPropertyEditor editor) {
    super(editor);
  }
  
    public JMenuItem[] getMenuItems() {
    if (menuItem == null) {
      // create menu items
      
      String[] tags = getEditor().getTags();
      String value = getEditor().getAsText();
      menuItem = new JMenuItem((tags[0].equals(value) ? tags[1] : tags[0]));
      menuItem.setFont(Style.getMenuFont());
      menuItem.addActionListener(this);
      menuItem.setEnabled(getEditor().isEnabled());
    }
    return new JMenuItem[]{menuItem};
  }
  
  public void actionPerformed(ActionEvent ae) {
    //System.out.println("Action Event...");
    try {
      getEditor().setAsText(((JMenuItem)(ae.getSource())).getText());
      fireEditingFinished();
    } catch (PropertyVetoException e2) {
      System.out.println(getClass().getName() + "->" + e2);
    }
  }
  
  public void propertyChange(PropertyChangeEvent event) {
    //System.out.println("BBBBBBBBB...........");
    if (event.getPropertyName().equals(getEditor().getPropertyName())) {
      //
      JMenuItem menuItem;
      String[] tags = getEditor().getTags();
      String value = getEditor().getAsText();
      menuItem = getMenuItems()[0];
      
      menuItem.setText((tags[0].equals(value) ? tags[1] : tags[0])); 
    }
    super.propertyChange(event);
  }
  
  
  
}
