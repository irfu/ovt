/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/FrameModule.java,v $
  Date:      $Date: 2003/09/28 17:52:48 $
  Version:   $Revision: 2.8 $
 
 
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
 * FrameModule.java
 *
 * Created on March 22, 2000, 10:22 AM
 */

package ovt.object;

import ovt.*;
import ovt.mag.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.lang.reflect.*;
import java.awt.*;

/**
 *
 * @author  root
 * @version
 */
public class FrameModule extends VisualObject {
    
    public static final int XOYPLANE = 1;
    public static final int XOZPLANE = 2;
    public static final int YOZPLANE = 3;
    
    /** Holds value of property representation. */
    private int representation = RepresentationEditor.WIREFRAME;
    /** Holds value of property opacity. */
    private double opacity = 1.;
    /* Holds value of property color. */
    private Color color = Color.gray;
    
    protected vtkActor actor = null;
    
    private Frames parent;
    
    protected int type = XOYPLANE;
    
    /** Holds value of property position. */
    //private int position = 0;
    
/** Creates new FrameModule */
public FrameModule(Frames frames, int type) {
    super(frames.getCore(), getName(type), "images/frame.gif");
    parent = frames;
    setType(type);
}

public static String getName(int type) {
    switch (type) {
        case XOYPLANE : return "XOY";
        case YOZPLANE : return "YOZ";
        case XOZPLANE : return "XOZ";
        default: return "no name";
    }
}

protected void setType(int type) {
    if (type == XOYPLANE || type == XOZPLANE || type == YOZPLANE) {
        this.type = type;
    } else throw new IllegalArgumentException("type is not XOYPLANE || XOZPLANE || YOZPLANE");
}

public int getType() {
    return type;
}

/** l - number of cells, dx - cell size */
protected vtkActor createActor(int l, int dx) {
    int i;

    vtkFloatArray xCoords = new vtkFloatArray();
    xCoords.SetNumberOfValues(l+1);
    vtkFloatArray yCoords = new vtkFloatArray();
    yCoords.SetNumberOfValues(l+1);
    vtkFloatArray zCoords = new vtkFloatArray();
    zCoords.SetNumberOfValues(l+1);

    for (i=0; i<l+1; i++) xCoords.InsertValue(i,(i-l/2)*dx);


    for (i=0; i<l+1; i++) yCoords.InsertValue(i,(i-l/2)*dx);


    for (i=0; i<l+1; i++) zCoords.InsertValue(i,(i-l/2)*dx);

    vtkRectilinearGrid rgrid = new vtkRectilinearGrid();

    rgrid.SetDimensions(l+1,l+1,l+1);
    rgrid.SetXCoordinates(xCoords);
    rgrid.SetYCoordinates(yCoords);
    rgrid.SetZCoordinates(zCoords);

    vtkRectilinearGridGeometryFilter plane = new vtkRectilinearGridGeometryFilter();
    plane.SetInputData(rgrid);

    switch (getType()) {
        case XOYPLANE : plane.SetExtent(0,     l,   0,   l, l/2, l/2); break;
        case YOZPLANE : plane.SetExtent(l/2, l/2,   0,   l,   0,   l); break;
        case XOZPLANE : plane.SetExtent(0,     l, l/2, l/2,   0,   l); break;
    }

    vtkPolyDataMapper gridMapper = new vtkPolyDataMapper();
    gridMapper.SetInputConnection(plane.GetOutputPort());

    vtkActor actorTmp = new vtkActor();
        actorTmp.SetMapper(gridMapper);
        actorTmp.GetProperty().SetRepresentation(this.representation);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        actorTmp.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
        actorTmp.GetProperty().SetOpacity(this.opacity);	
    //actor.GetProperty().SetAmbientColor (0, 0, 0);
    //actor.GetProperty().SetSpecularColor(0, 0, 0);
    //actor.GetProperty().SetDiffuseColor (0, 0, 0);
        actorTmp.SetScale((double)parent.getCellSize());
    setActorPosition(actorTmp, getPosition() * parent.getCellSize());
    return actorTmp;
}

protected vtkActor getActor() {
    if (actor == null) actor = createActor(parent.getCellsNumber(), 1);
    return actor;
}

protected void show() {
    getRenderer().AddActor(getActor());
}

protected void hide() {
    getRenderer().RemoveActor(getActor());
}

public void setVisible(boolean visible) {
    if (visible != isVisible()) {
        if (visible) show();
        else hide();
        super.setVisible(visible);
    }
}

private void setActorPosition(vtkActor actor, double pos) {
    switch(getType()) {
        case XOYPLANE: actor.SetPosition(0, 0, pos); break;
        case XOZPLANE: actor.SetPosition(0, pos, 0); break;
        case YOZPLANE: actor.SetPosition(pos, 0, 0); break;
    }
}

public void propertyChange(PropertyChangeEvent evt) {
    if (!isVisible()) {
        actor = null;
        return;
    }

    String prop = evt.getPropertyName();
    //System.out.println("FrameModule: Property changed: " + prop);
    if (prop == "cellsNumber") {
        //System.out.println("recreating actor...");
        int oldCellsNumber = ((Integer)evt.getOldValue()).intValue();
        int newCellsNumber = ((Integer)evt.getNewValue()).intValue();
        if (newCellsNumber != oldCellsNumber) {
            //if (Math.abs(getPosition()) > Math.abs(newCellsNumber)/2) {
            //    parent.setYOZPosition(newCellsNumber / 2 * (getPosition() > 0 ? 1 : -1));
            //}
            hide();
            actor = createActor(newCellsNumber, 1);
            show();
            Render();
        }
    }
    if (prop == "cellSize") {
        if (actor != null) {
            actor.SetScale((double)parent.getCellSize());
            setActorPosition(actor, getPosition() * parent.getCellSize());
            Render();
        }
    }

    if (prop == "YOZPosition") {
        if (type == YOZPLANE && actor != null) {
            int oldPos = ((Integer)evt.getOldValue()).intValue();
            int newPos = ((Integer)evt.getNewValue()).intValue();
            if (newPos != oldPos) {
                setActorPosition(actor, newPos * parent.getCellSize());
                Render();
            }
        }
    }
}

/** Getter for property position.
 * @return Value of property position.
 */
public int getPosition() {
    return (type == YOZPLANE) ? parent.getYOZPosition() : 0;
}

    /** Getter for property color.
 * @return Value of property color.
 */
public Color getColor() {
    return color;
}

/** Setter for property color.
 * @param color New value of property color.
 */
public void setColor(Color color) {
    Color oldColor = color;
    this.color = color;
    firePropertyChange("color", oldColor, color);
    //super(color);
    if (actor != null){
        float[] rgb = ovt.util.Utils.getRGB(color);
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
    }
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
            pd.setDisplayName(getName()+" Opacity");
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
            
            // color property descriptor

            pd = new BasicPropertyDescriptor("color", this);
            pd.setLabel("Color");
            pd.setDisplayName(getName()+" color");

            ComponentPropertyEditor colorEditor = new ColorPropertyEditor(pd);
            colorEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            addPropertyChangeListener("color", colorEditor);
            pd.setPropertyEditor(new WindowedPropertyEditor(colorEditor, getCore().getXYZWin(), "Close"));
            descriptors.put(pd);


        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());
            System.exit(0);
        }
    }
    return descriptors;
}
    
}
