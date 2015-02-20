/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/DataScalarBar.java,v $
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
 * DataScalarBar.java
 *
 * Created on August 2, 2001, 5:20 AM
 */

package ovt.object;

import vtk.*;

import ovt.*;
import ovt.beans.*;
import ovt.interfaces.*;
import ovt.datatype.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author  root
 * @version 
 */
public class DataScalarBar extends SingleActor2DObject {

    private DataModule dataModule;
    /** X position of scalar bar */
    private double x = 0.5;
    /** Y position of scalar bar */
    private double y = 0.01;
    
    public static final int HORIZONTAL_ORIENTATION = 0;
    public static final int VERTICAL_ORIENTATION   = 1;
    
    /** Holds value of property customizerVisible. */
    private boolean customizerVisible = false;
    private DataScalarBarCustomizer customizer = null;
    
    /** Holds value of property orientation. */
    private int orientation = HORIZONTAL_ORIENTATION;
    
    /** Holds value of property width. */
    private double width = 0.8;
    
    /** Holds value of property height. */
    private double height = 0.12;
    
    /** Creates new DataScalarBar */
    public DataScalarBar(DataModule dataModule) {
        super(dataModule.getCore(), "ScalarBar", "images/scalarbar.gif");
        
        this.dataModule = dataModule;
        // listen to parent's "endabled" state
        dataModule.addPropertyChangeListener("enabled", this);
        try {
            descriptors = super.getDescriptors();
            BasicPropertyDescriptor pd;
            GUIPropertyEditor editor;
            
            // x property descriptor 
            pd = new BasicPropertyDescriptor("x", this);
            pd.setMenuAccessible(false);
            pd.setLabel("Horizontal");
            SliderPropertyEditor sEditor = new SliderPropertyEditor(pd, 0., 1.);
            addPropertyChangeListener("x", sEditor);
            pd.setPropertyEditor(sEditor);
            sEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            descriptors.put(pd);
            
            // y property descriptor 
            pd = new BasicPropertyDescriptor("y", this);
            pd.setMenuAccessible(false);
            pd.setLabel("Vertical");
            sEditor = new SliderPropertyEditor(pd, 0., 1.);
            pd.setPropertyEditor(sEditor);
            addPropertyChangeListener("y", sEditor);
            sEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            descriptors.put(pd);
            
            // width
            pd = new BasicPropertyDescriptor("width", this);
            pd.setMenuAccessible(false);
            pd.setLabel("Width");
            sEditor = new SliderPropertyEditor(pd, 0.1, 1.);
            addPropertyChangeListener("width", sEditor);
            pd.setPropertyEditor(sEditor);
            sEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            descriptors.put(pd);
            
            // height
            pd = new BasicPropertyDescriptor("height", this);
            pd.setMenuAccessible(false);
            pd.setLabel("Height");
            sEditor = new SliderPropertyEditor(pd, 0.1, 1.);
            pd.setPropertyEditor(sEditor);
            addPropertyChangeListener("height", sEditor);
            sEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            descriptors.put(pd);
            
            pd = new BasicPropertyDescriptor("orientation", this);
            String[] tags = {"Horizontal", "Vertical"};
            editor = new MenuPropertyEditor(pd, tags);
            addPropertyChangeListener("orientation", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);
            
            // customizerVisible
            pd = new BasicPropertyDescriptor("customizerVisible", this);
            editor = new VisibilityEditor(pd);
            editor.setTags(new String[]{"Properties...", "Properties..."});
            editor.setValues(new Object[]{new Boolean(true), new Boolean(true)});
            addPropertyChangeListener("customizerVisible", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);
            
            pd = descriptors.getDescriptor("color");
            pd.setLabel("Text color...");
            
        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());
            System.exit(0);
        }
    }

    /** can return null if actor is not initialized */
    public vtkScalarBarActor getScalarBarActor() {
        return (vtkScalarBarActor)actor;
    }
    
    protected void validate() {
        vtkScalarBarActor scalarBar = new vtkScalarBarActor();
        actor = scalarBar;
        scalarBar.SetLookupTable(dataModule.getLookupTable());
        scalarBar.SetTitle("Data ("+dataModule.getFile().getName()+")");
        scalarBar.GetPositionCoordinate().SetCoordinateSystemToNormalizedViewport();
        setX(x);
        setY(y);
        setOrientation(orientation);
        setWidth(width);
        setHeight(height);
        setColor(getColor());
        super.validate();
    }
    
    /** updates position of output label, called if label or window size changes ocures
 */    
    public void updatePosition() {
        
        if (actor != null) {
            
            double x1 =  getX()  * (1 - width); // x position
            double y1 =  getY()  * (1 - height); // y position
            //Log.log("Position=["+x+":"+y+"], Size=["+width+":"+height+"], RealSize=["+actor.GetWidth()+":"+actor.GetHeight()+"]");
            //Log.log("New position ["+x+":"+y+"]");
            actor.GetPositionCoordinate().SetValue(x1,y1);
        
        }
    }
    
    private vtkMapper2D getMapper() {
        return actor.GetMapper();
    }
    
        /** Getter for property x.
     * @return Value of property x.
     */
    public double getX() {
        return x;
    }
    
    /** Setter for property x.
     * @param x New value of property x.
     */
    public void setX(double x) {
        double oldX = this.x;
        this.x = x;
        updatePosition();
        propertyChangeSupport.firePropertyChange ("x", new Double (oldX), new Double (x));
    }
    
    /** Getter for property y.
     * @return Value of property y.
     */
    public double getY() {
        return y;
    }
    
    /** Setter for property y.
     * @param y New value of property y.
     */
    public void setY(double y) {
        double oldY = this.y;
        this.y = y;
        updatePosition();
        propertyChangeSupport.firePropertyChange ("y", new Double (oldY), new Double (y));
    }
