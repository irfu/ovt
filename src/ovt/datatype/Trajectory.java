/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Trajectory.java,v $
  Date:      $Date: 2003/09/28 17:52:39 $
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

package ovt.datatype;

import ovt.object.*;
import ovt.util.*;

import vtk.vtkCardinalSpline;

import java.util.*;
import java.lang.*;

public class Trajectory extends Timetable  {
   /**  holds {int CoordSystem, vtkCardinalSpline[3]} pairs */
   protected Hashtable splines = new Hashtable();
   

  public Trajectory() {
    super();
  }
  
  public void put(TrajectoryPoint p) { 
    put(p.mjd, p);
  }

  /** returns the position interpolated by splines */
  public double[] evaluate(double mjd, int cs) throws IllegalArgumentException {
    if (size() == 0) throw new IllegalArgumentException("Trajectory size = 0");
    double first_mjd = ((TrajectoryPoint)firstElement()).mjd;
    vtkCardinalSpline[] sp = (vtkCardinalSpline[])splines.get(new Integer(cs));
    int i=0;
    if (sp == null) { // fill it
        sp = new vtkCardinalSpline[3];
        for (int j=0; j<3; j++) sp[j] = new vtkCardinalSpline();
        Enumeration e = elements();
        TrajectoryPoint p;
        while (e.hasMoreElements()) {
            p = (TrajectoryPoint)e.nextElement();
            for (i=0; i<3; i++) sp[i].AddPoint((p.mjd - first_mjd) / first_mjd, p.get(cs)[i]);
        }
        splines.put(new Integer(cs), sp);
    }
    double[] res = new double[3];
    for (i=0; i<3; i++) res[i] = sp[i].Evaluate((mjd - first_mjd) / first_mjd);
    return res;
  }
  
  public TrajectoryPoint get(double mjd) { 
    return (TrajectoryPoint)getElement(mjd); 
  }
  
  public void clear() {
    super.clear();
    Enumeration e = splines.elements();
    vtkCardinalSpline[] sp;
    while (e.hasMoreElements()) {
            sp = (vtkCardinalSpline[])e.nextElement();
            for (int i=0; i<3; i++) sp[i].RemoveAllPoints();
    }
    splines.clear();
  }
  
/** Returns the normalized vector which is perpendicular to the trajectory
 * and the orbit plane
 */
public double[] getOrbitPlaneNormal(double mjd, int cs) {
    double mjd_a, mjd_b;
    mjd_a = mjd - Time.DAYS_IN_SECOND;
    mjd_b = mjd + Time.DAYS_IN_SECOND;
    
    double[] r_a = evaluate(mjd_a, cs);
    double[] r_b = evaluate(mjd_b, cs);
            
    // compute the normalized vector, orthogonal to a and b
    return Vect.crossn(r_a, r_b);
}

/** Returns the normalized vector which is perpendicular to the trajectory
 * and lays in the orbit plane.
 */
public double[] getNormal(double mjd, int cs) {
    double mjd_a, mjd_b;
    mjd_a = mjd - Time.DAYS_IN_SECOND;
    mjd_b = mjd + Time.DAYS_IN_SECOND;
    
    double[] r_a = evaluate(mjd_a, cs);
    double[] r_b = evaluate(mjd_b, cs);
            
    double[] a_minus_b = Vect.sub(r_a, r_b); // a-b
    double[] orbitPlaneVect = Vect.cross(a_minus_b, r_a);
    return Vect.crossn(a_minus_b, orbitPlaneVect);
}
  
  
}
