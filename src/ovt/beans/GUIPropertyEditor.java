/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/GUIPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:33 $
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
 * GUIPropertyEditor.java
 *
 * Created on March 7, 2000, 3:37 PM
 */
 
package ovt.beans;

import ovt.event.*;
import ovt.interfaces.*;

import javax.swing.*;
import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
public class GUIPropertyEditor extends BasicPropertyEditor {
  
    /** Holds value of property frameOwner. */
  private JFrame frameOwner = null;
  protected GUIPropertyEditorSupport editorSupport = new GUIPropertyEditorSupport(this);
  
  /** Creates new GUIPropertyEditor */
  public GUIPropertyEditor(BasicPropertyDescriptor pd) {
    super(pd);
  }

  /** Creates new GUIPropertyEditor */
  public GUIPropertyEditor(BasicPropertyDescriptor pd, Object[] values, String[] tags) {
    super(pd, values, tags);
  }

  /** Creates new GUIPropertyEditor */
  public GUIPropertyEditor(BasicPropertyDescriptor pd, int[] values, String[] tags) {
    super(pd, values, tags);
  }

  public void addGUIPropertyEditorListener (GUIPropertyEditorListener listener) {
    editorSupport.addGUIPropertyEditorListener(listener);
  }

  public void removeGUIPropertyEditorListener (GUIPropertyEditorListener listener) {
    editorSupport.removeGUIPropertyEditorListener(listener);
  }

  public boolean hasListener(GUIPropertyEditorListener listener) {
    return editorSupport.hasListener(listener);
  }
    /** Getter for property frameOwner.
   * @return Value of property frameOwner.
   */
  public JFrame getFrameOwner() {
    return frameOwner;
  }
  /** Setter for property frameOwner.
   * @param frameOwner New value of property frameOwner.
   */
  public void setFrameOwner(JFrame frameOwner) {
    JFrame oldFrameOwner = this.frameOwner;
    this.frameOwner = frameOwner;
    propertySupport.firePropertyChange ("frameOwner", oldFrameOwner, frameOwner);
  }

  /** fires propertyChangeEvent
   *
   */
  
  public void fireEditingFinished() {
    editorSupport.fireEditingFinished();
  }

  // ??? public abstract JMenuItem[] getMenuItems();
  
}

class GUIPropertyEditorSupport {

  private Vector editorListeners = new Vector();
  private GUIPropertyEditor source = null;
  
  /** Creates new ActionSupport */
  GUIPropertyEditorSupport(GUIPropertyEditor source) {
    this.source = source;
  }

  public void addGUIPropertyEditorListener (GUIPropertyEditorListener listener) {
    editorListeners.addElement(listener);
  }

  public void removeGUIPropertyEditorListener (GUIPropertyEditorListener listener) {
    editorListeners.removeElement(listener);
  }

  /** 
   * Fires <CODE>GUIPropertyEditorEvent</CODE> to all GUIPropertyEditorListeners.
   * It is widely used for excecuting <CODE>Render()</CODE> method:
   * <PRE>editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
   *                 public void editingFinished(GUIPropertyEditorEvent evt) {
   *                     Render();
   *                 }
   *             });</PRE>
   */
  public void fireEditingFinished() {
    GUIPropertyEditorEvent evt = new GUIPropertyEditorEvent(source);
    Enumeration e = editorListeners.elements();
    while (e.hasMoreElements()) 
      ((GUIPropertyEditorListener)(e.nextElement())).editingFinished(evt);
  }
  
  public boolean hasListener(GUIPropertyEditorListener listener) {
    return editorListeners.contains(listener);
  }
}
