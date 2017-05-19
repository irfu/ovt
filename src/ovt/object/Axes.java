/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/Axes.java,v $
  Date:      $Date: 2005/12/14 18:31:32 $
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

OVT Team (https://ovt.irfu.se)   K. Stasiewicz, M. Khotyaintsev, Y.
Khotyaintsev

=========================================================================*/

/*
 * Axes.java
 *
 * Created on March 28, 2000, 8:50 PM
 */
 
package ovt.object;

import vtk.*;

import ovt.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.interfaces.*;

import java.beans.*;

/** 
 *
 * @author  ko
 * @version 
 */
public final class Axes extends VisualObject {

  /** The size of the actor for scale=1 */
  protected double normalActorSize = 1;
  protected double normalTitleSize = 0.1;
  private double scale = 2;
  /** Axes actor */
  protected vtkActor actor; 
  protected vtkFollower[] axeTitleActor = new vtkFollower[3];
  protected String[] axeTitle = {"x", "y", "z"};
  protected static final int[][] axeTitlePosition = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
  
  /** Creates new Axes
   * @param core */
  public Axes(OVTCore core) {
    super(core, "Axes", "images/axes.gif");
    
    vtkAxes axes = new vtkAxes();
      axes.SetOrigin(0, 0, 0);      
    
    vtkTubeFilter tubeFilter = new vtkTubeFilter();
    tubeFilter.SetInputConnection(axes.GetOutputPort());
    tubeFilter.SetRadius(0.01);
    
    vtkPolyDataMapper mapper = new vtkPolyDataMapper();
    mapper.SetInputConnection(tubeFilter.GetOutputPort());
    
    actor = new vtkActor();
    actor.SetMapper(mapper);
    
    
    vtkVectorText atext;
    vtkFollower textActor;
    double[] x = new double[3];
    int k;
    // create axis titles
    for (int i=0; i<3; i++) {
      atext = new vtkVectorText();
        atext.SetText(axeTitle[i]);
      mapper = new vtkPolyDataMapper();
        mapper.SetInputConnection(atext.GetOutputPort());
      axeTitleActor[i] = new vtkFollower();
        axeTitleActor[i].SetMapper(mapper);
        axeTitleActor[i].SetCamera(getRenderer().GetActiveCamera());
        axeTitleActor[i].GetProperty().SetColor(0, 0, 0);
    }
    setScale(scale);
    setVisible(true);
  }
  
  public double getScale() {
        return scale;
  }
    
  public void setScale(double scale) {
        //Log.log("new scale: " + scale);
        double oldScale = scale;
        this.scale = scale;
        if (actor != null) {
            actor.SetScale(normalActorSize * scale);
            double[] x = new double[3];
            for (int i=0; i<3; i++) {
              axeTitleActor[i].SetScale(normalTitleSize * scale);
              for (int k=0; k<3; k++) x[k] = axeTitlePosition[i][k]*scale*normalActorSize;
              axeTitleActor[i].SetPosition(x);
            }
        }
        firePropertyChange("scale", oldScale, scale);
  }
  
  public void show() {
    for (int k=0; k<3; k++) getRenderer().AddActor(axeTitleActor[k]);
    getRenderer().AddActor(actor);
  }
  
  public void hide() {
    for (int k=0; k<3; k++) getRenderer().RemoveActor(axeTitleActor[k]);
    getRenderer().RemoveActor(actor);
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible != isVisible()) {
      if (visible) show();
      else hide();
      super.setVisible(visible);
    }
  }
  
  @Override
  public Descriptors getDescriptors() {
    if (descriptors == null) {
        try {
            descriptors = super.getDescriptors();
            
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("scale", this);
            pd.setLabel("Length");
            pd.setDisplayName("Axes length (RE)");
            SliderPropertyEditor sliderEditor = //new SliderPropertyEditor(pd, 2, 30, 2, 2); 
              new ExponentialSliderPropertyEditor(pd, 1, 32, 100, new double[] {1, 2, 4, 8, 16, 32});
            sliderEditor.setPrecision(3);
            addPropertyChangeListener("scale", sliderEditor);
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
