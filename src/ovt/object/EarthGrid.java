/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/EarthGrid.java,v $
  Date:      $Date: 2003/09/28 17:52:47 $
  Version:   $Revision: 1.16 $


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
 * EarthGrid.java
 *
 * Created on June 21, 2001, 2:49 AM
 */

package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;
import ovt.object.vtk.ProgrammableEarthSource;

import vtk.*;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.beans.*;
import java.awt.*;
import javax.swing.*;
/**
 * Visualizes parallel-meridian grid on the {@link ovt.object.Earth Earth} surface.
 * @author  ko
 * @version
 */

public class EarthGrid extends ovt.object.SingleActorObject implements TimeChangeListener,
                      CoordinateSystemChangeListener, MenuItemsSource {

/** Holds value of property prefferedVisibility. */
private boolean prefferedVisibility = false;
public EarthGridLabels labels;


/** Creates new EarthGrid */
public EarthGrid(Earth earth) {
    super(earth.getCore(), "EarthGrid");
    setParent(earth);
    showInTree(false);
    setColor(Color.white);

    labels = new EarthGridLabels(this);

    Descriptors descriptors = super.getDescriptors();
    descriptors.remove("visible"); // remove "Show/Hide" descriptor
    try {

        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("prefferedVisibility", this);
        pd.setDisplayName(getName());

        BasicPropertyEditor editor = new MenuPropertyEditor(pd, MenuPropertyEditor.SWITCH);
            editor.setTags(new String[]{"On", "Off"});
            editor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
        addPropertyChangeListener("prefferedVisibility", editor);
        pd.setPropertyEditor(editor);
        descriptors.put(pd);

    } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
    }

}

protected void show() {
  super.show();
  rotate();
}

public void setVisible(boolean visible) {
    if (visible) { // to show one must look at a parent's "visibility" state
        if (isPrefferedVisibility()) {
            super.setVisible(visible);
            labels.setVisible(visible);
        }
    } else { // no questions if someone whants to hide us
        super.setVisible(visible);
        labels.setVisible(visible);
    }
}

public boolean parentIsVisible() {
    //try {
        return ((VisualObject)getParent()).isVisible();
    //} catch (NullPointerException e2) { return false;
    //}
}

protected void validate() {
    Log.log("Recalculating Earth Grid ...", 5);
    // create actor
    // Here we go!
    vtkAppendPolyData appendPolyData = new vtkAppendPolyData();
    vtkCellArray lines;
    vtkPoints points;
    vtkPolyData circle;
    double[] r;

    double dAngle = 1;
    int cellSize = (int)(360./dAngle);
    // create meridians
    for (int phi=0; phi<360; phi+=15) {
        points = new vtkPoints();
        lines = new vtkCellArray();
        lines.InsertNextCell(cellSize);
        int pointNumber = 0;
        for (int delta=-180; delta<180; delta+=dAngle) {
            r = Utils.sph2rec(1., delta, phi);
            points.InsertNextPoint(r[0], r[1], r[2]);
            lines.InsertCellPoint(pointNumber++);
        }
        circle = new vtkPolyData();
            circle.SetPoints(points);
            circle.SetLines(lines);
        appendPolyData.AddInputData(circle);
    }
    // parallels
    for (int delta=-80; delta!=90; delta+=10) {

        points = new vtkPoints();
        lines = new vtkCellArray();
        lines.InsertNextCell(cellSize);
        int pointNumber = 0;
        for (int phi=0; phi<360; phi+=dAngle) {
            r = Utils.sph2rec(1., delta, phi);
            points.InsertNextPoint(r[0], r[1], r[2]);
            lines.InsertCellPoint(pointNumber++);
        }
        circle = new vtkPolyData();
            circle.SetPoints(points);
            circle.SetLines(lines);
        appendPolyData.AddInputData(circle);
    }

    vtkTubeFilter tubeFilter = new vtkTubeFilter();
        //tubeFilter.SetInputData(appendPolyData.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
        tubeFilter.SetInputConnection(appendPolyData.GetOutputPort());
        tubeFilter.SetRadius(0.001);
        tubeFilter.SetNumberOfSides(2);
    // create slid surface



    vtkPolyDataMapper mapper = new vtkPolyDataMapper();

      //mapper.SetInputData(tubeFilter.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
      mapper.SetInputConnection(tubeFilter.GetOutputPort());
      //mapper.SetInput(appendPolyData.GetOutput());

     actor = new vtkActor();
        actor.SetMapper(mapper);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
        actor.GetProperty().SetAmbientColor(rgb[0], rgb[1], rgb[2]);
        actor.GetProperty().SetSpecularColor(rgb[0], rgb[1], rgb[2]);
        actor.GetProperty().SetDiffuseColor(rgb[0], rgb[1], rgb[2]);
    super.validate();
}

