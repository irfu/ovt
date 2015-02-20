/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/MjdEditor.java,v $
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
 * MjdEditor.java
 *
 * Created on February 25, 2000, 9:28 AM
 */
 
package ovt.beans;

import ovt.datatype.*;

import java.beans.*;

import java.awt.*;

/** 
 *
 * @author  mykola
 * @version 
 */
public class MjdEditor extends TextFieldEditor {
  
  /** Creates new MjdEditor */
 public MjdEditor(BasicPropertyDescriptor pd) {
    super(pd);
  }
  
  public String getAsText() {
    double mjd = ((Double)getValue()).doubleValue();
    return Time.toString(mjd);
  }
  
  // may be it is better to throw IllegalArgumentException ?
  // I'm not shure, that Mjd will path PropertyVetoException to setValue.
  public void setAsText(String s) throws PropertyVetoException {
    try {
      if (s.equals(getAsText())) return; 
      setValue(new Double(new Time(s).getMjd()));
    } catch (IllegalArgumentException e2) {
      throw new PropertyVetoException("Time format : (yyyy-mm-dd hh:mm:ss)", null);
    }
  }
  
}
