/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/TimeSettings.java,v $
  Date:      $Date: 2006/03/21 12:13:59 $
  Version:   $Revision: 2.9 $


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
 * TimeSettings.java
 *
 * Created on February 28, 2000, 11:00 AM
 *
 */
 
package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;
import ovt.object.TimeSettingsCustomizer;

import java.beans.*;

import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;


/** 
 *
 * @author  mykola
 * @version 
 */
public class TimeSettings extends BasicObject implements ovt.interfaces.TimeSetSource { // implements java.io.Serializable 

  private final int NBR_OF_STEPS_BEFORE_WARNING = 400;

  /** Holds value of property customizerVisible. */
  private boolean customizerVisible = false;
    
//  public static final String PROP_TIME = "time";
//  public static final String PROP_CURRENT_MJD = "currentMjd";

  private TimeChangeSupport timeChangeSupport = new TimeChangeSupport(this);

  private TimeSet timeSet;
  
  private TimeSettingsCustomizer customizer;
  
  /** Creates new TimeSettings */
  public TimeSettings(OVTCore core) {
    super(core, "TimeSettings");
    showInTree(false);
    setParent(core); // to have a full name "OVT.TimeSettings"
    
    timeSet = new TimeSet(Time.getMjd("2002-01-01 00:00:00"), 1., 
        MinutesAndSeconds.getInDays("20:00"), Time.getMjd("2002-01-01 00:00:00"));

    if (!OVTCore.isServer()) customizer = new TimeSettingsCustomizer(this);
    
  }

  public void addTimeChangeListener (TimeChangeListener listener) {
    timeChangeSupport.addTimeChangeListener (listener);
  }

  public void removeTimeChangeListener (TimeChangeListener listener) {
    timeChangeSupport.removeTimeChangeListener (listener);
  }

  public void fireTimeSetChange() {
    timeChangeSupport.fireTimeChange(new TimeEvent(this, TimeEvent.TIME_SET, timeSet));
    //firePropertyChange("time", null, null);
  }
  
  public void fireCurrentMjdChange() {
    timeChangeSupport.fireTimeChange(new TimeEvent(this, TimeEvent.CURRENT_MJD, timeSet));
  }
  
  /** Sets time and fires time change... hmmm.. may be it is not needed (fire)?... */
  public void setTimeSet(TimeSet ts) throws IllegalArgumentException {
    //Log.log("->setTimeSet("+ts+")");
    if (ts.getStepMjd() > ts.getIntervalMjd()/2.)
        throw new IllegalArgumentException("Step is more than twice as large as the length of the time interval.");
    
    if ( ts.getIntervalMjd()/ts.getStepMjd() > NBR_OF_STEPS_BEFORE_WARNING) 
        getCore().sendWarningMessage("Warning", "Number of steps exceeds "+NBR_OF_STEPS_BEFORE_WARNING+".");
    
    ts.adjustInterval();
    ts.adjustCurrentMjd();
    
    this.timeSet = ts;
    fireTimeSetChange();
    firePropertyChange("time", null, null);
  }
  
  /** Should be used instead of get*Mjd methods. 
   * @return actual TimeSet of OVT including currentMjd 
   */
  public TimeSet getTimeSet() { 
      return timeSet; 
  }
  
  /** Getter for property startMjd.
   * @return Value of property startMjd.
   */
  public double getStartMjd() {
    return timeSet.getStartMjd();
  }

  
  public double getIntervalMjd() {
    return timeSet.getIntervalMjd();
  }
  
  
  /** Getter for property stopMjd.
   * @return Value of property stopMjd.
   */
  public double getStopMjd() {
    return getStartMjd() + getIntervalMjd();
  }
  
  /** Getter for property stepMjd.
   * @return Value of property stepMjd.
   */
  public double getStepMjd() {
    return timeSet.getStepMjd();
  }
  /** Setter for property stepMjd.
   * @param stepMjd New value of property stepMjd.
   *
   * @throws PropertyVetoException
   
  public void setStepMjd(double stepMjd) throws IllegalArgumentException {
      double oldStepMjd = this.stepMjd;
      if ( intervalMjd / stepMjd < 1) throw new IllegalArgumentException("Number of steps is less then 1");
      // check number of points
      if ( intervalMjd / stepMjd > 400) getCore().sendWarningMessage("Warning", "Number of steps exceeds 400");
      this.stepMjd = stepMjd;
      firePropertyChange("stepMjd", new Double(oldStepMjd), new Double(stepMjd));
  }*/
  
  /** Getter for property currentMjd.
   * @return Value of property currentMjd.
   */
  public double getCurrentMjd() {
    return timeSet.getCurrentMjd();
  }


  
  
  /** returns time, from wich is possible to start 
   *
   *
  public double getStartFor(double mjd) {
    double start = getStartMjd();
    double step = getStepMjd();
    if (mjd <= start) return start;
    else {
      // mjd > startMjd
      int n = (int)((mjd - start) / step);
      return start + step * (n + 1);
    }
  }*/
  
  /** Getter for property customizerVisible.
 * @return Value of property customizerVisible.
 */
public boolean isCustomizerVisible() {
    if (!OVTCore.isServer()) return customizer.isVisible();  
    else return false; 
}

/** Setter for property customizerVisible.
 * @param customizerVisible New value of property customizerVisible.
 */
public void setCustomizerVisible(boolean customizerVisible) {
  boolean oldCustomizerVisible = isCustomizerVisible();
  if (oldCustomizerVisible && customizerVisible) {
      if (!OVTCore.isServer()) customizer.toFront();
  }
  if (!OVTCore.isServer()) { 
        customizer.setVisible(customizerVisible);
        propertyChangeSupport.firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
  }
}

}