/** Getter for property customizerVisible.
 * @return Value of property customizerVisible.
 */
    public boolean isCustomizerVisible() {
        return customizerVisible;
    }
    
    /** Setter for property customizerVisible.
     * @param customizerVisible New value of property customizerVisible.
 */
    public void setCustomizerVisible(boolean customizerVisible) {
        boolean oldCustomizerVisible = this.customizerVisible;
        //if (oldCustomizerVisible == customizerVisible) return;
        this.customizerVisible = customizerVisible;
        if (!OVTCore.isServer()) {
            if (customizerVisible && customizer == null) {
                customizer = new DataScalarBarCustomizer(this, dataModule.getCore().getXYZWin());
            }
            if (customizer != null) customizer.setVisible(customizerVisible);
        }
        propertyChangeSupport.firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
    }
    
    /** Getter for property orientation.
     * @return Value of property orientation.
 */
    public int getOrientation() {
        return orientation;
    }
    
    /** Setter for property orientation.
     * @param orientation New value of property orientation.
 */
    public void setOrientation(int orientation) {
        int oldOrientation = this.orientation;
        if (actor != null) {
            switch (orientation) {
                case HORIZONTAL_ORIENTATION : 
                    getScalarBarActor().SetOrientationToHorizontal(); 
                    if (oldOrientation == VERTICAL_ORIENTATION) reorderHeightAndWidth(); 
                    break;
                case VERTICAL_ORIENTATION   : 
                    getScalarBarActor().SetOrientationToVertical(); 
                    if (oldOrientation == HORIZONTAL_ORIENTATION) reorderHeightAndWidth(); 
                    break;
                default: throw new IllegalArgumentException("Wrong orientation ("+orientation+")");
            }
            updatePosition();
        }
        this.orientation = orientation;
        propertyChangeSupport.firePropertyChange ("orientation", new Integer (oldOrientation), new Integer (orientation));
    }
    
    /** Change height <-> width */
    private void reorderHeightAndWidth() {
        double oldHeight = getHeight();
        double oldWidth = getWidth();
        setHeight(oldWidth);
        setWidth(oldHeight);
    }
    
/** Getter for property width.
 * @return Value of property width.
 */
    public double getWidth() {
        return width;
    }
    
    /** Setter for property width.
     * @param width New value of property width.
 */
    public void setWidth(double width) {
        double oldWidth = this.width;
        this.width = width;
        if (actor != null) {
            getScalarBarActor().SetWidth(width);
            updatePosition();
        }
        propertyChangeSupport.firePropertyChange ("width", new Double (oldWidth), new Double (width));
    }
    
    /** Getter for property height.
     * @return Value of property height.
 */
    public double getHeight() {
        return height;
    }
    
    /** Setter for property height.
     * @param height New value of property height.
 */
    public void setHeight(double height) {
        double oldHeight = this.height;
        this.height = height;
        if (actor != null) {
            getScalarBarActor().SetHeight(height);
            updatePosition();
        }
        propertyChangeSupport.firePropertyChange ("height", new Double (oldHeight), new Double (height));
    }
    
    public void dispose() {
        setVisible(false);
        if (customizer != null) customizer.dispose();
        super.dispose();
    }
}
