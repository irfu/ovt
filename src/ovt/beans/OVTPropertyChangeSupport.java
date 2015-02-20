/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/OVTPropertyChangeSupport.java,v $
  Date:      $Date: 2003/09/28 17:52:35 $
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
 * PropertyChangeSupport.java
 *
 * Created on March 22, 2000, 2:27 PM
 */
 
package ovt.beans;

import ovt.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
/** 
 *
 * @author  root
 * @version 
 */
public class OVTPropertyChangeSupport {
  
  protected Object sourceBean;
  protected Vector propertyListeners = new Vector();
  /** Contains (propertyName), (Vector of propertylisteners) pairs*/
  protected Hashtable specificPropertyListeners = new Hashtable();
  
  public OVTPropertyChangeSupport(Object sourceBean) {
    this.sourceBean = sourceBean;
  }
 
  public void addPropertyChangeListener (PropertyChangeListener listener) {
    propertyListeners.addElement (listener);
  }
  
  public void removePropertyChangeListener (PropertyChangeListener listener) {
    propertyListeners.removeElement (listener);
  }
  
  public void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
    Vector listeners = (Vector)specificPropertyListeners.get(propertyName);
    if (listeners == null) listeners.remove(listener);
  }
  
  public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
    Vector listeners = (Vector)specificPropertyListeners.get(propertyName);
    if (listeners == null) {
      listeners = new Vector();
      listeners.addElement(listener);
      specificPropertyListeners.put(propertyName, listeners);
    } else {
      listeners.addElement(listener);
    }
  }
  
  public void firePropertyChange(PropertyChangeEvent evt) {
    deliverEvent(propertyListeners, evt);
    Vector listeners = (Vector)specificPropertyListeners.get(evt.getPropertyName());
    if (listeners != null) deliverEvent(listeners, evt);
  }
  
  protected static void deliverEvent(Vector propertyListeners, PropertyChangeEvent evt) {
    Enumeration e = propertyListeners.elements();
    PropertyChangeListener propertyListener;
    while (e.hasMoreElements()) {
      propertyListener = ((PropertyChangeListener)e.nextElement());
      if (OVTCore.DEBUG > 0) {
        try {
          
          System.out.println( " PropertyChange->" + ((NamedObject)propertyListener).getName());
        } catch (ClassCastException e2) {
          Object source = (evt.getSource());
          System.out.println(source.getClass().getName() + " PropertyChange->" + (propertyListener).getClass().getName());
        }
      }
      propertyListener.propertyChange(evt);
    }
  }
  
  public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    PropertyChangeEvent evt = new PropertyChangeEvent(sourceBean, propertyName, oldValue, newValue);
    firePropertyChange(evt);
  }
  
  public void removeAllPropertyChangeListeners() {
    propertyListeners.removeAllElements();
    specificPropertyListeners.clear();
  }
  
  public String toString() {
    return "OVTPropertyChangeSupport has "+propertyListeners.size() + "listeners and "+specificPropertyListeners.size()+" listeners of specific property";
  }
    
}
