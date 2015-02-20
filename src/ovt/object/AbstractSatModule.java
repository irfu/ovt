/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/AbstractSatModule.java,v $
  Date:      $Date: 2006/03/21 12:16:34 $
  Version:   $Revision: 2.6 $


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

package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This is the base class for all SatModules, that should be added
 * to { @link ovt.datatypes.Sat }. 
 * @author Mykola Khotyaintsev
 * @see ovt.datatypes.Sat#addModule(AbstractSatModule)
 */

public class AbstractSatModule extends VisualObject
  implements CoordinateSystemChangeListener, TimeChangeListener, 
              MagPropsChangeListener {

protected Sat sat;
	
  /** Creates AbstractSatModule with name*/
  public AbstractSatModule(Sat sat, String name){ 
	super(sat.getCore(), name);
	this.sat = sat;
        setParent(sat);
  }

    /** Creates AbstractSatModule with name*/
  public AbstractSatModule(Sat sat, String name, String iconFileName){ 
	super(sat.getCore(), name, iconFileName);
	this.sat = sat;
        setParent(sat);
  }

    /** Creates AbstractSatModule with name*/
  public AbstractSatModule(Sat sat, String name, String iconFileName, boolean containsVisualChildren){ 
	super(sat.getCore(), name, iconFileName, containsVisualChildren);
	this.sat = sat;
        setParent(sat);
  }

  
  public Sat getSat() {
    return sat;
  }
  
  public TimeSet getTimeSet() {
    return sat.getTimeSet();
  }

  
  /** Returns current Sat's location */
  public TrajectoryPoint getTrajectoryPoint() {
    return sat.getTrajectoryPoint();
  }

  /** Returns Sat's position in current CS */
  public double[] getPosition() {
    return sat.getPosition();
  }
  
  /** Returns Sat's position in GSM CS */
  public double[] getPositionGSM() {
    return sat.getPositionGSM();
  }
  
  /** Returns current Sat's location */
  public double[] getPosition(double mjd) throws IllegalArgumentException {
    return sat.getPosition(mjd);
  }
  
  public FootprintCollection[] getMagFootprintCollection() {
    return sat.getMagFootprintCollection();
  }
  
  /** Returns <CODE>sat.getMagFootprints(mjd)</CODE> */
  public MagPoint[] getMagFootprints(double mjd) {
    return sat.getMagFootprints(mjd);
  }
  
  /** Returns true if the sat is in solar wind
   */
  public boolean isInSolarWind() {
    return sat.isInSolarWind();
  }
  
  /** Returns Sat's Trajectory */
  public Trajectory getTrajectory() 
	{ return sat.getTrajectory(); }
        
  public boolean canBeVisible()
        { return sat.canBeVisible(); }
      
  
  /** Tell children about CS change */
  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    Children children = getChildren();
    
    if (children != null) {
      Enumeration e = children.elements();
      while (e.hasMoreElements()) {
        try {
        ((CoordinateSystemChangeListener)(e.nextElement())).coordinateSystemChanged(evt);
        } catch (ClassCastException e2) {
          //Log.log("this module doesn't care about cs..");
        }
      }
    }
  }

  /** Tell children about time change */
  public void timeChanged(TimeEvent evt) {
    // Tell children about time change
    Children children = getChildren();
    if (children == null) return;
    Enumeration e = children.elements();
    if (e !=null) {
      while (e.hasMoreElements()) {
        try {
        ((TimeChangeListener)(e.nextElement())).timeChanged(evt);
        } catch (ClassCastException e2) {
          System.out.println("this module doesn't care about time..");
        }
      }
    }
  }

 
  
  public void magPropsChanged(MagPropsEvent evt) {
    // Tell children about time change
    Children children = getChildren();
    if (children !=null) {
      Enumeration e = children.elements();
      while (e.hasMoreElements()) {
        try {
          ((MagPropsChangeListener)(e.nextElement())).magPropsChanged(evt);
        } catch (ClassCastException e2) {}
      }
    }
  }

}
