/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/Descriptors.java,v $
  Date:      $Date: 2003/09/28 17:52:33 $
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
 * PropertyDescriptors.java
 *
 * Created on March 8, 2000, 9:37 PM
 */
 
package ovt.beans;

import java.beans.IndexedPropertyDescriptor;

import java.util.*;

/** 
 * This class is a hashtable (propertyName, BasicPropertyDescriptor)
 * @author  root
 * @version 
 */
public class Descriptors extends Hashtable {

  private Hashtable indexedPropertyDescriptors = new Hashtable();
    
  /** Creates new PropertyDescriptors */
  public Descriptors() {
  }
  
  public void put(BasicPropertyDescriptor pd) 
    { put(pd.getName(), pd); }
  
  public BasicPropertyDescriptor getDescriptor(String propertyName) 
    { return (BasicPropertyDescriptor)get(propertyName); }
  
  public void put(IndexedPropertyDescriptor pd) 
    { indexedPropertyDescriptors.put(pd.getName(), pd); }
    
  public IndexedPropertyDescriptor getIndexedPropertyDescriptor(String propertyName) 
    { return (IndexedPropertyDescriptor)indexedPropertyDescriptors.get(propertyName); }  
  
  public Enumeration getIndexedPropertyDescriptors() 
    {  return indexedPropertyDescriptors.elements(); }
  
    
  /** Removes descriptor index or not indexed */
  public void removeDescriptor(String propertyName) {
      remove(propertyName);
      indexedPropertyDescriptors.remove(propertyName);
  }
  
  public String toString() {
    String res = "";
    Enumeration e = elements();
    BasicPropertyDescriptor pd;
    ovt.interfaces.OVTPropertyEditor ed;
    while (e.hasMoreElements()) {
        pd = (BasicPropertyDescriptor)e.nextElement();
        ed = pd.getPropertyEditor();
        res += "'" + pd.getName() + "' editor='" + ((ed == null) ? "" : ed.getClass().getName()) + "'\n";
    }
    return res;
  }
}
