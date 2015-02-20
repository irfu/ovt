/*=========================================================================
 
Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/SingleActorSatModule.java,v $
Date:      $Date: 2009/10/27 12:14:36 $
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
import ovt.event.*;
import ovt.beans.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author  ko
 * @version
 */
public class SingleActorSatModule extends AbstractVisualSatModule {
   /** Holds value of property color. */
    private Color color = Color.blue;
    
    protected vtkActor actor = null;
    
    
/** Creates new SingleActorSatModule with name*/
    public SingleActorSatModule(Sat sat, String name) {
        super(sat, name);
    }
    
  /** Creates new SingleActorSatModule with the name and Icon */
    public SingleActorSatModule(Sat sat, String name, String iconFilename) {
        super(sat, name, iconFilename);
    }
    
    public vtkActor getActor() {
        return actor;
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
        if (isVisible() && isEnabled()) {
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
            super.setVisible(visible);
            if (visible) show();
            else hide();
        }
    }
    
    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) return; // nothing' s changed
        super.setEnabled(enabled);
        if (!enabled) setVisible(false);
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
                pd.setLabel("Color...");
                pd.setDisplayName(getParent().getName()+" : "+ getName() +" color");
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