/*
protected void validate() {
    Log.log("Recalculating Earth Grid ...", 5);
	// create actor
	// Here we go!

    ProgrammableEarthSource earthSource = new ProgrammableEarthSource();
      earthSource.setRadius(1);
      earthSource.setPhiResolution(5);
      earthSource.setThetaResolution(5);

    vtkPolyData polyData = earthSource.GetOutput();

    vtkThresholdPoints thresholdPoints = new vtkThresholdPoints();
      thresholdPoints.SetInput(polyData);
      thresholdPoints.ThresholdBetween(1, 1);

    //vtkTubeFilter tubeFilter = new vtkTubeFilter();
        //tubeFilter.SetInput(thresholdPoints.GetOutput());
       // tubeFilter.SetRadius(0.01);
    // create slid surface


    // Grid

    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
      //mapper.SetInput(polyData);
      mapper.SetInput(thresholdPoints.GetOutput());

     actor = new vtkActor();
        actor.SetMapper(mapper);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);

    super.validate();
}*/

public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).trans_matrix(getPolarCS(), getCS());
    actor.SetUserMatrix(m3x3.getVTKMatrix());
}

public void timeChanged(TimeEvent evt) {
    if (isVisible()) rotate();
    labels.timeChanged(evt);
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
    labels.coordinateSystemChanged(evt);
}


/** Getter for property prefferedVisibility.
 * @return Value of property prefferedVisibility.
 */
public boolean isPrefferedVisibility() {
    return prefferedVisibility;
}

/** Setter for property prefferedVisibility.
 * @param prefferedVisibility New value of property prefferedVisibility.
 */
public void setPrefferedVisibility(boolean prefferedVisibility) {
    Log.log("prefferedVisibility="+prefferedVisibility+" parentIsVisible="+parentIsVisible()+" isVisible()="+isVisible(), 8);
    boolean oldPrefferedVisibility = this.prefferedVisibility;
    if (oldPrefferedVisibility == prefferedVisibility) return; // nothing have changed
    this.prefferedVisibility = prefferedVisibility;

    if (prefferedVisibility == true) {
        if (parentIsVisible()) setVisible(true); // show only if the parent is visible
    } else {
        if (isVisible()) setVisible(false); // hide if shown
    }
    propertyChangeSupport.firePropertyChange ("prefferedVisibility", new Boolean (oldPrefferedVisibility), new Boolean (prefferedVisibility));
}

public JMenuItem[] getMenuItems() {
    // return menu item of labels
    MenuItemsSource vis_ed = (MenuItemsSource)labels.getDescriptors().getDescriptor("prefferedVisibility").getPropertyEditor();
    MenuItemsSource color_ed = (MenuItemsSource)labels.getDescriptors().getDescriptor("color").getPropertyEditor();
    JMenuItem colorMenuItem = color_ed.getMenuItems()[0];
    colorMenuItem.setText("Labels Color..."); // adjust text
    return new JMenuItem[]{ vis_ed.getMenuItems()[0], colorMenuItem };
}

/** for XML */
public EarthGridLabels getEarthGridLabels() { return labels; }

}
