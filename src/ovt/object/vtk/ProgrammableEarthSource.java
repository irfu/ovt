/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/vtk/ProgrammableEarthSource.java,v $
  Date:      $Date: 2003/09/28 17:52:53 $
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * ProgramableEarthSource.java
 *
 * Created on April 1, 2000, 2:31 PM
 */

package ovt.object.vtk;

import vtk.*;

/**
 *
 * @author  ko
 * @version
 */
public class ProgrammableEarthSource {

  /** Holds value of property meridians. */
  private int meridians = 24;
  /** Holds value of property parallels. */
  private int parallels = 18;
  /** Holds value of property phiResolution. */
  private int phiResolution = 5;
  /** Holds value of property thetaResolution. */
  private int thetaResolution = 5;
  /** Holds value of property radius. */
  private double radius = 1;
  /** Creates new ProgramableEarthSource */
  vtkPolyData polyData = new vtkPolyData();

  public ProgrammableEarthSource() {
    super();
  }

  protected void Execute() {

    vtkPoints points = new vtkPoints();
    vtkFloatArray scalars = new vtkFloatArray();
    double dPhi = 2.*Math.PI/getMeridians();
    double dTheta = Math.PI/getParallels();
    double ddPhi = dPhi/getPhiResolution();
    double ddTheta = dTheta/getThetaResolution();

    double phi, theta, x, y, z;
    int meridian, parallel, mm, pp;
    boolean visible;

    for (parallel=0; parallel<getParallels(); parallel++) {
       for (pp=0; pp<getThetaResolution(); pp++) {

        theta = parallel * dTheta + pp*ddTheta;
        z = getRadius() * Math.cos(theta);

        for (meridian=0; meridian<getMeridians(); meridian++) {
           for (mm=0; mm<getPhiResolution(); mm++) {
            phi = meridian * dPhi + mm*ddPhi;
            x = getRadius() * Math.sin(theta) * Math.cos(phi);
            y = getRadius() * Math.sin(theta) * Math.sin(phi);
            points.InsertNextPoint(x, y, z);
            visible = (mm==0) || (pp==0);
            scalars.InsertNextValue((visible) ? 1 : 0);
          }
        }
      }
    }

    vtkCellArray cells = new vtkCellArray();
    cells.InsertNextCell(points.GetNumberOfPoints());
    for(int j=0; j<points.GetNumberOfPoints(); j++)
      cells.InsertCellPoint(j);

    int phiRes = getPhiResolution()*getMeridians();
    int thetaRes = getThetaResolution()*getParallels();

    vtkStructuredGrid sgrid = new vtkStructuredGrid();
      sgrid.SetDimensions(thetaRes, phiRes ,1);
      sgrid.SetPoints(points);
      sgrid.GetPointData().SetScalars(scalars);

    vtkStructuredGridGeometryFilter gfilter = new vtkStructuredGridGeometryFilter();
      gfilter.SetInputData(sgrid);
      gfilter.SetExtent(0, thetaRes, 0, phiRes, 0, 0);

    polyData = gfilter.GetOutput();


    /*polyData.GetPolyDataOutput().SetPoints(points);
    polyData.GetPolyDataOutput().SetLines(cells);*/
  }

  public vtkPolyData GetOutput() {
    Execute();
    return polyData;
  }


  /** Getter for property meridians.
   * @return Value of property meridians.
   */
  public int getMeridians() {
    return meridians;
  }
  /** Setter for property meridians.
   * @param meridians New value of property meridians.
   */
  public void setMeridians(int meridians) {
    this.meridians = meridians;
  }
  /** Getter for property parallels.
   * @return Value of property parallels.
   */
  public int getParallels() {
    return parallels;
  }
  /** Setter for property parallels.
   * @param parallels New value of property parallels.
   */
  public void setParallels(int parallels) {
    this.parallels = parallels;
  }
  /** Getter for property phiResolution.
   * @return Value of property phiResolution.
   */
  public int getPhiResolution() {
    return phiResolution;
  }
  /** Setter for property phiResolution.
   * @param phiResolution New value of property phiResolution.
   */
  public void setPhiResolution(int phiResolution) {
    this.phiResolution = phiResolution;
  }
  /** Getter for property thetaResolution.
   * @return Value of property thetaResolution.
   */
  public int getThetaResolution() {
    return thetaResolution;
  }
  /** Setter for property thetaResolution.
   * @param thetaResolution New value of property thetaResolution.
   */
  public void setThetaResolution(int thetaResolution) {
    this.thetaResolution = thetaResolution;
  }
  /** Getter for property radius.
   * @return Value of property radius.
   */
  public double getRadius() {
    return radius;
  }
  /** Setter for property radius.
   * @param radius New value of property radius.
   */
  public void setRadius(double radius) {
    this.radius = radius;
  }
}
