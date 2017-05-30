/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/MagTangent.java,v $
  Date:      $Date: 2006/03/21 12:22:30 $
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

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.model.bowshock.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.util.*;
import ovt.object.editor.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.lang.Math;
import java.util.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Bow Shock depends on Sine and Cosine of Dipole tilt angle,
 * SWP and Mach Number.
 */

public class MagTangent extends SingleActorObject implements
  TimeChangeListener, CoordinateSystemChangeListener,
  MagPropsChangeListener, MenuItemsSource {


  /** Holds value of property representation. */
  private int representation = RepresentationEditor.WIREFRAME;
  /** Holds value of property opacity. */
  private double opacity = 0.1;

  private int[] activityDependsOn = { MagProps.IMF, MagProps.SWP, MagProps.MACHNUMBER };
  /** Holds Characteristics of this object */
  private Characteristics characteristics = new Characteristics(-1);

  /** Holds value of property customizerVisible. */
  private boolean customizerVisible = false;

public MagTangent(OVTCore core) {
  super(core, "Magnetic Tangent", "images/mag_tangent.gif");
  // set the color
  setColor(Color.green);
  Log.log("MagTangent :: init ...", 3);
}


protected void show() {
  super.show();
  setRepresentation(getRepresentation());
  rotate();
}


protected void validate() {
        Log.log("Recalculating BowShock ...", 5);
	// create actor
	// Here we go!

	double[] gsm = new double[3];
	double[] rv = new double[3];


	vtkPoints points = new vtkPoints();

        double[] imf = getMagProps().getIMF(getMjd());
        double swp = getMagProps().getSWP(getMjd());
        double machNumber = getMagProps().getMachNumber(getMjd());

        // save characteristics
        characteristics.setMjd(getMjd());
        characteristics.put(MagProps.SWP, swp);
        characteristics.put(MagProps.MACHNUMBER, machNumber);

        int sizex = 2;
        int sizey = 31;

        double sinPhi = imf[1]/Math.sqrt(imf[1]*imf[1]+imf[2]*imf[2]);
        double cosPhi = imf[2]/Math.sqrt(imf[1]*imf[1]+imf[2]*imf[2]);
        double L = FieldlineModule.LENGH_OF_IMF_LINE;
        double x, y, z, x0;
        // plot equidistant lines (in projection to YZ)
        for (double yp=-30; yp<=30; yp+=2) {
          y = L/2*sinPhi + yp*cosPhi;
          z = L/2*cosPhi - yp*sinPhi;
          x0 = Bowshock99Model.getMagTangentX0(imf, swp, machNumber, y, z);
          x = (imf[0]/Math.sqrt(imf[1]*imf[1]+imf[2]*imf[2]))*L/2 + x0;
          points.InsertNextPoint(x, y, z);
          y = -L/2*sinPhi + yp*cosPhi;
          z = -L/2*cosPhi - yp*sinPhi;
          x = -(imf[0]/Math.sqrt(imf[1]*imf[1]+imf[2]*imf[2]))*L/2 + x0;
          points.InsertNextPoint(x, y, z);
        }



	vtkStructuredGrid sgrid = new vtkStructuredGrid();
			sgrid.SetDimensions(sizex, sizey,1);
			sgrid.SetPoints(points);

	vtkStructuredGridGeometryFilter gfilter = new vtkStructuredGridGeometryFilter();
			gfilter.SetInputData(sgrid);
			gfilter.SetExtent(0,sizex,0,sizey,0,0);


	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
			//mapper.SetInputData(gfilter.GetOutput());
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
    firePropertyChange ("opacity", new Double (oldOpacity), new Double (opacity));
}

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

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
  if (isVisible()) rotate();
}

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

public JMenuItem[] getMenuItems() {
    JMenu menu = new JMenu("Depends on");
        menu.setFont(Style.getMenuFont());
    JMenuItem item = new JMenuItem("IMF...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            //getMagProps().activityEditors[MagProps.IMF].setVisible(true);
            getMagProps().setActivityEditorVisible(MagProps.IMF, true);
        }
        });
        menu.add(item);
    item = new JMenuItem("SWP...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //getMagProps().activityEditors[MagProps.SWP].setVisible(true);
                getMagProps().setActivityEditorVisible(MagProps.SWP, true);
            }
        });
        menu.add(item);
    item = new JMenuItem("Mach Number...");
        item.setFont(Style.getMenuFont());
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //getMagProps().activityEditors[MagProps.MACHNUMBER].setVisible(true);
                getMagProps().setActivityEditorVisible(MagProps.MACHNUMBER, true);
            }
        });
        menu.add(item);
    return new JMenuItem[] { menu };
}

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
            representationEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(representationEditor);
            descriptors.put(pd);
            addPropertyChangeListener("representation", representationEditor);

            // opacity

            pd = new BasicPropertyDescriptor("opacity", this);
            pd.setLabel("Opacity");
            pd.setDisplayName("Magnetic Tangent opacity");
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

        } catch (IntrospectionException e2) {
            e2.printStackTrace();
            System.exit(-1);
        }
    }
    return descriptors;
}


}
