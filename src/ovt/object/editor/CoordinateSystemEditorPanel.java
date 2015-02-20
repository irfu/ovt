/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/editor/CoordinateSystemEditorPanel.java,v $
  Date:      $Date: 2003/09/28 17:52:53 $
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
 * CoordinateSystemEditorPanel.java
 *
 * Created on March 20, 2000, 6:49 PM
 */
 
package ovt.object.editor;

import ovt.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.datatype.*;


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
public class CoordinateSystemEditorPanel extends JComboBox implements PropertyChangeListener {

  CoordinateSystemEditor editor;
  
  /** Creates new CoordinateSystemEditorPanel */
  public CoordinateSystemEditorPanel(CoordinateSystemEditor editor) {
    super(editor.getTags());
    setMaximumSize(getPreferredSize());
    setToolTipText("Coordinate System");
    this.editor = editor;
    addActionListener(this);
    refresh();
  }

  public void refresh() {
    removeActionListener(this);
    setSelectedItem(editor.getAsText());
    addActionListener(this);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JComboBox) {
      JComboBox cb = (JComboBox)e.getSource();
		
      String csName = (String)cb.getSelectedItem();
      try {
        editor.setAsText(csName);
        editor.fireEditingFinished();
      } catch (PropertyVetoException e2) {
        e2.printStackTrace();
      }
    }
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    refresh();
  }
}
