/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/BowShock.java,v $
  Date:      $Date: 2003/09/28 17:52:45 $
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

OVT Team (http://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.model.bowshock.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Bow Shock depends on Sine and Cosine of Dipole tilt angle,
 * SWP and Mach Number.
 */

public class BowShock extends SingleActorObject implements 
  TimeChangeListener, CoordinateSystemChangeListener, 
  MagPropsChangeListener, MenuItemsSource
{
    
  private static final int DEBUG = 20;

  
  /** Holds value of property representation. */
  private int representation = RepresentationEditor.WIREFRAME;
  /** Holds value of property opacity. */
  private double opacity = 0.1;
  
  private final int[] activityDependsOn = { MagProps.SWP, MagProps.MACHNUMBER };
  /** Holds Characteristics of this object */
  private final Characteristics characteristics = new Characteristics(-1);
  
  /** Holds value of property customizerVisible. */
  private final boolean customizerVisible = false;

public BowShock(OVTCore core) { 
  super(core, "BowShock", "images/bowshock.gif");
  // set the color
  setColor(Color.green); 
  Log.log("BowShock :: init ...", DEBUG);
}


  @Override
  protected void show() {
  super.show();
  setRepresentation(getRepresentation()); 
  rotate();
}


  @Override
  protected void validate() {
        Log.log("Recalculating BowShock ...", DEBUG);
	// create actor
	// Here we go!

	double[] gsm = new double[3];
	double[] rv = new double[3];
	
	double thetaMax = 140*Const.D_TO_R;
	
	double phi, theta, dPhi, dTheta, r;
        int phiResolution = 50, thetaResolution = 60;
	double x, y, z;
	
	dPhi   = 2.*Math.PI/(phiResolution );
	dTheta = thetaMax/(thetaResolution );
	
	vtkPoints points = new vtkPoints();
        
        final double swp = getMagProps().getSWP(getMjd());        
        final double machNumber = getMagProps().getMachNumber(getMjd());
        
        // save characteristics
        characteristics.setMjd(getMjd());
        characteristics.put(MagProps.SWP, swp);
        characteristics.put(MagProps.MACHNUMBER, machNumber);
        
        //System.out.println("MachNumber = "+machNumber);
        double cosTheta, sinTheta, cosPhi, sinPhi;
        
	int i, j;
        int sizex = phiResolution + 1;
        int sizey = thetaResolution + 1;
        
	for (theta=0, i=0; i<sizey; theta+=dTheta, i++) {
          sinTheta = Math.sin(theta);
          cosTheta = Math.cos(theta);
	  for (phi=0, j=0; j<sizex; phi+=dPhi, j++) {
            sinPhi = Math.sin(phi);
            cosPhi = Math.cos(phi);
            r = Bowshock99Model.getR(cosTheta, swp, machNumber);
            x = r * cosTheta;
            z = r * sinTheta * cosPhi;
            y = r * sinTheta * sinPhi;
            points.InsertNextPoint(x, y, z);
	  }
	}
        
	vtkStructuredGrid sgrid = new vtkStructuredGrid();
			sgrid.SetDimensions(sizex, sizey,1);
			sgrid.SetPoints(points);
		
	vtkStructuredGridGeometryFilter gfilter = new vtkStructuredGridGeometryFilter();
			gfilter.SetInputData(sgrid);
			gfilter.SetExtent(0,sizex,0,sizey,0,0);                

	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			mapper.SetInputConnection(gfilter.GetOutputPort());
		
        actor = new vtkActor();
        	actor.SetMapper(mapper);
                float[] rgb = ovt.util.Utils.getRGB(getColor());
                actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
                actor.GetProperty().SetOpacity(this.opacity);
       
       super.validate();
}

public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
    actor.SetUserMatrix(m3x3.getVTKMatrix()); 
}


/** Getter for property representation.
 * @return RepresentationEditor.WIREFRAME or RepresentationEditor.SURFACE.
 */
public int getRepresentation() {
  return representation;
}

/** Setter for property representation (RepresentationEditor.WIREFRAME or RepresentationEditor.SURFACE).
 * @param representation New value of property representation.
 * @see ovt.beans.editor.RepresentationEditor
 */
public void setRepresentation(int representation) {
  this.representation = representation;
  if (actor != null) actor.GetProperty().SetRepresentation(representation);
  firePropertyChange("representation", null, null);
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
    firePropertyChange ("opacity", oldOpacity, opacity);
}

  @Override
  public void timeChanged(TimeEvent evt) {
  // check if SWP and MachNumber Changed
  Characteristics newCh = getMagProps().getCharacteristics(activityDependsOn, getMjd());
  if (!characteristics.equals(newCh)) {
      //newCh.list();
      invalidate();
      if (isVisible()) {
            hide();
            show();
      }
  } else if (isVisible()) {
      rotate();
  }
}

  @Override
  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
  if (isVisible()) rotate();
}

  @Override
  public void magPropsChanged(MagPropsEvent evt) {
  // check if SWP and MachNumber Changed
  if (Vect.contains(activityDependsOn, evt.whatChanged())) { // if data, bowshock depens on changed
    Characteristics newCh = getMagProps().getCharacteristics(activityDependsOn, getMjd());
    if (!characteristics.equals(newCh)) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
    }
  }
}

  @Override
  public JMenuItem[] getMenuItems() {
    JMenu menu = new JMenu("Depends on");
        menu.setFont(Style.getMenuFont());
    JMenuItem item = new JMenuItem("SWP...");
        item.setFont(Style.getMenuFont());
        item.addActionListener((ActionEvent evt) -> {
          getMagProps().activityEditors[MagProps.SWP].setVisible(true);
    });
        menu.add(item);
    item = new JMenuItem("Mach Number...");
        item.setFont(Style.getMenuFont());
        item.addActionListener((ActionEvent evt) -> {
          getMagProps().activityEditors[MagProps.MACHNUMBER].setVisible(true);
    });
        menu.add(item);
    return new JMenuItem[] { menu };
}

  @Override
  public Descriptors getDescriptors() {
    if (descriptors == null) {
        descriptors = super.getDescriptors();
        try {
            
            // representation property descriptor 
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("representation", this);
            pd.setDisplayName("Representation:");
            MenuPropertyEditor representationEditor = new MenuPropertyEditor(pd, 
                new int[]{ RepresentationEditor.WIREFRAME, RepresentationEditor.SURFACE}, 
                new String[]{ "Wireframe", "Surface"}
            );
            // Render each time user changes time by means of gui
            representationEditor.addGUIPropertyEditorListener((GUIPropertyEditorEvent evt) -> {
              Render();
            });
            pd.setPropertyEditor(representationEditor);
            descriptors.put(pd);
            addPropertyChangeListener("representation", representationEditor); 
            
            // opacity
            
            pd = new BasicPropertyDescriptor("opacity", this);
            pd.setLabel("Opacity");
            pd.setDisplayName("BowShock opacity");
            SliderPropertyEditor sliderEditor = new SliderPropertyEditor(pd, 0., 1., 0.05, 
                new double[]{0,.25,.5,.75,1}, new String[]{"0%","25%","50%","75%","100%"});
            addPropertyChangeListener("opacity", sliderEditor);
            sliderEditor.addGUIPropertyEditorListener((GUIPropertyEditorEvent evt) -> {
              Render();
            });
            pd.setPropertyEditor(new WindowedPropertyEditor(sliderEditor, getCore().getXYZWin()));
            descriptors.put(pd);
            
        } catch (IntrospectionException e2) {
            System.exit(-1);
        }
    }
    return descriptors;
}


}
