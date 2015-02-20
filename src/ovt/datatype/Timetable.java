/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Timetable.java,v $
  Date:      $Date: 2003/09/28 17:52:39 $
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
 * TimeTable.java
 *
 * Created on March 24, 2000, 3:41 PM
 */
 
package ovt.datatype;

import java.util.*;

/** 
 * This is a class for storing mjd - object pairs
 * @author  mykola
 * @version 
 */
public class Timetable {

  protected Vector keys;
  protected Vector values;
  
  /** Creates new TimeTable */
  public Timetable() {
    keys   = new Vector();
    values = new Vector();
  }
  
  public void put(double mjd, Object obj) {
    keys.addElement(new Double(mjd));
    values.addElement(obj); 
  }

  public Object getElement(double mjd, double eps) {
      Enumeration e = keys();
      while (e.hasMoreElements()) {
          double key = ((Double)e.nextElement()).doubleValue();
          if (Math.abs(key - mjd) < eps) {
              return getElement(key);
          }
      }
      return null;
  }
  
  public Object getElement(double mjd) { 
    int index = keys.indexOf(new Double(mjd));
    //System.out.println("index=" + index);
    if (index == -1) {
        //System.out.println("Timetable::getElement() returns null!!!");
        //System.out.println("\tmjd = " + mjd);
        //dump();
        return getElement(mjd, 0.0000001);
    }
    else 
        return values.elementAt(index);
  }

  public Object firstElement() {
    return values.firstElement();
  }
  
  public Object lastElement() {
    return values.lastElement();
  }
  
  public Enumeration elements() {
    return values.elements();
  } 
  
  public void clear() {
    keys.removeAllElements();
    values.removeAllElements();
  }
  
  public int size() {
    return values.size();
  }
  
  public Enumeration keys() {
    return keys.elements();
  }
  
  public void dump() {
      System.out.println("--------------------- Dumping Timetable --------------------");
      Enumeration e = keys();
      int i=0;
      while (e.hasMoreElements()) {
          Double mjd = (Double)e.nextElement();
          Object obj = getElement(mjd.doubleValue());
          System.out.println("" + (i++) + ". mjd = " + mjd + "\t Object = " + obj);
      }
  }
}
