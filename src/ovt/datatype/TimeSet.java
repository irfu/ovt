/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/TimeSet.java,v $
  Date:      $Date: 2003/09/28 17:52:38 $
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
 * TimeSet.java
 *
 * Created on February 28, 2000, 11:26 AM
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
 * @author  mykola
 * @version 
 */
public class TimeSet extends OVTObject implements ovt.interfaces.TimeSetSource {
  
  private double startMjd;
  private double intervalMjd;
  private double stepMjd;
  private double currentMjd = -1;
  private int currentMjdIndex = 0;
  
  /** Creates new TimeSet */
  public TimeSet() {}
  
  /** Creates new TimeSet */
  public TimeSet(double startMjd, double intervalMjd, double stepMjd) {
    this.startMjd = startMjd;
    this.intervalMjd = intervalMjd;
    this.stepMjd = stepMjd;
    this.currentMjd = startMjd;
    this.currentMjdIndex = 0;
  }
  
  public TimeSet(double startMjd, double intervalMjd, double stepMjd, double currentMjd) {
    this.startMjd = startMjd;
    this.intervalMjd = intervalMjd;
    this.stepMjd = stepMjd;
    this.currentMjd = currentMjd;
    this.currentMjdIndex = indexOf(currentMjd);
  }
  
  /** Creates new TimeSet */
  public TimeSet(TimeSetSource timeSet) {
    set(timeSet);
  }
  
  public void set(TimeSetSource timeSet) {
    setStartMjd(timeSet.getStartMjd());
    setIntervalMjd(timeSet.getIntervalMjd());
    setStepMjd(timeSet.getStepMjd());
    setCurrentMjd(timeSet.getCurrentMjd());
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
      firePropertyChange("startMjd", new Double(oldStartMjd), new Double(startMjd));
  }
  
  public double getIntervalMjd() {
    return intervalMjd;
  }
  
  public void setIntervalMjd(double intervalMjd) throws IllegalArgumentException {
      double oldIntervalMjd = this.intervalMjd;
      if (intervalMjd == oldIntervalMjd) return;
      if (intervalMjd <= 0) throw new IllegalArgumentException("Interval "+intervalMjd+" <= 0");
      this.intervalMjd = intervalMjd;
      firePropertyChange("intervalMjd", new Double(oldIntervalMjd), new Double(intervalMjd));
  }
  
  /** Getter for property stopMjd.
   * @return Value of property stopMjd.
   */
  public double getStopMjd() {
    return startMjd + intervalMjd;
  }
  /** Setter for property stopMjd.
   * @param stopMjd New value of property stopMjd.
   *
   * @throws PropertyVetoException
   */
  public void setStopMjd(double stopMjd) throws IllegalArgumentException {
        setIntervalMjd(stopMjd - startMjd);
  }
  
  /** Getter for property stepMjd.
   * @return Value of property stepMjd.
   */
  public double getStepMjd() {
    return stepMjd;
  }
  /** Setter for property stepMjd.
   * @param stepMjd New value of property stepMjd.
   *
   * @throws PropertyVetoException
   */
  public void setStepMjd(double stepMjd) throws IllegalArgumentException {
      double oldStepMjd = this.stepMjd;
      this.stepMjd = stepMjd;
      firePropertyChange("stepMjd", new Double(oldStepMjd), new Double(stepMjd));
  }
  
  public void setCurrentMjdIndex(int index) {
    this.currentMjdIndex = index;
    this.currentMjd = -1; // it means it will be calculated when getCurrentMjd() is executed
    firePropertyChange("currentMjd", null, null);
    firePropertyChange("currentMjdIndex", null, null);
  }
  
  public int getCurrentMjdIndex() {
    return currentMjdIndex;
  }
  
