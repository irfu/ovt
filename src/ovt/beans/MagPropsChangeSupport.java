/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/MagPropsChangeSupport.java,v $
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
 * MagPropsChangeSupport.java
 *
 * Created on den 9 april 2000, 17:53
 */
 
package ovt.beans;

import ovt.util.Log;
import ovt.event.*;
import ovt.interfaces.*;

import java.util.*;

/** 
 *
 * @author  mykola
 * @version 
 */

public class MagPropsChangeSupport {

  private Vector listeners = new Vector();
  private Object source = null;


  /** Creates new MagPropssChangeSupport */
  public MagPropsChangeSupport(Object source) {
    this.source = source;
  }

  public void addMagPropsChangeListener (MagPropsChangeListener listener) {
    listeners.addElement(listener);
  }

  public void removeMagPropsChangeListener (MagPropsChangeListener listener) {
    listeners.removeElement(listener);
  }

  public void fireMagPropsChange(MagPropsEvent evt) {
    Enumeration e = listeners.elements();
    while (e.hasMoreElements())
      ((MagPropsChangeListener)(e.nextElement())).magPropsChanged(evt);
  }

  /** Delivers event evt to all elements of enumeration e */
  public static void fireMagPropsChange(MagPropsEvent evt, Enumeration e) {
    while (e.hasMoreElements()) {
      try {
        ((MagPropsChangeListener)(e.nextElement())).magPropsChanged(evt);
      } catch (ClassCastException e2) {}
    }
  }
  
  public void fireMagPropsChange() {
    Log.log("fireMagPropsChange is doing nothing :-))", 0);
    /*MagPropsEvent evt = new MagPropsEvent(source, );
    fireMagPropsChange(evt);*/
  }

  public boolean hasListener(MagPropsChangeListener listener) {
    return listeners.contains(listener);
  }

}
