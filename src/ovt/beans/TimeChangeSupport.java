/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/TimeChangeSupport.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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
 * TimeChangeSupport.java
 *
 * Created on March 13, 2000, 4:09 PM
 */
 
package ovt.beans;

import ovt.*;
import ovt.event.*;
import ovt.interfaces.*;

import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */

public class TimeChangeSupport {

  private Vector timeChangeListeners = new Vector();
  private Object source = null;
  
  public TimeChangeSupport(Object source) {
    this.source = source;
  }

  public void addTimeChangeListener (TimeChangeListener listener) {
    timeChangeListeners.addElement(listener);
  }

  public void removeTimeChangeListener (TimeChangeListener listener) {
    timeChangeListeners.removeElement(listener);
  }

  public void fireTimeChange(TimeEvent evt) {
    Enumeration e = timeChangeListeners.elements();
    fireTimeChange(evt, e);
  }
  
  /** Deliver event evt to all elements of enumeration e */
  public static void fireTimeChange(TimeEvent evt, Enumeration e) {
    TimeChangeListener timeListener;
    while (e.hasMoreElements()) {
      try {
        timeListener = ((TimeChangeListener)(e.nextElement()));
        if (OVTCore.DEBUG > 0) {
          try {
            System.out.println("TimeChangeEvent ->" + ((NamedObject)timeListener).getName());
          } catch (ClassCastException e2) {}
        }
        timeListener.timeChanged(evt);
      } catch (ClassCastException e2) {}
    }
  }
  
  /*public void fireTimeChange(String property, Object oldValue, Object newValue) {
    TimeEvent evt = new TimeEvent(source, property, oldValue, newValue);
    fireTimeChange(evt);
  }*/
  
  public boolean hasListener(TimeChangeListener listener) {
    return timeChangeListeners.contains(listener);
  }
    
}
