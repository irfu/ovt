/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/MagTangentModule.java,v $
  Date:      $Date: 2006/03/21 12:22:22 $
  Version:   $Revision: 2.2 $


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

package ovt.object;

import vtk.*;

import ovt.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.model.bowshock.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
 * Visualizes foreshock magnetic tangent.
 * @author Mykola Khotyaintsev
 */
public class MagTangentModule extends SingleActorSatModule implements MagPropsChangeListener {

  private vtkPoints points = new vtkPoints();

  public MagTangentModule(Sat satellite) {
    super(satellite, "Magnetic tangent", "images/mag_tangent.gif");
    setColor(Color.red);
  }


  /** This method should be moved into a parent SingleActorSatModule */
  public void show() {
    super.show();
    rotate();
  }

  public void timeChanged(TimeEvent evt) {
    invalidate();
    if (isVisible()) {
      hide();
      show();
    }
  }

  public void magPropsChanged(MagPropsEvent evt) {
    invalidate();
    if (isVisible()) {
       hide();
       show();
    }
  }

  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
      if (isVisible()) rotate();
  }

  public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
    actor.SetUserMatrix(m3x3.getVTKMatrix());
  }

  public void validate() {

    if (actor == null) {
    	vtkPolyData profile = new vtkPolyData();
        vtkCellArray lines = new vtkCellArray();

        //vtkPoints points = new vtkPoints();
        // insert two points
        points.InsertNextPoint(0, 0, 0);
        points.InsertNextPoint(1, 0, 0);

       	lines.InsertNextCell(points.GetNumberOfPoints());

        for(int j=0; j<points.GetNumberOfPoints(); j++)
		lines.InsertCellPoint(j);


	profile.SetPoints(points);
	profile.SetLines(lines);
	//profile.GetPointData().SetScalars(scalars);

	vtkLookupTable lut  = new vtkLookupTable();
	lut.SetHueRange(0.6667, 0);

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
    }


    double[] imf = getMagProps().getIMF(getMjd());
    double swp = getMagProps().getSWP(getMjd());
    double machNumber = getMagProps().getMachNumber(getMjd());

    double x, y, z, x0;

    y = getPositionGSM()[1];
    z = getPositionGSM()[2];

    //x0 = Bowshock99Model.getMagTangentX0(imf, swp, machNumber, y, z);
    double[] tan = Bowshock99Model.getTangentPoint(y, z, imf, swp, machNumber);
    double[] imf_norm_L = Vect.norm(imf, FieldlineModule.LENGH_OF_IMF_LINE);

    double[] p1 = Vect.add(tan, imf_norm_L);
    double[] p2 = Vect.add(tan, Vect.multiply(imf_norm_L, -1));

    points.SetPoint(0, p1[0], p1[1], p1[2]);
    points.SetPoint(1, p2[0], p2[1], p2[2]);

    super.validate();
  }

}

