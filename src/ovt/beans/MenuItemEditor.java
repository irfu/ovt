/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/MenuItemEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:34 $
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

/*
 * OVTMenuItemEditor.java
 *
 * Created on March 5, 2000, 6:44 PM
 */
 
package ovt.beans;

import ovt.interfaces.*;

import java.beans.*;
import javax.swing.*;
import java.awt.event.*;

/** 
 * This is a superclass for CheckBoxMenuItemEditor, RadioButtonMenuItemEditor, etc..
 * It is NOT PropertyEditor class!
 * @author  mykola
 * @version 
 */
public abstract class MenuItemEditor implements MenuItemsSource, ActionListener, PropertyChangeListener {
  
  private GUIPropertyEditor editor = null;
  //private JMenuItem menuItem = null;
  
  /** Creates new OVTMenuItemEditor */
  public MenuItemEditor(GUIPropertyEditor ed) {
    editor = ed;
  }
  
  public abstract JMenuItem[] getMenuItems();
  
  public GUIPropertyEditor getEditor() 
    { return editor; }
  
  public void fireEditingFinished() {
    editor.fireEditingFinished();
  }
    
  public void actionPerformed(ActionEvent ae) {
  }
  
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("enabled")) {
      //System.out.println("AAAAAAAAAA...........");
      boolean enabled = ((Boolean)event.getNewValue()).booleanValue();
      setEnabled(enabled);
    }
  }
  
  public void setEnabled(boolean enabled) {
    JMenuItem[] menuItems = getMenuItems();
    for (int i=0; i<menuItems.length; i++)
        menuItems[i].setEnabled(enabled);
  }
}
