/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/FieldlineDataModule.java,v $
Date:      $Date: 2003/09/28 17:52:47 $
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * FieldlineDataModule.java
 *
 * Created on November 26, 2000, 10:55 PM
 */

package ovt.object;

/**
 *
 * @author  ko
 * @version
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
public class FieldlineDataModule extends SingleActorSatModule {
    public final int TO_EARTH   = 0;
    public final int TO_EQUATOR = 1;
    private static final int TIME  = 0;
    private static final int VALUE = 1;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    DataModule dataModule;
    double height = 0.1;

    /** Holds value of property direction. */
    private int direction;

public FieldlineDataModule(DataModule module) {
  super(module.getSat(),  "On Fieldlines", "images/fieldline.gif");
  this.dataModule = module;
  // listen to parent's "endabled" state
  // propertyChange method is implemented in VisualObject
  dataModule.addPropertyChangeListener("enabled", this);
}

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


   int minNumOfPoints = Integer.MAX_VALUE;
   int maxNumOfPoints = 0;

   int[] indexes = dataModule.getFirstAndLastIndexes();
   int firstIndex = indexes[0];
   int lastIndex  = indexes[1];
   //Log.log("first="+firstIndex+" last="+lastIndex + "data.length="+data.length);
   int dateCount = lastIndex - firstIndex + 1;

   Fieldline[][] fl = new Fieldline[dateCount][2];

   double[] r = null, r2, r3;
   double y = 1, mjd;
   for (int i=firstIndex; i<=lastIndex; i++) {
      mjd = data[i][TIME];

      r = getPosition(mjd); // GSM!!!
      fl[i] = makeFieldlines(mjd, r);
      //Log.log(i+" "+new Time(mjd));
      //Log.log("fl0.size=" + fl[i][0].size() + " fl1.size=" + fl[i][1].size());
      if (fl[i][direction].size() < minNumOfPoints) minNumOfPoints = fl[i][direction].size();
      if (fl[i][direction].size() > maxNumOfPoints) maxNumOfPoints = fl[i][direction].size();
   }

   //minNumOfPoints = 8;

   vtkPoints points = new vtkPoints();
   vtkFloatArray scalars = new vtkFloatArray();
   Enumeration e;
   MagPoint p;
   for (int i=0; i<dateCount; i++) {
     e = fl[i][direction].elements();
     for (int j=0; j<fl[i][direction].size(); j++) {
         p = (MagPoint)e.nextElement();
         r = p.gsm;
         points.InsertNextPoint(r[X], r[Y], r[Z]);
         if (j!=0) scalars.InsertNextValue(data[i][VALUE]);
     }
     // add the last point to fill the length to maxNumOfPoints
     for (int j=fl[i][direction].size(); j<maxNumOfPoints; j++) {
         points.InsertNextPoint(r[X], r[Y], r[Z]);
         if (j!=0) scalars.InsertNextValue(data[i][VALUE]);
     }
   }

   vtkStructuredGrid grid = new vtkStructuredGrid();
        grid.SetPoints(points);
        grid.SetDimensions(maxNumOfPoints, dateCount, 1);
        grid.GetCellData().SetScalars(scalars);


   vtkStructuredGridGeometryFilter filter = new vtkStructuredGridGeometryFilter();
        filter.SetInputData(grid);

        //filter.SetExtent(0, dateCount - 1, 0, minNumOfPoints - 1, 1, 1);

    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    //mapper.SetInputData(filter.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
    mapper.SetInputConnection(filter.GetOutputPort());
    mapper.SetScalarModeToUseCellData();
        mapper.ScalarVisibilityOn();
    mapper.SetScalarRange(min, max);
    mapper.SetLookupTable(dataModule.getLookupTable());


    actor = new vtkActor();
        actor.SetMapper(mapper);
        actor.GetProperty().SetRepresentationToSurface();
        //actor.GetProperty().SetOpacity(0.6);
    super.validate();
}


protected Fieldline[] makeFieldlines(double mjd, double[] gsm) {
    Fieldline[] fl;
    double rmin = getMagProps().alt / Const.RE + 1.;
    MagProps magProps = getMagProps();

    if (gsm == null) System.out.println("NULL POSITION!!!");
    double alt = 2*Const.RE;
    Fieldline fl1 = Trace.traceline(magProps, mjd, gsm,  1 * 0.001, 0, alt);
    Fieldline fl2 = Trace.traceline(magProps, mjd, gsm, -1 * 0.001, 0, alt);

    boolean first = true; // - first line is FL_2_EARTH

    // if two fotprints are close to earth - choose the shirtest line
    if ((Vect.absv(fl1.lastPoint().gsm) < rmin) &&
        (Vect.absv(fl2.lastPoint().gsm) < rmin)) {
      if ((Math.abs(fl1.length()) < Math.abs(fl2.length()))) {
        first = true;
      } else {
        first = false;
      }
    // choose the line, which footprin is close to earth
    } else if (Vect.absv(fl1.lastPoint().gsm) < rmin) {
      first = true;
    } else {
      first = false;
    }

    if (first) return new Fieldline[]{ fl1, fl2 };
    else return new Fieldline[]{ fl2, fl1 };

}


public Descriptors getDescriptors() {
    if ( descriptors == null) {
        try {
            descriptors = super.getDescriptors();
            descriptors.removeDescriptor("color");

            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("direction", this);
            String[] tags = { "to Earth", "to Equator" };
            MenuPropertyEditor editor = new MenuPropertyEditor(pd, tags);
            addPropertyChangeListener("direction", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);
        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());  System.exit(0);
        }
    }
    return descriptors;
}

/** Getter for property direction.
 * @return Value of property direction.
 */
public int getDirection() {
    return direction;
}

/** Setter for property direction.
 * @param direction New value of property direction.
 */
public void setDirection(int direction) throws IllegalArgumentException {
    int oldDirection = this.direction;
    if (oldDirection == direction) return; // nothing has changed
    if (direction != TO_EARTH && direction != TO_EQUATOR)
        throw new IllegalArgumentException("Invalid direction("+direction+")");
    this.direction = direction;
    invalidate();
    if (isVisible()) { hide(); show(); }
    firePropertyChange ("direction", new Integer (oldDirection), new Integer (direction));
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
    System.out.println("Coord system changed.. I see...");
    if (evt.getWindow() == Const.XYZ) {
        if (getCS() != CoordinateSystem.GSM  &&  isEnabled()) setEnabled(false);
        else if (getCS() == CoordinateSystem.GSM  &&  !isEnabled()) setEnabled(true);
    }
}

/** Method overriden to unregister this as a propertyChange listener and to dispose customizer */
public void dispose() {
    setVisible(false);
   // if (customizer != null) customizer.dispose();
    super.dispose();
}


}
