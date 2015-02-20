/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/editor/CurrentMjdEditor.java,v $
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
 * CurrentMjdEditor.java
 *
 * Created on March 9, 2000, 8:10 PM
 */
 
package ovt.object.editor;

import ovt.util.*;
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
public class CurrentMjdEditor extends WindowPropertyEditor {
  
  private Component comp = null;
  private Component[] components = null;
  private CurrentMjdVCRComponents currentMjdVCRComponents= null;

  
  /** Creates new CurrentMjdEditor */
  public CurrentMjdEditor(BasicPropertyDescriptor pd) {
    super(pd);
    initialize();
  }
  
  public Component getComponent() {
    if (comp == null) {
      JPanel c = new JPanel();
      Component[] comps = getComponents();
      for (int i=0; i<comps.length; i++)
        c.add(comps[i]);
      comp = c;
    }
    return comp;
  }
  
  public Component[] getComponents() {
    if (currentMjdVCRComponents == null) {
      currentMjdVCRComponents = new CurrentMjdVCRComponents(this);
      addPropertyChangeListener(currentMjdVCRComponents);
    }
    return currentMjdVCRComponents.getComponents();
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    //System.out.println("editor-propertychange: " + evt.getPropertyName());
    if ((evt.getPropertyName().equals("time"))) {
      //(evt instanceof TimeEvent) &&  ?????
      initialize();
    }
    super.propertyChange(evt);
  }
  
  public void initialize() {
      Object[] values = Vect.toObjectArray(getTimeSet().getValues());
      String[] tags = new String[values.length];
      
      for (int i=0; i<values.length; i++) {
        tags[i] = Time.toString(((Double)values[i]).doubleValue());
        //System.out.println(Time.toString(((Double)values[i]).doubleValue()));
      }
      setTags(tags);
      setValues(values);
      setModal(false);
  }
  
  public TimeSet getTimeSet() 
    { return ((TimeSettings)getBean()).getTimeSet(); }
    
  public double getCurrentMjd() 
    { return ((Double)getValue()).doubleValue(); }

  public boolean isStart() 
    { return (getCurrentMjd() == getTimeSet().getStartMjd()); }

  public boolean isStop() 
    { return (getCurrentMjd() > getTimeSet().getStopMjd() -
          getTimeSet().getStepMjd() + 2e-10); }
}
