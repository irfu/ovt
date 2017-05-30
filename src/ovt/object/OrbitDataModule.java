/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/OrbitDataModule.java,v $
Date:      $Date: 2003/09/28 17:52:50 $
Version:   $Revision: 2.11 $


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

/*
 * OrbitDataModule.java
 *
 * Created on November 26, 2000, 5:50 PM
 */


import vtk.*;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author  ko
 * @version
 */
public class OrbitDataModule extends SingleActorSatModule {
    private static final int TIME  = 0;
    private static final int VALUE = 1;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    DataModule dataModule;
    double height = 1.;
    /** Holds value of property opacity. */
    private double opacity = 0.6;
    /** Holds x,y,z points of orbit */
    private double[][] r;
    /** Holds normal vectors */
    private double[][] n;
    /** */
    private int firstIndex = 0;
    int lastIndex = 0;
    /** Holds normal vectors */
    private vtkPoints points = new vtkPoints();

    /** Holds value of property reversed. */
    private boolean reversed = false;

public OrbitDataModule(DataModule module) {
  super(module.getSat(),  "On Orbit", "images/orbit.gif");
  this.dataModule = module;
  // listen to parent's "endabled" state
  // propertyChange method is implemented in VisualObject
  dataModule.addPropertyChangeListener("enabled", this);
}

/*
public void setVisible(boolean visible) {
  Log.log("----------------------------------");
  Log.log("----------setVisible("+visible+")------------");
  if (!visible) throw new IllegalArgumentException("Nu Nu :-)");
  super.setVisible(visible);
  Log.log("---  isVisible()="+isVisible()+"------");
  Log.log("--- this.visible="+visible+" -----");
  Log.log("----------------------------------");
}*/

private double[][] getData() {
    return dataModule.getData();
}

private TimePeriod getDataTimePeriod() {
    return dataModule.getDataTimePeriod();
}


protected void validate() {

   double[][] data = getData();
   if (data == null) return;

   double min = dataModule.getMin();
   double max = dataModule.getMax();

   int firstIndex = dataModule.getFirstIndex();
   int lastIndex  = dataModule.getLastIndex();
   Log.log("first="+firstIndex+" last="+lastIndex + "data.length="+data.length);
   int dateCount = lastIndex - firstIndex + 1;

   Fieldline[][] fl = new Fieldline[dateCount][2];

   double[] r2, r3;
   double y = 1, mjd;

   points.Reset(); // reset to empty state without freeing memory
   points.Squeeze(); // free memory

   vtkFloatArray scalars = new vtkFloatArray();
   double value;

   r = new double[dateCount][3];
   n = new double[dateCount][3];

   for (int i=0; i<dateCount; i++) {
       Log.log(i+" ["+dateCount+"] -> "+(firstIndex+i));
       mjd = data[firstIndex+i][TIME];
       r[i] = getPosition(mjd);
       n[i] = sat.getTrajectoryNormal(mjd);
   }

   for (int i=0; i<dateCount-1; i++) {

       if (max >=0 && min <= 0) value = data[i + firstIndex][VALUE] / (max - min) * height;
       else value = (data[i + firstIndex][VALUE] - min) / (max - min) * height;
       if (isReversed()) value *= -1.;

       r2 = Vect.add(r[i], Vect.multiply(n[i], value));

       r3 = Vect.add(r[i+1], Vect.multiply(n[i+1], value));

       //System.out.println("r="+Vect.toString(Vect.norm(r)));
       //System.out.println("n="+Vect.toString(Vect.norm(n)));
       //System.out.println("Value=" + value + " orig=" + data[i][VALUE]);

       // orbit points (0 - dateCount)
       points.InsertPoint(i, r[i][X], r[i][Y], r[i][Z]); // add orbit point
       // data1 points (dateCount - 2*dateCount)
       points.InsertPoint(dateCount + i, r2[X], r2[Y], r2[Z]); // add data point 1
       // data2 points (2*dateCount - 3*dateCount)
       points.InsertPoint(2*dateCount + i, r3[X], r3[Y], r3[Z]); // add data point 2
       // add orbit points
       scalars.InsertNextValue(data[i + firstIndex][VALUE]);

   }
   // add last orbit point
   points.InsertPoint(lastIndex-firstIndex, r[dateCount-1][X], r[dateCount-1][Y], r[dateCount-1][Z]); // add orbit point

   vtkCellArray polys = new vtkCellArray();

   for (int i=0; i<dateCount - 1; i++) {
       polys.InsertNextCell(4);
       polys.InsertCellPoint(i);
       polys.InsertCellPoint(i+1);
       polys.InsertCellPoint(2*dateCount + i);
       polys.InsertCellPoint(dateCount + i);
   }

   vtkPolyData profile = new vtkPolyData();
        profile.SetPoints(points);
        profile.SetPolys(polys);
        //profile.SetLines(polys);
        //profile.GetPointData().SetScalars(scalars);
        profile.GetCellData().SetScalars(scalars);

    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        mapper.SetInputData(profile);
        mapper.SetScalarModeToUseCellData();
        mapper.ScalarVisibilityOn();
        mapper.SetScalarRange(min, max);
        mapper.SetLookupTable(dataModule.getLookupTable());

    //Log.log("Number of colours in lookup table="+dataModule.getLookupTable().GetNumberOfColors());


    actor = new vtkActor();
        actor.SetMapper(mapper);
        actor.GetProperty().SetOpacity(this.opacity);

    super.validate();
}

private void updateDataPoints() {
    double min = dataModule.getMin();
    double max = dataModule.getMax();
    int firstIndex = dataModule.getFirstIndex();
    int lastIndex  = dataModule.getLastIndex();
    int dateCount = lastIndex - firstIndex + 1;
    double[][] data = dataModule.getData();
    // misc variables
    double value, r2[], r3[];
    for (int i=0; i<dateCount-1; i++) {

        if (max >=0 && min <= 0) value = data[i + firstIndex][VALUE] / (max - min) * height;
        else value = (data[i + firstIndex][VALUE] - min) / (max - min) * height;

        if (isReversed()) value *= -1.;

        r2 = Vect.add(r[i], Vect.multiply(n[i], value));

        r3 = Vect.add(r[i+1], Vect.multiply(n[i+1], value));

        points.SetPoint(dateCount + i, r2[X], r2[Y], r2[Z]); // change data point 1

        points.SetPoint(2*dateCount + i, r3[X], r3[Y], r3[Z]); // change data point 2

    }
    // mark points as modified
    points.Modified();
}


/** Getter for property height.
 * @return Value of property height.
 */
public double getHeight() {
    return height;
}

/** Setter for property height.
 * @param height New value of property height.
 */
public void setHeight(double height) {
    double oldHeight = this.height;
    this.height = height;

    if (actor != null) updateDataPoints();

    firePropertyChange ("height", new Double (oldHeight), new Double (height));
}

/** Getter for property opacity.
 * @return Value of property opacity.
 */
public double getOpacity() {
    return opacity;
}

/** Setter for property opacity.
 * @param opacity New value of property opacity.
 */
public void setOpacity(double opacity) {
    double oldOpacity = this.opacity;
    this.opacity = opacity;
    if (actor != null) actor.GetProperty().SetOpacity(opacity);
    firePropertyChange ("opacity", new Double (oldOpacity), new Double (opacity));
}

public Descriptors getDescriptors() {
    if ( descriptors == null) {
        try {
            descriptors = super.getDescriptors();
            descriptors.removeDescriptor("color");

            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("opacity", this);
            pd.setLabel("Opacity");
            pd.setDisplayName("Data on orbit opacity");
            SliderPropertyEditor sliderEditor = new SliderPropertyEditor(pd, 0., 1., 0.05,
                new double[]{0,.25,.5,.75,1}, new String[]{"0%","25%","50%","75%","100%"});
            addPropertyChangeListener("opacity", sliderEditor);
            sliderEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(new WindowedPropertyEditor(sliderEditor, getCore().getXYZWin()));
            descriptors.put(pd);

            pd = new BasicPropertyDescriptor("height", this);
            pd.setLabel("Height");
            pd.setDisplayName("Data on orbit height");
            ExponentialSliderPropertyEditor expSliderEd = new ExponentialSliderPropertyEditor(pd,
                1./8., 8., new double[]{1./8.,1./2., 1, 2, 8});
            addPropertyChangeListener("opacity", expSliderEd);
            expSliderEd.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(new WindowedPropertyEditor(expSliderEd, getCore().getXYZWin()));
            descriptors.put(pd);

            pd = new BasicPropertyDescriptor("reversed", this);
            pd.setLabel("Reversed");
            BooleanEditor ed = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
            addPropertyChangeListener("reversed", ed);
            pd.setPropertyEditor(ed);
            descriptors.put(pd);

        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString()); System.exit(0);
        }
    }
    return descriptors;
}


