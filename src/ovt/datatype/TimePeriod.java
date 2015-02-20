/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/TimePeriod.java,v $
  Date:      $Date: 2003/09/28 17:52:38 $
  Version:   $Revision: 1.3 $


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
 * TimePeriod.java
 *
 * Created on July 31, 2001, 6:38 AM
 */

package ovt.datatype;

import ovt.object.*;
import ovt.object.editor.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.interfaces.*;

import java.beans.*;
import java.lang.reflect.*;

/**
 *
 * @author  root
 * @version 
 */
public class TimePeriod extends Object implements TimePeriodSource {

   private double startMjd = 0;
   private double stopMjd = 0;
    
    /** Creates new TimePeriod */
    public TimePeriod() {
    }

 
  /** Creates new TimePeriod */
  public TimePeriod(double startMjd, double stopMjd) {
    this.startMjd = startMjd;
    this.stopMjd = stopMjd;
  }
  
  public void set(TimePeriodSource timePeriodSource) {
    setStartMjd(timePeriodSource.getStartMjd());
    setStopMjd(timePeriodSource.getStopMjd());
  }

    /** Getter for property startMjd.
   * @return Value of property startMjd.
   */
  public double getStartMjd() {
    return startMjd;
  }
  /** Setter for property startMjd.
   * @param startMjd New value of property startMjd.
   *
   * @throws PropertyVetoException
   */
  public void setStartMjd(double startMjd) throws IllegalArgumentException {
      double oldStartMjd = this.startMjd;
      if (startMjd == oldStartMjd) return;
      if (startMjd < Time.Y1970) throw new IllegalArgumentException("Start time could not be earlier then 1970");
      this.startMjd = startMjd;
      //firePropertyChange("startMjd", new Double(oldStartMjd), new Double(startMjd));
  }
  
  public double getIntervalMjd() {
    return stopMjd - startMjd;
  }
  
  public void setIntervalMjd(double intervalMjd) throws IllegalArgumentException {
      double oldIntervalMjd = getIntervalMjd();
      if (intervalMjd == oldIntervalMjd) return;
      if (intervalMjd <= 0) throw new IllegalArgumentException("Interval "+intervalMjd+" <= 0");
      setStopMjd(startMjd + intervalMjd);
      //firePropertyChange("intervalMjd", new Double(oldIntervalMjd), new Double(intervalMjd));
  }
  
  /** Getter for property stopMjd.
   * @return Value of property stopMjd.
   */
  public double getStopMjd() {
    return stopMjd;
  }
  /** Setter for property stopMjd.
   * @param stopMjd New value of property stopMjd.
   *
   * @throws PropertyVetoException
   */
  public void setStopMjd(double stopMjd) throws IllegalArgumentException {
        this.stopMjd = stopMjd;
  }
  
  
  /** Equals function. Doesn't care about currentMjd */
  public boolean equals(ovt.interfaces.TimePeriodSource ts) {
    if (ts.getStartMjd() == getStartMjd()  &&
        ts.getStopMjd() == getStopMjd()) return true;
    else return false;
  }

  /** returns true if the object intersects with <CODE>ts</CODE> */
  public boolean intersectsWith(ovt.interfaces.TimePeriodSource ts) {
      double start1 = getStartMjd();
      double stop1  = getStopMjd();
      double start2 = ts.getStartMjd();
      double stop2  = ts.getStopMjd();
      if ( start2 < stop1  && stop2 > start1) return true;
      return false;
  }
  

}
