/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/MenuPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:34 $
  Version:   $Revision: 2.5 $


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
 * MenuPropertyEditor.java
 *
 * Created on March 7, 2000, 3:31 PM
 */
 
package ovt.beans;

import ovt.interfaces.*;

import javax.swing.*;

/** 
 *
 * @author  root
 * @version 
 */
public class MenuPropertyEditor extends GUIPropertyEditor implements MenuItemsSource {
  
  public static final int CHECKBOX    = 0;
  public static final int RADIOBUTTON = 1;
  public static final int SWITCH      = 2;
  
  private MenuItemEditor menuItemEditor = null;
  private int type = RADIOBUTTON;
  
  
  /** Creates new MenuPropertyEditor */
  public MenuPropertyEditor(BasicPropertyDescriptor pd) {
    super(pd);
    this.type = RADIOBUTTON;
  }
  
  /** Creates new MenuPropertyEditor */
  public MenuPropertyEditor(BasicPropertyDescriptor pd, int type) {
    super(pd);
    this.type = type;
  }
  
  /** Creates new MenuPropertyEditor */
  public MenuPropertyEditor(BasicPropertyDescriptor pd, String[] tags) {
    super(pd);
    this.type = RADIOBUTTON;
    setValues(ovt.util.Utils.getIndexes(tags));
    setTags(tags);
  }
  
  /** Creates new MenuPropertyEditor */
  public MenuPropertyEditor(BasicPropertyDescriptor pd, int[] values, String[] tags) {
    super(pd, values, tags);
    this.type = RADIOBUTTON;
    //setValues(ovt.util.Utils.getIndexes(tags));
    //setTags(tags);
  }
  
  
  
  protected MenuItemEditor getMenuItemEditor() {
    //System.out.println("Type = " + type);
    if (menuItemEditor == null) {
      switch (type) {
        case CHECKBOX:     menuItemEditor = new CheckBoxMenuItemEditor(this); break;
        case RADIOBUTTON:  menuItemEditor = new RadioButtonMenuItemEditor(this); break;
        case SWITCH:       menuItemEditor = new SwitchMenuItemEditor(this);
        //default:     menuItemEditor = new RadioButtonMenuItemEditor(this); break;
      }
      addPropertyChangeListener(menuItemEditor);
    }
    return menuItemEditor;
  }

  public JMenuItem[] getMenuItems() {
    return getMenuItemEditor().getMenuItems();
  }

public int getType() {
    return type;
}

public void setEnabled(int item, boolean enabled) {
    getMenuItems()[item].setEnabled(enabled);
}

}
