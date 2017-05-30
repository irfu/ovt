/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/MagFootprintModule.java,v $
  Date:      $Date: 2003/09/28 17:52:49 $
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

/*
 * MagFootprintModule.java
 *
 * Created on March 30, 2000, 6:08 PM
 */

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;

import vtk.*;

import java.awt.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;


/**
 *
 * @author  ko
 * @version
 */
public class MagFootprintModule extends SingleActorSatModule
implements MagPropsChangeListener, MenuItemsSource, BeansSource {

    protected static final int XYZ = Const.XYZ;
    protected static final int POLAR = Const.POLAR;
  /** Contains Footprints from the L-shell of the satellite.
   */
    protected FootprintCollection[] fprints = new FootprintCollection[2];
  /** Actors collection */
    protected Hashtable actorCollection = new Hashtable();

    /** Holds value of property scale. */
    private double scale = 1.;
    /** The size of the actor for scale=1 */
    protected double normalActorSize = 0.02;

    private vtkGlyph3D glyph = null;

    public MagFootprintLabels labels;
    private BeansCollection beans;

  /** Creates new MagFootprintModule */
    public MagFootprintModule(AbstractSatModule mainModule) {
        super(mainModule.getSat(), "Magnetic", "images/footprints.gif");
        setParent(mainModule);
        for (int k=0; k<2; k++) fprints[k] = new FootprintCollection();
        setColor(Color.red);
        labels = new MagFootprintLabels(this);
    }

    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = super.getDescriptors();

                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("scale", this);
                pd.setLabel("Size");
                pd.setDisplayName("Magnetic footprints size");
                ExponentialSliderPropertyEditor sliderEditor = new ExponentialSliderPropertyEditor(pd,
                    1./4., 4., new double[]{1./4.,1./2., 1, 2, 4});
                addPropertyChangeListener("scale", sliderEditor);
                pd.setPropertyEditor(new WindowedPropertyEditor(sliderEditor, getCore().getXYZWin()));
                descriptors.put(pd);

            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }

    public void update() {
        OVTCore.setStatus("Computing footprints for " + getSat().getName());
        Fieldline f_l;
        FieldlineCollection f_l_coll;
        MagPoint magPoint;
        for (int i=0; i<2; i++) { // for southern and northern hemispheres
            fprints[i].clear(); // remove all footprints
            f_l_coll = getFieldlineCollection(i);
            Enumeration e = f_l_coll.elements();
            while (e.hasMoreElements()) {
                // take fieldline
                f_l = (Fieldline)(e.nextElement());
                // take the last point of fieldline
                magPoint = f_l.lastPoint();
                // check if it is a real footprint
                if (Vect.absv(magPoint.gsm) <= getMagProps().getFootprintAltitude()+1)
                    fprints[i].put(f_l.getMjd(), (MagPoint)magPoint.clone());
            }
        }
        actorCollection.clear();
        valid = true;
    }
  /** Footprints - Northern and southern hemisphere */
    public FootprintCollection[] getFootprintCollection() {
        if (!isValid()) update();
        return fprints;
    }

  /** Returns the footprint in <CODE>GEO</CODE> for mjd
   * public double[] evaluate(double mjd, int type) {
   *
   * }*/

    public vtkActor getActor(int cs) {

        if (!isValid()) update();
        vtkActor act = (vtkActor)actorCollection.get(new Integer(cs));

        if ( act == null)  {
            double resolution = 2;
            vtkPolyData polyData = new vtkPolyData();
            vtkCellArray lines = new vtkCellArray();
            vtkPoints points = new vtkPoints();
            vtkFloatArray vectors = new vtkFloatArray();
            	vectors.SetNumberOfComponents(3);

            double i = 0;
            double[] a, b;
            Enumeration e;
            for (int k=0; k<2; k++) {
                e = getFootprintCollection()[k].elements();
                while (e.hasMoreElements()) {
                    MagPoint mp = (MagPoint)e.nextElement();
                    Matrix3x3 trans_matrix = getTrans(mp.mjd).gsm_trans_matrix(cs);
                    a = trans_matrix.multiply(mp.gsm);
                    points.InsertNextPoint(a[0], a[1], a[2]);
                    b = trans_matrix.multiply(mp.bv);
                    vectors.InsertNextTuple3(b[0], b[1], b[2]);
                }
            }
            lines.InsertNextCell(points.GetNumberOfPoints());

            for(int j=0; j<points.GetNumberOfPoints(); j++)
                lines.InsertCellPoint(j);


            polyData.SetPoints(points);
            polyData.GetPointData().SetVectors(vectors);
            polyData.SetLines(lines);
            //profile.GetPointData().SetScalars(scalars);

            vtkConeSource cone = new vtkConeSource();
            cone.SetResolution(6);

            //vtkPolyData conePolyData = new vtkPolyData();
            //conePolyData.SetSource(cone.GetOutput());

            glyph = new vtkGlyph3D();
            glyph.SetInputData(polyData);
//            glyph.setSo
            glyph.SetSourceConnection(cone.GetOutputPort());
            glyph.SetVectorModeToUseVector();
            glyph.SetScaleModeToDataScalingOff();
            //glyph.SetColorModeToColorByVector();
            glyph.SetScaleFactor(normalActorSize * getScale());

            vtkLookupTable lut  = new vtkLookupTable();
            lut.SetHueRange(0.6667, 0);

            vtkPolyDataMapper mapper = new vtkPolyDataMapper();
            mapper.SetInputData(glyph.GetOutput());
            mapper.SetInputConnection(glyph.GetOutputPort());
            //mapper.SetInputConnection(glyph.GetOutputPort());

            mapper.SetScalarModeToUsePointData();
            mapper.ScalarVisibilityOn();
            mapper.SetScalarRange(MagProps.BMIN, MagProps.BMAX);
            mapper.SetLookupTable(lut);

            act = new vtkActor();
            act.SetMapper(mapper);

            float[] rgb = ovt.util.Utils.getRGB(getColor());
            act.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);

            actorCollection.put(new Integer(cs), act);
        }
        return act;
    }

  public void setVisible(boolean visible) {
    super.setVisible(visible);
    labels.setVisible(visible);
  }

    public void show() {
        actor = getActor(getPolarCS());
        updateToCurrentCS();
        getRenderer().AddActor(actor);
    }

    public void hide() {
        if (actor != null) {
            getRenderer().RemoveActor(actor);
            actor = null;
        }
    }

    public void updateToCurrentCS() {
        if (actor != null) {
            Matrix3x3 trm = getTrans(getMjd()).trans_matrix(getPolarCS(), getCS());
            actor.SetUserMatrix(trm.getVTKMatrix());
        }
    }


    public void timeChanged(TimeEvent evt) {
        if (evt.timeSetChanged()) {
            //System.out.println("I'm MagFootprint. Time's changed...");
            invalidate();
            if (isVisible()) {
                hide();
                show();
            }
        }
        updateToCurrentCS();
        labels.timeChanged(evt);
    }

    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        //invalidate();
        //if (isVisible()) updateToCurrentCS();
        if (isVisible()) {
            hide();
            show();
        }
        labels.coordinateSystemChanged(evt);
    }

    public FieldlineCollection getFieldlineCollection(int type) {
        return getSat().getFieldlineCollection(type);
    }

    public void magPropsChanged(MagPropsEvent evt) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
        labels.magPropsChanged(evt);
    }

    /** Getter for property scale.
     * @return Value of property scale.
     */
    public double getScale() {
        return scale;
    }

    /** Setter for property scale.
     * @param scale New value of property scale.
     */
    public void setScale(double scale) {
        double oldScale = this.scale;
        this.scale = scale;
        if (scale != oldScale && glyph != null) {
            glyph.SetScaleFactor(normalActorSize * scale);
            Render();
        }
        propertyChangeSupport.firePropertyChange ("scale", new Double (oldScale), new Double (scale));
    }

    public JMenuItem[] getMenuItems() {
        JMenu menu1 = new JMenu("Labels");
          menu1.setFont(ovt.gui.Style.getMenuFont());
          MenuUtils.addMenuItemsFromDescriptors(menu1, labels, getCore());
          MenuUtils.addMenuItemsFromSource(menu1, labels, false);
      return new JMenuItem[]{ menu1 };
    }

    public BeansCollection getBeanDesriptors() {
        if (beans == null)
        {
            beans = new BeansCollection();
            try {
                beans.put(new BasicBeanDescriptor(labels.getName(), "labels", getClass()));
            } catch (NoSuchFieldException e2) { e2.printStackTrace(); System.exit(-1); }
        }
        return beans;
    }

}
