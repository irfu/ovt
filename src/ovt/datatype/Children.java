/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Children.java,v $
  Date:      $Date: 2003/09/28 17:52:36 $
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
 * Children.java
 *
 * Created on March 14, 2000, 3:28 PM
 */
 
package ovt.datatype;

import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.interfaces.*;

import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
public class Children {

  /** Childrens parent */
  private OVTObject parent = null;  
  protected Vector keys;
  protected Vector values;
  
  private Vector changeListeners = new Vector();
  
/** Creates new Children 
  public Children() {
    keys   = new Vector();
    values = new Vector();
  }*/

  /** Creates new Children with a specified parent */
  public Children(OVTObject parent) {
    this.parent = parent;
    keys   = new Vector();
    values = new Vector();
  }
  
  public OVTObject getParent() {
    return parent;
  }

  
  /** Adds the child and sets its parent to {@link #parent } */
  public void addChild(OVTObject obj) {
    if (parent != null) obj.setParent(parent);
    put(obj.getName(), obj);
  }  

  protected void put(String key, OVTObject obj) {
    keys.addElement(key);
    values.addElement(obj); 
  }
  
  /** Used by XML indirectly. 
   * Also sets childs parent to {@link #parent this.parent}. 
   * @see ovt.object.Sats#setSatAt(int,ovt.object.Sat) 
   */
  public void setChildAt(int index, OVTObject child) {
    //ovt.util.Log.log("setChildAt("+index+","+child.getName()+")");
    if (parent != null) child.setParent(parent);
    keys.setElementAt(child.getName(), index);
    values.setElementAt(child, index); 
  }

  /*
  public void remove(String key) {
    Object obj = get(key);
    if (obj != null) {
      keys.removeElement(key);
      values.removeElement(obj);
    }
  } */

  public void remove(OVTObject obj) {
    int index = values.indexOf(obj);
    //System.out.println("index=" + index);
    if (index != -1) {
      keys.removeElementAt(index);
      values.removeElementAt(index);
    }
  }

  public void removeChild(OVTObject obj) {
    remove(obj);
  }
  
  /** Child getter by name */
  public OVTObject getChild(String name) { 
    Log.log("Children ::  get(" + name + ")", 7);
    int index = keys.indexOf(name);
    //System.out.println("index=" + index);
    if (index == -1) return null;
    return (OVTObject)values.elementAt(index); 
  }

  public boolean containsChild(String name) {
    return keys.contains(name);
  }
  
  public Enumeration elements() {
    return values.elements();
  }
  
  public Object[] toArray() {
    return values.toArray();
  }
  
  public OVTObject getLastChild() {	// added by oleg
    return (OVTObject) values.lastElement();
  }

  /*public static Children add(Children ch1, Children ch2) {
    Enumeration e = ch2.elements();
    NamedObject obj;
    while (e.hasMoreElements()) {
      obj = (NamedObject)e.nextElement();
      ch1.put(obj);
    }
    retur
  }*/

  public void clear() {
    keys.removeAllElements();
    values.removeAllElements();
  }

  /** used by XML */
  public void setSize(int newSize) {
    keys.setSize(newSize);
    values.setSize(newSize);
  }
  
  public int size() {
    return values.size();
  }
  
  public void addChildrenListener(ChildrenListener listener) {
    if (!changeListeners.contains(listener)) changeListeners.addElement(listener);
  }
  
  public void removeChildrenListener(ChildrenListener listener) {
    changeListeners.removeElement(listener);
  }
  
  public void fireChildAdded(OVTObject child) {
    fireChange(new ChildrenEvent(this, ChildrenEvent.CHILD_ADDED, child));
  }
  
  public void fireChildRemoved(OVTObject child) {
      fireChange(new ChildrenEvent(this, ChildrenEvent.CHILD_REMOVED, child));
  }
  
  
  public void fireChildrenChanged() {
      fireChange(new ChildrenEvent(this));
  }
  
  public void fireChange(ChildrenEvent evt) {
    Enumeration e = changeListeners.elements();
    switch (evt.getType()) {
        case ChildrenEvent.CHILD_ADDED      :
            while (e.hasMoreElements()) {
                ChildrenListener listener = ((ChildrenListener)(e.nextElement()));
                listener.childAdded(evt);
            } 
            break;
        case ChildrenEvent.CHILD_REMOVED    :
            while (e.hasMoreElements()) {
                ChildrenListener listener = ((ChildrenListener)(e.nextElement()));
                listener.childRemoved(evt);
            } 
            break;
        case ChildrenEvent.CHILDREN_CHANGED :  
            while (e.hasMoreElements()) {
                ChildrenListener listener = ((ChildrenListener)(e.nextElement()));
                listener.childrenChanged(evt);
            }
            break;
  }
  
}



    
}
