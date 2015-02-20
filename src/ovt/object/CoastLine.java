/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/CoastLine.java,v $
  Date:      $Date: 2003/09/28 17:52:46 $
  Version:   $Revision: 1.5 $


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
 * CoastLine.java
 *
 * Created on June 22, 2001, 5:23 AM
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
import javax.swing.*;
/**
 * This object belongs to ovt.object.Earth
 * @author  ko
 * @version 
 */
public class CoastLine extends ovt.object.SingleActorObject implements TimeChangeListener, 
                      CoordinateSystemChangeListener {

/** Holds value of property prefferedVisibility. */
private boolean prefferedVisibility = false;
private  BasicPropertyEditor prefferedVisibilityEditor;
                         
/** Creates new EarthGrid */
public CoastLine(Earth earth) {
    super(earth.getCore(), "CoastLine");
    setParent(earth);
    
    showInTree(false);
    
    setColor(new java.awt.Color(-7763575));
    Descriptors descriptors = super.getDescriptors();
    descriptors.remove("visible"); // remove "Show/Hide" descriptor
    try {
      
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("prefferedVisibility", this);
        pd.setDisplayName(getName());
        
        prefferedVisibilityEditor = new MenuPropertyEditor(pd, MenuPropertyEditor.SWITCH);
            prefferedVisibilityEditor.setTags(new String[]{"On", "Off"});
            prefferedVisibilityEditor.setValues(new Object[]{new Boolean(true), new Boolean(false)});
        addPropertyChangeListener("prefferedVisibility", prefferedVisibilityEditor);
        pd.setPropertyEditor(prefferedVisibilityEditor);
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
        if (isPrefferedVisibility()) super.setVisible(visible);
    } else super.setVisible(visible); // no questions if someone whants to hide us
}

public boolean parentIsVisible() {
        return ((VisualObject)getParent()).isVisible();
}

protected void validate() {
    Log.log("Recalculating CoastLine ...", 5);
	// create actor
	// Here we go!

    try {
        ContinentsReader cr = new ContinentsReader(OVTCore.getMdataDir()+"coastline.dat");
        vtkPolyDataMapper mapper = new vtkPolyDataMapper();
            mapper.SetInputData(cr.GetOutput());
    
        actor = new vtkActor();
            actor.SetMapper(mapper);
            float[] rgb = ovt.util.Utils.getRGB(getColor());
            actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);

        
    } catch (IOException e) {
        Log.err("Error loading coastline from "+OVTCore.getMdataDir()+"coastline.dat", 0);
    }
    super.validate();
}

public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).geo_trans_matrix(getCS());
    actor.SetUserMatrix(m3x3.getVTKMatrix()); 
}

public void timeChanged(TimeEvent evt) {
    if (isVisible()) rotate();
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    if (isVisible()) rotate();
          
    if (evt.getWindow() == Const.POLAR) {
      // it is not possible to show earth with continents 
      // if CS is not GEO
      if (evt.getNewCS() == CoordinateSystem.GEO) {
        prefferedVisibilityEditor.setEnabled(true);
        if (!isVisible() && isPrefferedVisibility()) setVisible(true);
      } else { // new CS != GEO
        prefferedVisibilityEditor.setEnabled(false);
        if (isVisible()) setVisible(false); // hide if visible
      }
    }
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
public void setPrefferedVisibility(boolean prefferedVisibility) throws IllegalArgumentException {
    Log.log("prefferedVisibility="+prefferedVisibility+" parentIsVisible="+parentIsVisible()+" isVisible()="+isVisible(), 8);
    boolean oldPrefferedVisibility = this.prefferedVisibility;
    if (oldPrefferedVisibility == prefferedVisibility) return; // nothing have changed

    if (prefferedVisibility && getPolarCS() != CoordinateSystem.GEO)
        throw new IllegalArgumentException("Coastline can be only shown in GEO coordinate system");

    this.prefferedVisibility = prefferedVisibility;
    
    if (prefferedVisibility == true) {
        if (parentIsVisible()) setVisible(true); // show only if the parent is visible
    } else {
        if (isVisible()) setVisible(false); // hide if shown
    }
    propertyChangeSupport.firePropertyChange ("prefferedVisibility", new Boolean (oldPrefferedVisibility), new Boolean (prefferedVisibility));
}


}

class ContinentsReader {
    String file;
    vtkPolyData profile = new vtkPolyData();

ContinentsReader(String file) throws IOException {
    this.file = file;
    Log.log("Loading continents from " + file + " ...", 0);  
    vtkCellArray lines = new vtkCellArray();
    vtkPoints points = new vtkPoints();
      
    double i = 0;
    
    
    String str;
      
    try {
      //DataInputStream inData = new DataInputStream(new FileInputStream(file));
      BufferedReader inData = new BufferedReader(new FileReader(file));
      double lat, lon;
      double[] r_lat_lon = new double[3];
      r_lat_lon[0] = 1;
      double[] r;
      int n_points = 0;
      int n_lines = 0;
    
      int line_start = 0;
      StringTokenizer st;
      String line;
      String s1, s2;
      while((line = inData.readLine()) != null) {
        
        if (!line.equals("nan nan")) {
            st = new StringTokenizer(line, "\t");
            s1 = st.nextToken();
            s2 = st.nextToken();
            lon = new Double(s1).doubleValue();
            lat = new Double(s2).doubleValue();
            r_lat_lon[1] = lat;
            r_lat_lon[2] = lon;
            //System.out.println(lat + "\t" + lon);
            r = Utils.sph2rec(r_lat_lon);
            points.InsertNextPoint(r[0], r[1], r[2]);
        } else {
            lines.InsertNextCell(points.GetNumberOfPoints() - line_start);
            for(int j=line_start; j<points.GetNumberOfPoints(); j++) 
            lines.InsertCellPoint(j);
            
            line_start = points.GetNumberOfPoints();
        }
    }
        
    profile.SetPoints(points);
    profile.SetLines(lines);
    inData.close();
   } catch (FileNotFoundException e){
         throw new IOException("File "+file+" not found.");
   } catch (IOException e){
         throw new IOException("IO error with "+file+" datafile.");
   } // try - catch wrong!!
   System.out.println(" done.");  
}
    
    vtkPolyData GetOutput () {
        return profile;
    }
    
}

