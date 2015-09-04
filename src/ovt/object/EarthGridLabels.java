/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/EarthGridLabels.java,v $
  Date:      $Date: 2003/09/28 17:52:47 $
  Version:   $Revision: 1.7 $


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
 * EarthGridlabels.java
 *
 * Created on July 9, 2001, 5:59 AM
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
 *
 * @author  root
 * @version
 */
public class EarthGridLabels extends ovt.object.SingleActorObject implements TimeChangeListener,
                      CoordinateSystemChangeListener {

private Vector labels = new Vector();
vtkAssembly actors = new vtkAssembly();
/** Holds value of property prefferedVisibility. */
private boolean prefferedVisibility = true;

/** The size of the actor for scale=1 */
protected double normalLabelSize = 0.02;
/** distance from 0,0,0 to label */
private static final double R = 1.001;

private static final double[][] m1 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

 /** Creates new EarthGridlabels */
public EarthGridLabels(EarthGrid earthGrid) {
    super(earthGrid.getCore(), "EarthGridLabels");
    setParent(earthGrid);
    showInTree(false);
    setColor(new Color(170,170,170));
    Descriptors descriptors = super.getDescriptors();
    descriptors.remove("visible"); // remove "Show/Hide" descriptor
    try {

        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("prefferedVisibility", this);
            pd.setLabel("Show Labels");
            pd.setDisplayName(getName());


        BasicPropertyEditor editor = new BooleanEditor(pd, MenuPropertyEditor.CHECKBOX);
            editor.setTags(new String[]{"LabelsOn", "LabelsOff"});
           // editor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
        addPropertyChangeListener("prefferedVisibility", editor);
        pd.setPropertyEditor(editor);
        descriptors.put(pd);

    } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
    }

}

protected void validate() {
    labels.removeAllElements();
    //System.out.println("Setting lables...");
    float[] rgb = ovt.util.Utils.getRGB(getColor());
    double mjd=0;
    double dPhi = 0;
    vtkAppendPolyData appendPolyData = new vtkAppendPolyData();
    double labelWidth = normalLabelSize*2;
    for (int lon=0; lon<360; lon+=45) {
        for (int lat=0; lat<180; lat+=10) {
            double r = R*Math.sin(Utils.toRadians(lat));
            if (r != 0) dPhi = Math.acos(1-labelWidth*labelWidth/(2*r*r));
            else dPhi = 0;
            //vtkPolyData label = getLabelPolyData(Utils.toRadians(lat), Utils.toRadians(lon) - dPhi, ""+Math.abs(90-lat));
            //appendPolyData.AddInputData(label);
            
            appendPolyData.AddInputConnection(getLabelPolyData(Utils.toRadians(lat), Utils.toRadians(lon) - dPhi, ""+Math.abs(90-lat)));

            
        }
    }

    double[] lat = { 30, 60, 90, 120, 150 };

    for (int i=0; i<lat.length; i++) {
        for (int lon=0; lon<360; lon+=15) {
            //vtkPolyData label = getLabelPolyData(Utils.toRadians(lat[i]), Utils.toRadians(lon), ""+lon);
            //appendPolyData.AddInputData(label);   
            appendPolyData.AddInputConnection(getLabelPolyData(Utils.toRadians(lat[i]), Utils.toRadians(lon), ""+lon));


            

        }
    }




    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    //mapper.SetInputData(appendPolyData.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
    
    mapper.SetInputConnection(appendPolyData.GetOutputPort());
    actor = new vtkActor();
    actor.SetMapper(mapper);
    actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
    actor.GetProperty().SetAmbientColor(rgb[0], rgb[1], rgb[2]);
    actor.GetProperty().SetSpecularColor(rgb[0], rgb[1], rgb[2]);
    actor.GetProperty().SetDiffuseColor(rgb[0], rgb[1], rgb[2]);
    valid = true;
}


/** Note! lat is a angle in radians between R and OZ */
// modded function to output OutputPort instaed of vtkPolyData
//private vtkPolyData getLabelPolyData(double lat, double lon, String text) {
private vtkAlgorithmOutput getLabelPolyData(double lat, double lon, String text) {

vtkVectorText atext = new vtkVectorText();
        atext.SetText(text);

    // orient and move polyData, representing text to it's position
    vtkTransform transform = new vtkTransform();
        transform.SetMatrix(getTransformMatrix(lat, lon)); // apply matrix
        transform.Scale(normalLabelSize, normalLabelSize, normalLabelSize);

    vtkTransformPolyDataFilter transformPolyData = new vtkTransformPolyDataFilter();
        transformPolyData.SetTransform(transform);
        //transformPolyData.SetInputData(atext.GetOutput());//FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
        transformPolyData.SetInputConnection(atext.GetOutputPort());
//    return transformPolyData.GetOutput();
    return transformPolyData.GetOutputPort();
}

/** Returns tr.matrix for positioning and orienting labels.
 * They are always on the earth surface.
 * Note! lat is a ange in radians between R and OZ
 */
private vtkMatrix4x4 getTransformMatrix(double lat, double lon) {
    double [][] m2 = new double[3][3];
    int i = 0, j = 1, k = 2;

    double x = R * Math.sin(lat)*Math.cos(lon);
    double y = R * Math.sin(lat)*Math.sin(lon);
    double z = R * Math.cos(lat);

    // create transformation matrix
    double[] n = Vect.norm(new double[]{ x, y, z}); // normal to earth
    m2[k] = n;
    m2[i] = Vect.crossn(m1[k], m2[k]);
    m2[j] = Vect.crossn(m2[k], m2[i]);

    vtkMatrix4x4 matr = new vtkMatrix4x4();
    for (int ii=0; ii<3; ii++)
        for (int jj=0; jj<3; jj++) {
            matr.SetElement(ii, jj, Vect.cosAngle(m1[ii], m2[jj]));
    }
    // translate to x, y, z
    matr.SetElement(0, 3, x); // row, column, value
    matr.SetElement(1, 3, y);
    matr.SetElement(2, 3, z);
    return matr;
}

    protected void show() {
        if (!isValid()) validate();
        getRenderer().AddActor(actor);
        rotate();
    }

    protected void hide() {
        getRenderer().RemoveActor(actor);
    }

public void setVisible(boolean visible) {
    if (visible) { // to show one must look at a parent's "visibility" state
        if (isPrefferedVisibility()) super.setVisible(visible);
    } else super.setVisible(visible); // no questions if someone whants to hide us
}

public boolean parentIsVisible() {
    //try {
        return ((VisualObject)getParent()).isVisible();
    //} catch (NullPointerException e2) { return false;
    //}
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

public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).trans_matrix(getPolarCS(), getCS());
    actor.SetUserMatrix(m3x3.getVTKMatrix());
}

public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
    } else { // current time changed
        if (isVisible()) rotate();
    }
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
}

}
