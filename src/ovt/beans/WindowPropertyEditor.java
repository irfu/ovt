/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/WindowPropertyEditor.java,v $
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
 * WindowPropertyEditor.java
 *
 * Created on March 7, 2000, 3:55 PM
 */
 
package ovt.beans;

import ovt.event.*;
import ovt.interfaces.*;

import java.beans.*;
import javax.swing.*;
import java.awt.*;


/** 
 *
 * @author  root
 * @version 
 */
public class WindowPropertyEditor extends ComponentPropertyEditor 
                              implements GUIPropertyEditorListener, MenuItemsSource {

  private DefaultPropertyEditorWindow window = null;
  private boolean windowVisible = false;
  private MenuPropertyEditor visibilityEditor = null;
  
  
 
  /** Holds value of property modal. */
  private boolean modal = true;
  /** Creates new WindowPropertyEditor 
   * type shoud be one of MenuPropertyEditor constants.
   */
  public WindowPropertyEditor(BasicPropertyDescriptor pd, int type) {
    super(pd);
    initialize(type, new String[]{"show", "hide"});
  }
  
  public WindowPropertyEditor(BasicPropertyDescriptor pd, String[] showHideTags) {
    super(pd);
    initialize(MenuPropertyEditor.SWITCH, showHideTags);
  }
  
  /** Creates new WindowPropertyEditor 
   * with default MenuItem type = SWITCH
   */
  public WindowPropertyEditor(BasicPropertyDescriptor pd) {
    super(pd);
    initialize(MenuPropertyEditor.SWITCH, new String[]{"show", "hide"});
  }
  
  protected void initialize(int type, String[] showHideTags) {
    try {
      BasicPropertyDescriptor prop_descr = new BasicPropertyDescriptor("windowVisible", this);
      prop_descr.setLabel(getPropertyLabel());
      visibilityEditor = new MenuPropertyEditor(prop_descr, type);
      visibilityEditor.setTags(showHideTags);
      visibilityEditor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
      //prop_descr.setPropertyEditor(visibilityEditor);
      addPropertyChangeListener(visibilityEditor);
      
    } catch (IntrospectionException e2) {System.out.println(""+e2);}
    addGUIPropertyEditorListener(this);
  }
  
  /** By default makes
   *
   * @return GUI Editor
   */
  public Window getWindow() {
    if (window == null) {
      // Create JDialog by default.
      window = new DefaultPropertyEditorWindow(getFrameOwner(), this, isModal());
      addPropertyChangeListener(window);
    }
    return window;
  }
  
  public void setWindowVisible(boolean value) {
    if (value == windowVisible) return; // nothing, s changed
    boolean oldvalue = windowVisible;
    windowVisible = value;
    getWindow().setVisible(value);
    propertySupport.firePropertyChange("windowVisible", new Boolean(oldvalue), new Boolean(windowVisible));
  }
  
  public boolean isWindowVisible() {
    return windowVisible;
  }
  
  public JMenuItem[] getMenuItems() {
    return visibilityEditor.getMenuItems();
  }

  public void editingFinished(GUIPropertyEditorEvent evt) {
    if (closeOnEditingFinished()) setWindowVisible(false);
  }
  /** Getter for property modal.
   * @return Value of property modal.
   */
  public boolean isModal() {
    return modal;
  }
  /** Setter for property modal.
   * @param modal New value of property modal.
   */
  public void setModal(boolean modal) {
    this.modal = modal;
  }
  
  public boolean closeOnEditingFinished() {
    return false;
  }
}
