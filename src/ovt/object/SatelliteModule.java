/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/SatelliteModule.java,v $
  Date:      $Date: 2003/09/28 17:52:51 $
  Version:   $Revision: 2.12 $
 
 
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

import vtk.*;

import ovt.*;
import ovt.beans.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This  that should be added
 * to { @link ovt.datatypes.Sat }.
 * @author Mykola Khotyaintsev
 * @see ovt.datatypes.AbstractSatModule
 */
public class SatelliteModule extends SingleActorSatModule implements MenuItemsSource {
    
    protected double[] spinActorPos = new double[3];
    private boolean actorIsArrow;
    /** The size of the actor for scale=1 */
    protected double normalActorSize = 0.05;
    private double scale = 1;
    public static final int DEBUG = 10;
 
    public SatelliteModule(Sat satellite) {
        super(satellite, "Sat","images/satellite.gif");
        setColor(Color.blue);
    }
    
    public boolean isSpinActorAvailable(){
        return getSat().isSpinAvailable();
    }
    
    
    private void createSatAsSphere(){
        // create sphere geometry
        actor = new vtkActor();
        vtkSphereSource sphere = new vtkSphereSource();
        sphere.SetRadius(1);
        
        sphere.SetThetaResolution(8);
        sphere.SetPhiResolution(8);
        
        // map to graphics library
        vtkPolyDataMapper map = new vtkPolyDataMapper();
        //map.SetInputData(sphere.GetOutput()); FKJN 8/5 2015
        map.SetInputConnection(sphere.GetOutputPort());
        // actor coordinates geometry, properties, transformation
        actor.SetMapper(map);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
        actor.SetScale(normalActorSize * scale);
        actorIsArrow = false;
    }
    
    private void createSatAsArrow() {
        if (isSpinActorAvailable()) {
            double[] spinVect = getSat().getSpinVectorRate();
            if (spinVect != null) {
                
                // Cluster size is 3m x 1.2m
                double cylinderHeight = 0.4;
                double cylinderRadius = 0.5;
                double coneHeight = 0.4;
                double coneRadius = 0.1;
                

               // create cylinder geometry
                vtkCylinderSource cylinder = new vtkCylinderSource();
                  cylinder.SetRadius(cylinderRadius);
                  cylinder.SetHeight(cylinderHeight);
                  cylinder.SetResolution(20);

               // create cone to indicate spin
               vtkConeSource cone = new vtkConeSource();
                  cone.SetHeight(coneHeight);
                  cone.SetRadius(coneRadius);
                  cone.SetResolution(3);

                // align cylinder along Z
                vtkTransform cylinderTransform = new vtkTransform();
                    cylinderTransform.RotateX(90);

                // align cone along Z and shift it to the top of the cone
                vtkTransform coneTransform = new vtkTransform();
                    coneTransform.Translate(0,0,cylinderHeight/2. + coneHeight/2.);
                    coneTransform.RotateY(90);
		    coneTransform.RotateZ(180);

                vtkTransformPolyDataFilter cylinderTransformPolyData = new vtkTransformPolyDataFilter();
                    cylinderTransformPolyData.SetTransform(cylinderTransform);
	            //cylinderTransformPolyData.SetInputData(cylinder.GetOutput());
	            cylinderTransformPolyData.SetInputConnection(cylinder.GetOutputPort());

                vtkTransformPolyDataFilter coneTransformPolyData = new vtkTransformPolyDataFilter();
                    coneTransformPolyData.SetTransform(coneTransform);
	            //coneTransformPolyData.SetInputData(cone.GetOutput());
	            coneTransformPolyData.SetInputConnection(cone.GetOutputPort());


                vtkAppendPolyData appendPolyData = new vtkAppendPolyData();
                    //appendPolyData.AddInputData(cylinderTransformPolyData.GetOutput()); //FKJN 8/5 2015 changed all AddInputData & SetInputData to ***InputConnection
                    //appendPolyData.AddInputData(coneTransformPolyData.GetOutput());
                    appendPolyData.AddInputConnection(cylinderTransformPolyData.GetOutputPort());
                    appendPolyData.AddInputConnection(coneTransformPolyData.GetOutputPort());              
                // map to graphics library
                vtkPolyDataMapper mapper = new vtkPolyDataMapper();
                    //mapper.SetInputData(appendPolyData.GetOutput());
                    mapper.SetInputConnection(appendPolyData.GetOutputPort());
                    
                    mapper.ScalarVisibilityOff();
                
                actor = new vtkActor();
                    actor.SetMapper(mapper);
                    float[] rgb = ovt.util.Utils.getRGB(getColor());
                    actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
                    actor.SetScale(normalActorSize * scale);
                actorIsArrow = true;
                
            }
        }
    }
    
    public void validate() {
        createSatAsArrow();
        if (actor == null) createSatAsSphere();
        super.validate();
    }
    
    public void show() {
        if (actorIsArrow != isSpinActorAvailable()) invalidate();
        if (!isValid()) validate();
        double[] r = getPosition();
        if (actor != null) actor.SetPosition(r);
        if (actorIsArrow) rotate();
        super.show();
    }
    
    
    public void rotate() {
        double[] z = {0, 0, 1};
        double[] spinv = getSat().getSpinVectorRate(); // Z-axis
        double[] rot_axis = Vect.crossn(spinv, z);
        double angle = Vect.angleOf2vect(spinv, z);
        //System.out.println("Angle = " + new java.text.DecimalFormat().format(angle));
        //spinv[0] = Math.random();
        Log.log("Spin Vector = " + Vect.toString(spinv), DEBUG);
        //actor.SetOrientation(spinv[0], spinv[1], spinv[2]);
        actor.SetOrientation(0, 0, 0);
        actor.RotateWXYZ(Utils.toDegrees(angle), rot_axis[0], rot_axis[1], rot_axis[2]);
        // -1*Utils.toDegrees(angle)?? buggy place... needs a bug-check
    }
    
