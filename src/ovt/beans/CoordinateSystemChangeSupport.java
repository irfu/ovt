/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/CoordinateSystemChangeSupport.java,v $
  Date:      $Date: 2003/09/28 17:52:33 $
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
 * CoordinateSystemsChangeSupport.java
 *
 * Created on March 20, 2000, 5:23 PM
 */
 
package ovt.beans;

import ovt.event.*;
import ovt.interfaces.*;

import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
public class CoordinateSystemChangeSupport extends Object {

  private Vector coordinateSystemChangeListeners = new Vector();
  private Object source = null;

  
  /** Creates new CoordinateSystemsChangeSupport */
  public CoordinateSystemChangeSupport(Object source) {
    this.source = source;
  }

  public void addCoordinateSystemChangeListener (CoordinateSystemChangeListener listener) {
    coordinateSystemChangeListeners.addElement(listener);
  }

  public void removeCoordinateSystemChangeListener (CoordinateSystemChangeListener listener) {
    coordinateSystemChangeListeners.removeElement(listener);
  }

  public void fireCoordinateSystemChange(CoordinateSystemEvent evt) {
    Enumeration e = coordinateSystemChangeListeners.elements();
    fireCoordinateSystemChange(evt, e);
  }
  
  /** Deliver event evt to all elements of enumeration e */
  public static void fireCoordinateSystemChange(CoordinateSystemEvent evt, Enumeration e) {
    while (e.hasMoreElements()) {
      try {
          Object obj = e.nextElement();
          //try { System.out.println("fireCoordinateSystemChange to " + ((NamedObject)obj).getName());} catch (ClassCastException ignore) {ignore.printStackTrace();}
        ((CoordinateSystemChangeListener)(obj)).coordinateSystemChanged(evt);
      } catch (ClassCastException e2) {}
    }
  }
  
  public void fireCoordinateSystemChange(int window, int old_cs, int new_cs) {
    CoordinateSystemEvent evt = new CoordinateSystemEvent(source, window, old_cs, new_cs);
    fireCoordinateSystemChange(evt);
  }
  
  public boolean hasListener(CoordinateSystemChangeListener listener) {
    return coordinateSystemChangeListeners.contains(listener);
  }
    
}