/** Getter for property reversed.
 * @return Value of property reversed.
 */
public boolean isReversed() {
    return reversed;
}

/** Setter for property reversed.
 * @param reversed New value of property reversed.
 */
public void setReversed(boolean reversed) {
    boolean oldReversed = this.reversed;
    if (oldReversed == reversed) return; // nothing has changed
    this.reversed = reversed;
    if (actor != null) updateDataPoints();
    firePropertyChange ("reversed", new Boolean (oldReversed), new Boolean (reversed));
}

public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
        if (!getDataTimePeriod().intersectsWith(getTimeSet())) {
            invalidate();
            if (isVisible()) setVisible(false);
            setEnabled(false);
        } else setEnabled(true);
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

/** Method overriden to unregister this as a propertyChange listener and to dispose customizer */
public void dispose() {
    setVisible(false);
   // if (customizer != null) customizer.dispose();
    super.dispose();
}

}

/*

    vtkRibbonFilter ribbon = new vtkRibbonFilter();
        ribbon.SetInput(profile);
        ribbon.VaryWidthOn();
        ribbon.SetWidthFactor(0.005);
        ribbon.SetDefaultNormal(0, 1, 0);
        ribbon.UseDefaultNormalOn();

    vtkLinearExtrusionFilter extrusion = new vtkLinearExtrusionFilter();
         extrusion.SetInput(ribbon.GetOutput());
         extrusion.SetVector(0, 1, 0);
         extrusion.SetExtrusionType(1);
         extrusion.SetScaleFactor(0.05);

    vtkTubeFilter tube = new vtkTubeFilter();
        tube.SetInput(profile);
        tube.SetNumberOfSides(8);
        tube.SetRadius(0.05);
        tube.SetRadiusFactor(5);
        //tube.SetRadiusFactor(10000);
        tube.SetVaryRadiusToVaryRadiusByScalar();
 */