    public void update() {
        if (isVisible() && isEnabled()) {
            if (actorIsArrow != isSpinActorAvailable()) {
                invalidate();
                hide();
                show();
            }
            double[] r = getPosition();     // here returns null!!!
            if (actor != null) {
                actor.SetPosition(r[0], r[1], r[2]);
                if (actorIsArrow) rotate();
            }
        }
    }
    
    public void timeChanged(TimeEvent e) {
        update();
    }
    
public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    update();
}

public Descriptors getDescriptors() {
    if (descriptors == null) {
        try {
            descriptors = super.getDescriptors();
            
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("scale", this);
            pd.setLabel("Size");
            pd.setDisplayName(getParent().getName() + " size");
            ExponentialSliderPropertyEditor sliderEditor =
               new ExponentialSliderPropertyEditor(pd, 1./16., 16., 100, new double[] {1./16, 1./4., 1, 4., 16.});
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

    public double getScale() {
        return scale;
    }
    
    public void setScale(double scale) {
        //Log.log("new scale: " + scale);
        double oldScale = scale;
        this.scale = scale;
        if (actor != null) {
            actor.SetScale(normalActorSize * scale);
        }
        firePropertyChange("scale", new Double(oldScale), new Double(scale));
    }
    
    public JMenuItem[] getMenuItems() {
        JMenuItem item1 = new JMenuItem("Look at");
        item1.setFont(ovt.gui.Style.getMenuFont());
        item1.setEnabled(isEnabled());
        item1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if (!isVisible()) setVisible(true);
                getCore().getCamera().setViewTo(getSat());
                getCore().Render();
            }
        });
        return new JMenuItem[]{ item1 };
    }
    
}


/*

    private void createSatAsArrow() {
        if (isSpinActorAvailable()) {
            double[] spinVect = getSat().getSpinVectorRate();
            if (spinVect != null) {
                
                // Cluster size is 3m x 1.2m
                double cylinderHeight = 0.4;
                double cylinderRadius = 0.5;
                double coneHeight = 0.4;
                
                vtkPlane plane1 = new vtkPlane();
                plane1.SetOrigin(0, 0, -cylinderHeight/2);
                plane1.SetNormal( 0, 0, -1);
                
                vtkPlane plane2 = new vtkPlane();
                plane2.SetOrigin(0, 0, cylinderHeight/2);
                plane2.SetNormal( 0, 0, 1);    
                
                // align cylinder along Z
                vtkTransform cylinderTransform = new vtkTransform();
                    cylinderTransform.RotateX(90);
                
                vtkCylinder cylinder = new vtkCylinder();//it's cymetry axis is Y
                    cylinder.SetRadius(cylinderRadius);
                    cylinder.SetTransform(cylinderTransform);
                
                // align cone along Z and shift it up.
                vtkTransform coneTransform = new vtkTransform();
                    coneTransform.RotateY(90);
                    coneTransform.Translate(0, 0, cylinderHeight/2 + coneHeight);
                
                vtkCone cone = new vtkCone(); //it's cymetry axis is X
                    cone.SetAngle(10);
                    cone.SetTransform(coneTransform);
                
                vtkImplicitBoolean theCone = new vtkImplicitBoolean();
                    theCone.SetOperationTypeToIntersection();
                    theCone.AddFunction( cone );
                    theCone.AddFunction( plane2 );
                
                vtkImplicitBoolean theCylinder = new vtkImplicitBoolean();
                    theCylinder.SetOperationTypeToIntersection();
                    theCylinder.AddFunction( cylinder );
                    theCylinder.AddFunction( plane1 );  
                    theCylinder.AddFunction( plane2 );  
                
                vtkImplicitBoolean theSpacecraft = new vtkImplicitBoolean();
                    theSpacecraft.SetOperationTypeToUnion();
                    theSpacecraft.AddFunction( theCone );
                    theSpacecraft.AddFunction( theCylinder );
                
                
                // iso-surface to create geometry
                vtkSampleFunction theSpacecraftSample = new vtkSampleFunction();
                    theSpacecraftSample.SetImplicitFunction( theSpacecraft );
                    theSpacecraftSample.SetModelBounds(-cylinderRadius, cylinderRadius, -cylinderRadius, cylinderRadius, -(cylinderHeight/2+coneHeight), cylinderHeight/2);
                    theSpacecraftSample.SetSampleDimensions( 10, 10, 10);
                    theSpacecraftSample.ComputeNormalsOff();
                
                vtkContourFilter theSpacecraftSurface = new vtkContourFilter();
                    theSpacecraftSurface.SetInput( theSpacecraftSample.GetOutput() );
                    theSpacecraftSurface.SetValue( 0, 0);
                
                // map to graphics library
                vtkPolyDataMapper mapper = new vtkPolyDataMapper();
                    mapper.SetInput(theSpacecraftSurface.GetOutput());
                    mapper.ScalarVisibilityOff();
                
                actor = new vtkActor();
                    actor.SetMapper(mapper);
                    float[] rgb = ovt.util.Utils.getRGB(getColor());
                    actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
                    actor.SetScale(normalActorSize * scale);
                actorIsArrow = true;
            }
        }
    }
*/
