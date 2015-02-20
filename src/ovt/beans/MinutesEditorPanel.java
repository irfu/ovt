/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/MinutesEditorPanel.java,v $
  Date:      $Date: 2003/09/28 17:52:34 $
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
 * MjdEditorPanel.java
 *
 * Created on February 25, 2000, 10:03 AM
 */
 
package ovt.beans;

import ovt.datatype.*;

import java.beans.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/** 
 * This class is used to manipulate time values less, than 1 hour.
 * @author  mykola
 * @version 1.0
 */
public class MinutesEditorPanel extends JTextField implements PropertyChangeListener, FocusListener {

  private String lastValue = Time.toString(0);
  
  private MinutesEditor ed = null;
  
  /** Creates new MjdEditorPanel */
  public MinutesEditorPanel(MinutesEditor ed) {
    super(ed.getAsText(), 16);
    this.ed = ed;
    lastValue = ed.getAsText();
    
    //System.out.println("Init! "+ed.getAsText());
    
    addFocusListener(this);
  }
  
  public void editComplete() {
    try {
      ed.setAsText(getText());
      ed.fireEditingFinished();
      lastValue = getText();
    } catch (PropertyVetoException e2) {
      // someone didn't like it
      JOptionPane.showMessageDialog(this, "" + e2.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
      setText(lastValue);
      requestFocus();
    }
  }
  
  /*public Dimension getPrefferedSize() 
    { return new Dimension(XMINSIZE, YMINSIZE);}
  
  protected Document createDefaultModel() 
    { return new MjdDocument(); }
  
   
}

class MjdDocument extends PlainDocument {
  public void insertString(int offs, String str, AttributeSet a) 
    throws BadLocationException {
    if (str == null) return;
    
  }*/
  
  public void focusGained(FocusEvent event) {
        if (!event.isTemporary()) {
          //lastValue = getText();
        }
  }

  public void focusLost(FocusEvent event) {
        if (!event.isTemporary()) {
          editComplete();
        }
  }
  
  public void propertyChange(PropertyChangeEvent pche) {
    //if (pche.getPropertyName().equals(ed.getPropertyName()))
    refresh();
  }

  private void refresh() {
    lastValue = ed.getAsText();
    removeFocusListener(this);
    setText(lastValue);
    addFocusListener(this);
  }
  
  

  
}
