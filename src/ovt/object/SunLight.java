/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/SunLight.java,v $
  Date:      $Date: 2003/09/28 17:52:52 $
  Version:   $Revision: 2.7 $


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
 * SunLight.java
 *
 * Created on April 7, 2000, 3:40 PM
 */
 
package ovt.object;

import ovt.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
/** 
 *
 * @author  ko
 * @version 
 */
public class SunLight extends VisualObject
  implements TimeChangeListener, CoordinateSystemChangeListener {

  
  protected final static double r = 50;
  protected static final double[] position = {r, 0, 0};
  protected vtkLight light;
  
  /** Holds value of property intensity. */
  private double intensity = 1.;  
  
  /** Creates new SunLight */
  public SunLight(OVTCore core) {
    super(core, "The Sun", "images/sun.gif");
    
    /*vtkLight cameraLight = getRenderer().GetLights().GetNextItem();
    double intensity = cameraLight.GetIntensity();
    System.out.println("Intensity="+intensity);
    cameraLight.SetIntensity(0.1);*/
    
    light = new vtkLight();
      light.SetColor(1, 1, 1);
      light.SetConeAngle(180.0);
      light.SetFocalPoint(0, 0, 0);
      light.SetPosition(r, 0, 0);
      light.SetIntensity(1.0);
    //setVisible(true);
  }
  
  public void setVisible(boolean visible) {
      if (visible == isVisible()) return; //nothing has changed
      
      if (visible) getRenderer().AddLight(light);
      else getRenderer().RemoveLight(light);
      
      super.setVisible(visible);
  }
  
  public void rotate() {
    Matrix3x3 m3x3 = getTrans(getMjd()).gsm_trans_matrix(getCS());
    light.SetPosition(m3x3.multiply(position)); 
  }
  
  public void timeChanged(TimeEvent evt) {
    rotate();
  }
  public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    rotate();
  }

  /** Getter for property intensity.
   * @return Value of property intensity.
 */
  public double getIntensity() {
      return light.GetIntensity();
  }
  
  /** Setter for property intensity.
   * @param intensity New value of property intensity.
 */
  public void setIntensity(double intensity) {
      double oldIntensity = getIntensity();
      light.SetIntensity(intensity);
      propertyChangeSupport.firePropertyChange ("intensity", new Double (oldIntensity), new Double (intensity));
  }
  
  public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = super.getDescriptors();
                
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("intensity", this);
                pd.setLabel("Intensity");
                pd.setDisplayName("Sun's light intensity");
                ExponentialSliderPropertyEditor sliderEditor = new ExponentialSliderPropertyEditor(pd, 
                    1./8., 8., new double[]{1./8.,1./2., 1, 2, 8});
                addPropertyChangeListener("intensity", sliderEditor);
                sliderEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                pd.setPropertyEditor(new WindowedPropertyEditor(sliderEditor, getCore().getXYZWin()));
                descriptors.put(pd);
                
            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }
  
}
