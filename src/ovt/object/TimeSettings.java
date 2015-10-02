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
import ovt.beans.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;



/** 
 *
 * @author  mykola
 * @version 
 */
public class TimeSettings extends BasicObject implements ovt.interfaces.TimeSetSource { // implements java.io.Serializable 

  private final int NBR_OF_STEPS_BEFORE_WARNING = 24*60+100; // Number of minutes per day (plus some for rounding errors).
  private final double INITIAL_START_MJD = Time.getMjd("2012-12-30 00:00:00");   // Initial value used in constructor.
  private final double INITIAL_INTERVAL_MJD = 1;                                 // Initial value used in constructor.
  private final double INITIAL_STEP_MJD = MinutesAndSeconds.getInDays("10:00");     // Initial value used in constructor.
  private final double INITIAL_CURRENT_MJD = Time.getMjd("2012-12-30 00:00:00");  // Initial value used in constructor.

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
    
    timeSet = new TimeSet(INITIAL_START_MJD, INITIAL_INTERVAL_MJD, INITIAL_STEP_MJD, INITIAL_CURRENT_MJD);

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
    if (ts.getStepMjd() > ts.getIntervalMjd()/2.) {
        throw new IllegalArgumentException("Step is greater than half the specified time interval.");
    }
    
    final int nbrOfSteps = ts.getNumberOfValues();
    if (nbrOfSteps > NBR_OF_STEPS_BEFORE_WARNING)
        getCore().sendWarningMessage(
                "Warning", "Using a very high number of steps ("+nbrOfSteps+")."
                + " This may slow down the application."
                //+ " (Warning triggered when exceeding "+NBR_OF_STEPS_BEFORE_WARNING+" steps.)"
        );
    
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
   */
  /*public void setStepMjd(double stepMjd) throws IllegalArgumentException {
      double oldStepMjd = this.stepMjd;
      if ( intervalMjd / stepMjd < 1) throw new IllegalArgumentException("Number of steps is less then 1");
      // check number of points
      if ( intervalMjd / stepMjd > 400) getCore().sendWarningMessage("Warning", "Number of steps exceeds 400");
      this.stepMjd = stepMjd;
      firePropertyChange("stepMjd", new Double(oldStepMjd), new Double(stepMjd));
  }//*/
  
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

