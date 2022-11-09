/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/ActorUtils.java,v $
  Date:      $Date: 2003/09/28 17:52:54 $
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * ActorUtils.java
 *
 * Created on March 17, 2000, 2:56 PM
 */

package ovt.util;

import ovt.mag.*;
import ovt.datatype.*;

import vtk.*;

import java.util.*;
/**
 *
 * @author  root
 * @version
 */
public class ActorUtils extends Object {

  /** Creates new ActorUtils */
  public ActorUtils() {
  }

 /** USES SPOLINES!!!!!!!!!
  * Make fieldline actor in the coordinate system, specified by gsm_cs_trans_matrix
  * @param fl Field Line
  * @param gsm_cs_trans_matrix Transformation matrix from GSM to the CS you whant
  * to be native actor's coordinates
  * @return Value of property actor., Matrix3x3 gsm_cs_trans_matrix
  */
  public static vtkActor getActor(Fieldline fl) {
      //create actor
      vtkActor actor = new vtkActor();
      vtkPolyData profile = fl.getVTKPolyData();
      vtkPoints points = new vtkPoints();
      vtkFloatArray scalars = new vtkFloatArray();
      //added by kono
      vtkCardinalSpline splineX = new vtkCardinalSpline();
      vtkCardinalSpline splineY = new vtkCardinalSpline();
      vtkCardinalSpline splineZ = new vtkCardinalSpline();
      vtkCardinalSpline splineS = new vtkCardinalSpline();
      int resolution = 3; //for splining
      double i = 0;

      Enumeration e = fl.elements();
      MagPoint magPoint;
      double[] a;
      while (e.hasMoreElements()) {
        magPoint = (MagPoint)e.nextElement();
        //a = gsm_cs_trans_matrix.multiply(magPoint.gsm);
        a = (magPoint.gsm);
        splineX.AddPoint(i, a[0]);
        splineY.AddPoint(i, a[1]);
        splineZ.AddPoint(i, a[2]);
        splineS.AddPoint(i, Vect.absv(magPoint.bv));
        ++i;
        //points.InsertNextPoint(a[0],a[1],a[2]);
	//scalars.InsertNextScalar(Vect.absv(magPoint.bv));
		//k++;
		//System.out.println(k+" "+a[0]+"\t"+a[1]+"\t"+a[2]);
      }

      for(i=0;i<fl.size();i+=1.0/resolution){
        points.InsertNextPoint(splineX.Evaluate(i),splineY.Evaluate(i),splineZ.Evaluate(i));
	scalars.InsertNextValue(splineS.Evaluate(i));
      }

      //System.out.println("fl.size = "+fl.size());
      vtkCellArray lines = new vtkCellArray();
      lines.InsertNextCell(fl.size()*resolution);

      for(i=0; i<fl.size()*resolution; i++)
		lines.InsertCellPoint((int)i);

      profile.SetPoints(points);
      profile.SetLines(lines);
      profile.GetPointData().SetScalars(scalars);

      vtkLogLookupTable lut  = new vtkLogLookupTable();
      lut.SetHueRange(0.6667, 0);

      vtkPolyDataMapper mapper = new vtkPolyDataMapper();
      mapper.SetInputData(profile);
      mapper.SetScalarModeToUsePointData();
      mapper.ScalarVisibilityOn();
      mapper.SetScalarRange(MagProps.BMIN, MagProps.BMAX);
      mapper.SetLookupTable(lut);

      actor.SetMapper(mapper);
    return actor;
  }

}
