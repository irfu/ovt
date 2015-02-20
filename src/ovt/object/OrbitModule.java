/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/OrbitModule.java,v $
  Date:      $Date: 2005/12/13 16:46:42 $
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

import vtk.*;

import ovt.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
 * Visualizes satellites orbit.
 * { @see ovt.datatypes.Sat#getOrbitModule }. 
 * @author Mykola Khotyaintsev
 * @see ovt.datatypes.AbstractSatModule
 */
public class OrbitModule extends SingleActorSatModule {

  public OrbitModule(Sat satellite) { 
    super(satellite, "Orbit", "images/orbit.gif");
    setColor(Color.red);
  }
  
  public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
      invalidate();
      if (isVisible()) {
        hide();
        show();
      }
    }
  }
  
  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    //System.out.println("Coord system changed.. I see...");
    invalidate();
    if (isVisible()) {
      hide();
      show();
    }
  }

  
  public void validate() {
    
      int resolution = 10; // number of interpolated points between 2 normal points
      vtkPolyData profile = new vtkPolyData();
      vtkCellArray lines = new vtkCellArray();
      TrajectoryPoint tr;
      vtkCardinalSpline aSplineX = new vtkCardinalSpline();
      vtkCardinalSpline aSplineY = new vtkCardinalSpline();
      vtkCardinalSpline aSplineZ = new vtkCardinalSpline();

      double i = 0;
      double numberOfPoints = getTrajectory().size();
      Enumeration e = getTrajectory().elements();
      while (e.hasMoreElements()) {
        tr = (TrajectoryPoint)e.nextElement();
        double[] a = tr.get(getCS());
        aSplineX.AddPoint(i, a[0]);
        aSplineY.AddPoint(i, a[1]);
        aSplineZ.AddPoint(i, a[2]);
        i++;
      }
      
      vtkPoints points = new vtkPoints();
      
      double x,y,z;
      for (i=0; i<numberOfPoints; i+=1.0/resolution) {
        x = aSplineX.Evaluate(i);
        y = aSplineY.Evaluate(i);
        z = aSplineZ.Evaluate(i);
        points.InsertNextPoint(x, y, z);
      }
      
      lines.InsertNextCell(points.GetNumberOfPoints());
		
      for(int j=0; j<points.GetNumberOfPoints(); j++) 
		lines.InsertCellPoint(j);

	
	profile.SetPoints(points);
	profile.SetLines(lines);
	//profile.GetPointData().SetScalars(scalars);

	//vtkLookupTable lut  = new vtkLookupTable();
	//lut.SetHueRange(0.6667, 0);
	
	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
	mapper.SetInputData(profile);
	//mapper.SetScalarModeToUsePointData();
    //mapper.ScalarVisibilityOn();
	//mapper.SetScalarRange(minVelocity, maxVelocity);
	//mapper.SetLookupTable(lut);
	
      actor = new vtkActor();
        actor.SetMapper(mapper);
        float[] rgb = Utils.getRGB(getColor());
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
      super.validate();
  }

}

