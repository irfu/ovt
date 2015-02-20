/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/SingleActor2DObject.java,v $
  Date:      $Date: 2003/09/28 17:52:51 $
  Version:   $Revision: 1.3 $


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
 * SingleActor2DObject.java
 *
 * Created on August 2, 2001, 6:22 AM
 */

package ovt.object;


import ovt.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author  root
 * @version 
 */
public class SingleActor2DObject extends VisualObject {

/** Holds value of property color. */
  private Color color = Color.black;
  
  protected vtkActor2D actor = null;

    
/** Creates new SingleActorObject with name*/
  public SingleActor2DObject(OVTCore core, String name) {
    super(core, name);
  }
  
  /** Creates new SingleActorObject with name and Icon*/
  public SingleActor2DObject(OVTCore core, String name, String iconFilename) {
    super(core, name, iconFilename);
  }

/** Getter for property color.
 * @return Value of property color.
 */
  public Color getColor() {
    return color;
  }
  
/** Setter for property color.
 * @param color New value of property color.
 *
 * @throws PropertyVetoException
 */
  public void setColor(Color color) {
    Color oldColor = this.color;
    this.color = color;
    if (actor != null) {
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
    }
    propertyChangeSupport.firePropertyChange ("color", oldColor, color);
  }

  
public void update() {
    if (isVisible()) {
        hide();
        show();
    }
}
  
protected void show() {
  if (!isValid()) validate();
  if (actor != null)  getRenderer().AddActor(actor);
}

protected void hide() {
  if (actor != null)  getRenderer().RemoveActor(actor);
}
  

public void setVisible(boolean visible) {
    if (isVisible() != visible) {
      if (visible) show();
      else hide();
      super.setVisible(visible);
    }
}

/** This method should create <CODE>actor</CODE> variable */
protected void validate() {
  valid = true;
}
  
public Descriptors getDescriptors() {
    if (descriptors == null) {
      try {
        descriptors = super.getDescriptors();
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("color", this);
        pd.setLabel("Color");
        pd.setDisplayName(getName()+" color");
        
        ComponentPropertyEditor editor = new ColorPropertyEditor(pd);
        editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
            public void editingFinished(GUIPropertyEditorEvent evt) {
                Render();
            }
        });
        addPropertyChangeListener("color", editor);
        pd.setPropertyEditor(new WindowedPropertyEditor(editor, getCore().getXYZWin(), "Close"));
        descriptors.put(pd);
        
      } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
      }
    }
    return descriptors;
}

}
