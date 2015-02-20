/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/editor/CoordinateSystemEditor.java,v $
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
 * CoordinateSystemEditor.java
 *
 * Created on March 20, 2000, 6:24 PM
 */
 
package ovt.object.editor;

import ovt.beans.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;

import java.beans.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
//SHould be removed! should extend ComboBoxEditor...
public class CoordinateSystemEditor extends WindowPropertyEditor {

  private Component comp = null;
  private Component[] components = null;
  

  /** Creates new CoordinateSystemEditor */
  public CoordinateSystemEditor(BasicPropertyDescriptor pd, int[] intValues) {
    super(pd);
    initialize(intValues);
  }
  

  public Component getComponent() {
    if (comp == null) {
      comp = new CoordinateSystemEditorPanel(this);
      addPropertyChangeListener((PropertyChangeListener)comp);
    }
    return comp;
  }
  
  
  public void initialize(int[] intValues) {
    Object[] values = new Object[intValues.length];
    String[] tags = new String[intValues.length];
    
    for (int i=0; i<intValues.length; i++) {
      values[i] = new Integer(intValues[i]);
      tags[i]   = CoordinateSystem.getCoordSystem(intValues[i]);
    }
    
    setTags(tags);
    setValues(values);
    setModal(false);
  }
}