  public int getMaxCurrentMjdIndex() {
    return (int)(intervalMjd/stepMjd);
  }

  
  /** Getter for property currentMjd.
   * @return Value of property currentMjd.
   */
  public double getCurrentMjd() {
      if (currentMjd == -1) 
          currentMjd = get(currentMjdIndex);
    return currentMjd;
  }
  /** Setter for property currentMjd.
   * @param currentMjd New value of property currentMjd.
   *
   * @throws PropertyVetoException
   */
  public void setCurrentMjd(double currentMjd) throws IllegalArgumentException {
    double oldCurrentMjd = getCurrentMjd();
    /*
    if (currentMjd > getStopMjd() + 2e-10 ) {
        String reason = "Time is greater, than stop time\n(" +
                  Time.toString(currentMjd)+" > " + 
                  Time.toString(getStopMjd())+") ";// +currentMjd+" "+getStopMjd();
        throw new IllegalArgumentException(reason);
      }
      
      if (currentMjd < getStartMjd()) {
        String reason = "Time is less, than start time\n(" +
                  Time.toString(currentMjd)+" > " + 
                  Time.toString(getStartMjd())+")";
        throw new IllegalArgumentException(reason);
      } */
    
    this.currentMjd = currentMjd;//getClosestFor(currentMjd);
    this.currentMjdIndex = indexOf(currentMjd);
    
    firePropertyChange("currentMjd", new Double (oldCurrentMjd), new Double (currentMjd));
    firePropertyChange("currentMjdIndex", null, null);
  }
  
  
  public void adjustCurrentMjd() {
    double oldCurrentMjd = getCurrentMjd();
    this.currentMjd = getClosestFor(oldCurrentMjd);
    this.currentMjdIndex = indexOf(currentMjd);
    firePropertyChange("currentMjd", new Double (oldCurrentMjd), new Double (currentMjd));
    firePropertyChange("currentMjdIndex", null, null);
  }
  
  /** returns time, from wich is possible to start 
   *
   */
  public double getStartFor(double mjd) {
    double start = getStartMjd();
    double step = getStepMjd();
    if (mjd <= start) return start;
    else {
      // mjd > startMjd
      int n = (int)((mjd - start) / step);
      return start + step * (n + 1);
    }
  }
 
  
  
  public double get(int i) {
    if (i >= getNumberOfValues()) throw new IndexOutOfBoundsException(""+i+" [" + getNumberOfValues() + "]");
    return getStartMjd() + getStepMjd()*i;
  }

  /** Returns the position of <CODE>mjd</CODE> in this time set */
  public int indexOf(double mjd) {
    if (!contains(mjd)) throw new IndexOutOfBoundsException(""+new Time(mjd)+" is not between start and stop time.");
    return (int)Math.round((mjd - startMjd)/stepMjd);
  }
  
  /** @return values of time (mjd) from start to stop */
  public double[] getValues() {
    int nOfValues = getNumberOfValues();
    double[] res = new double[nOfValues];
    for (int i=0; i<nOfValues; i++) 
      res[i] = get(i);
    return res;
  }

  public int getNumberOfValues() {
    return (int)(intervalMjd/stepMjd + 1);
  }

  public void adjustInterval() {
      double newInterval = get(getNumberOfValues() - 1) - startMjd;
      // check if interval is a little bit less, then the last posible point
      // enlarge it in this case.
      if (newInterval + stepMjd < getStopMjd() + 2e-10) 
          intervalMjd = newInterval + 2e-10;
      else
          intervalMjd = newInterval;
  }
  
  public boolean contains(double mjd) {
    return ((mjd >= startMjd) && (mjd <= getStopMjd()));
  }
  
  public boolean contains(TimeSet ts) {
    return ((ts.getStartMjd() >= startMjd) && (ts.getStopMjd() <= getStopMjd()));
  }
  
  public boolean overlaps(TimeSet ts) {
    return (ts.getStartMjd() < getStopMjd());
  }
  

  public double getClosestFor(double mjd) throws IllegalArgumentException {
    if (contains(mjd)) 
        return get(indexOf(mjd));
    else return startMjd;
  }
  
  public Object clone() {
    // is buggy.. later, later... 
    TimeSet ts = new TimeSet(startMjd, intervalMjd, stepMjd, currentMjd);
    return ts;
  }
  
  public String toString() {
    return "start="+Time.toString(startMjd)+
           ", interval="+new Interval(intervalMjd)+
           ", step=" + new Interval(stepMjd)+
           ", current=" + new Time(currentMjd);
  }
  
  public void print(){
    System.out.println("start="+startMjd+
           ", interval="+intervalMjd+
           ", step=" + stepMjd+
           ", current=" + currentMjd);
  }
  
  /** Equals function. Doesn't care about currentMjd */
  public boolean equals(Object obj) {
      double eps = 1.e-9;
      if (obj instanceof TimeSet) {
          TimeSet ts = (TimeSet)obj;
          if (Math.abs(ts.getStartMjd() - getStartMjd()) < eps  &&
              Math.abs(ts.getIntervalMjd() - getIntervalMjd()) < eps  &&
              Math.abs(ts.getStepMjd() - getStepMjd()) < eps) 
                  return true;
      } 
      return false;
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
  
  public static void main(String[] args) {
        TimeSet ts = new TimeSet(Time.Y2000, 1, new Interval(0, 1, 0).getMjd());
        for (int i=0; i<ts.getNumberOfValues(); i++) {
            System.out.println(""+i+" : "+ts.indexOf(ts.get(i)));
        }
        System.out.println("Y2000"+new Time(Time.Y2000));
  }
  
  
}
