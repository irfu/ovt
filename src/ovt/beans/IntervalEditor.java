/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/beans/IntervalEditor.java,v $
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
 * IntervalEditor.java
 *
 * Created on November 28, 2000, 5:13 PM
 */

package ovt.beans;

import ovt.datatype.*;

import java.beans.*;

import java.util.*;

/**
 *
 * @author  ko
 * @version 
 */
public class IntervalEditor extends TextFieldEditor {

/** Creates new IntervalEditor */
public IntervalEditor(BasicPropertyDescriptor pd) {
    super(pd);
    if (pd.getToolTipText() == null)
        pd.setToolTipText("[_d] [_h] [_m] [_s]");
}

  public String getAsText() {
    double mjd = ((Double)getValue()).doubleValue();
    return new Interval(mjd).toString();
  }
  
  // may be it is better to throw IllegalArgumentException ?
  // I'm not shure, that Mjd will path PropertyVetoException to setValue.
  public void setAsText(String s) throws PropertyVetoException {
    try {
      Interval interval = new Interval(s);
      Interval oldInterval = new Interval(getAsText());
      if (interval.equals(oldInterval)) return;
      setValue(new Double(interval.getMjd()));
    } catch (NumberFormatException e2) {
      throw new PropertyVetoException("Time format : ([ad] [bh] [cm] [ds])", null);
    } catch (IllegalArgumentException e3) {
      throw new PropertyVetoException("Time format : ([ad] [bh] [cm] [ds])", null);
    }
  }

}

