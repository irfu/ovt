/*=========================================================================
 
Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/Camera.java,v $
Date:      $Date: 2009/10/29 23:23:43 $
Version:   $Revision: 2.15 $
 
 
Copyright (c) 2000-2009 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
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
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;

import vtk.*;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  ko
 * @version
 */
public class Camera extends BasicObject implements CameraChangeListener, TimeChangeListener, CoordinateSystemChangeListener {
    
    public static final int DEBUG = 10;
    
    public static final int VIEW_CUSTOM = 0;
    public static final int VIEW_FROM_X = 1;
    public static final int VIEW_FROM_Y = 2;
    public static final int VIEW_FROM_Z = 3;
    public static final int VIEW_FROM_MINUS_X = 4;
    public static final int VIEW_FROM_MINUS_Y = 5;
    public static final int VIEW_FROM_MINUS_Z = 6;
    public static final int VIEW_PERPENDICULAR_TO_ORBIT = 7;
    
    public static final String VIEW_TO_NORTH_HEMISPHERE = "North Hem.";
    public static final String VIEW_TO_SOUTH_HEMISPHERE = "South Hem.";
    
    /** Equals to 0. DO NOT CHANGE!*/
    public static final int PARALLEL_PROJECTION    = 0;
    /** Equals to 1. DO NOT CHANGE!*/
    public static final int PERSPECTIVE_PROJECTION = 1;
    /** Maximum camera distance from focal point */
    public static final double R_MAX = 200.;
    
    
    protected CameraCustomizer customizer;
    public vtkCamera cam;
    private vtkLight light; 
    
    protected ViewToObjects viewToObjects;
    
    /** Holds value of property customizerVisible. */
    private boolean customizerVisible;
    /** Holds value of property view. */
    protected int view;
    protected String[] viewNames           = {"Custom", "X", "Y", "Z", "-X", "-Y", "-Z"};
    protected String[] viewWithSatNames    = {"Custom", "X", "Y", "Z", "-X", "-Y", "-Z", "p. to Orbit"};
    
    static final double[][] views = {{0, 0, 0},
    { 1, 0, 0}, {0,  1, 0}, {0, 0,  0.99},
    {-1, 0, 0}, {0, -1, 0}, {0, 0, -0.99} };
    
    /** Holds value of property viewTo. */
    protected PositionSource viewTo;
    
    protected ComboBoxPropertyEditor viewToEditor;
    protected ComboBoxPropertyEditor viewFromEditor;
    protected ExponentialSliderPropertyEditor rEditor;
            
    /** Holds value of property projection. By default 
     * <CODE>PERSPECTIVE_PROJECTION</CODE> because it is by default in vtk.
     */
    private int projection = PARALLEL_PROJECTION;
    
    /** Holds value of property viewAngle. */
    private double viewAngle;    
    
    PositionSource focalPoint = new PositionSource() {
        public double[] getPosition() {
            return getFocalPoint();
        }
    };
    
    /** Creates new Camera */
    public Camera(OVTCore core) {
        super(core, "Camera");
        showInTree(false);
        Log.log("Camera :: init ...", 3);
        cam = getRenderer().GetActiveCamera();
                
        light = getCore().getRenPanel().getCameraLight();
        
        // set initial viewUp and position 
        cam.SetParallelProjection(1); // on, by default
        cam.SetViewUp(0,0,1);
        cam.SetPosition(0, R_MAX, 0);
        cam.SetFocalPoint(0, 0, 0);
        cam.SetParallelScale(8.);
        
        viewTo = core.getEarth();
        
        setViewFrom(VIEW_FROM_Y);
        
        // should be cpecified after viewTo = core.getEarth()
        viewToObjects = new ViewToObjects(this);
        
        Descriptors propertyDescriptors = new Descriptors();
        try {
        /* r property descriptor */
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("r", this);
            pd.setDisplayName("R");
            
            rEditor = new ExponentialSliderPropertyEditor(pd, 
                1.2, 150., 100, new double[]{2,10,50,150});
            // Render each time user changes time by means of gui
            rEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(rEditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener(pd.getName(), rEditor);
            addPropertyChangeListener("position", rEditor);
        
        // delta property descriptor 
            pd = new BasicPropertyDescriptor("parallelScale", this);
            pd.setDisplayName("Scale");
            ExponentialSliderPropertyEditor expSEditor = new ExponentialSliderPropertyEditor(pd, 
                0.04, 50., 101, new double[]{0.04,1,5,10,50});
            // Render each time user changes time by means of gui
            expSEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(expSEditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener(pd.getName(), expSEditor);    
            addPropertyChangeListener("position", expSEditor);
            
        // delta property descriptor 
            pd = new BasicPropertyDescriptor("delta", this);
            pd.setDisplayName("Delta");
            SliderPropertyEditor seditor = new SliderPropertyEditor(pd, -90., 90., 1., 45.);
            // Render each time user changes time by means of gui
            seditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(seditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener(pd.getName(), seditor);
            addPropertyChangeListener("position", seditor);
            
        // phi property descriptor 
            pd = new BasicPropertyDescriptor("phi", this);
            pd.setDisplayName("Phi");
            seditor = new SliderPropertyEditor(pd, 0., 360., 1., 90.);
            // Render each time user changes time by means of gui
            seditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(seditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener(pd.getName(), seditor);
            addPropertyChangeListener("position", seditor);
            
        // viewUpAngle property descriptor 
            pd = new BasicPropertyDescriptor("viewUpAngle", this);
            pd.setDisplayName("ViewUp");
            pd.setToolTipText("The angle between the camera ViewUp vector and Z axis");
            seditor = new SliderPropertyEditor(pd, -180., 180., 1., 90.);
            // Render each time user changes time by means of gui
            seditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            
            pd.setPropertyEditor(seditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener(pd.getName(), seditor);
            addPropertyChangeListener("viewUpAngle", seditor);
            addPropertyChangeListener("position", seditor);
            
        /* viewFrom property descriptor */
            pd = new BasicPropertyDescriptor("viewFrom", this);
            pd.setDisplayName("View From:");
            viewFromEditor = new ComboBoxPropertyEditor(pd,  Utils.getIndexes(viewNames), viewNames);
            // Render each time user changes time by means of gui
            viewFromEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(viewFromEditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener("viewFrom", viewFromEditor);
            
        /* viewTo property descriptor */
            pd = new BasicPropertyDescriptor("viewTo", this);
            pd.setDisplayName("View To:");
            String[] viewToObjectList = viewToObjects.getList();
            viewToEditor = new ComboBoxPropertyEditor(pd, viewToObjectList, viewToObjectList);
            // Render each time user changes time by means of gui
            viewToEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(viewToEditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener("viewTo", viewToEditor);
            
           /* viewTo property descriptor */
            pd = new BasicPropertyDescriptor("projection", this);
            pd.setDisplayName("Projection:");
            ComboBoxPropertyEditor projectionEditor = new ComboBoxPropertyEditor(pd, 
                new int[]{PARALLEL_PROJECTION ,PERSPECTIVE_PROJECTION}, 
                new String[]{ "Parallel", "Perspective"}
            );
            // Render each time user changes time by means of gui
            projectionEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                public void editingFinished(GUIPropertyEditorEvent evt) {
                    Render();
                }
            });
            pd.setPropertyEditor(projectionEditor);
            propertyDescriptors.put(pd);
            addPropertyChangeListener("projection", projectionEditor); 
            
            // camera light intensity
            pd = new BasicPropertyDescriptor("lightIntensity", this);
            pd.setDisplayName("Light Intensity:");
            
            SliderPropertyEditor sliderEditor = new SliderPropertyEditor(pd, 0., 1., 0.01, 0.2);
            addPropertyChangeListener("lightIntensity", sliderEditor);
                sliderEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
            });
            pd.setPropertyEditor(sliderEditor);
            propertyDescriptors.put(pd);
            
            if (!OVTCore.isServer()) {
            /* customizerVisible property descriptor */
                pd = new BasicPropertyDescriptor("customizerVisible", this);
                pd.setDisplayName("View Controls");
                GUIPropertyEditor ed = new VisibilityEditor(pd);
                addPropertyChangeListener("customizerVisible", ed);
                pd.setPropertyEditor(ed);
                propertyDescriptors.put(pd);
            }
            
        } catch (IntrospectionException e2) {
            System.out.println(getClass().getName() + " -> " + e2.toString());
            System.exit(0);
        }
        setDescriptors(propertyDescriptors);
        
        if (!OVTCore.isServer()) {
            customizer = new CameraCustomizer(this, getCore().getXYZWin());
            // customizer will listen to changes of visibility, etc.
            addPropertyChangeListener(customizer);
            
            // listen to VisualizationPanel camera change
            VisualizationPanel vp =  getCore().getXYZWin().getVisualizationPanel();
            vp.addCameraChangeListener(this);
        }
    }
    
    /** Getter for property position.
     * @return Value of property position.
     */
    public double[] getPosition() {
        return cam.GetPosition();
    }
    
    /** Setter for property position.
     * @param position New value of property position.
     *
     * @throws PropertyVetoException
     */
    public void setPosition(double[] position) throws IllegalArgumentException {
        double[] oldPosition = getPosition();
        
        //vetoableChangeSupport.fireVetoableChange("position", oldPosition, position);
        
        double oldViewUp = getViewUpAngle();
        Log.log("OldViewUpAngle="+getViewUpAngle(), DEBUG);
        cam.SetPosition(position[0], position[1], position[2]);
        cam.ComputeViewPlaneNormal();
        
        Log.log("After Position="+getViewUpAngle(), DEBUG);
        // restore viewUp
        setViewUpAngle(oldViewUp);
        Log.log("After restoring="+getViewUpAngle(), DEBUG);
        Log.log("-------------------------------------", DEBUG);
        
        resetClippingRange();
        
        
        
        // no more need to avoid the case of parallel view up and camera view direction vectors
        // it is avoided in setDelta()
        //cam.SetViewUp(0, 0, 1);
        
        //cam.OrthogonalizeViewUp();
        //Render();
        firePropertyChange ("position", oldPosition, position);
    }
    
    /** Set the position related to focal point */
    public void setRelativePosition(double[] position) throws IllegalArgumentException {
        setPosition(Vect.add(getFocalPoint(), position));
    }
    
    /** Set the position related to focal point */
    public double[] getRelativePosition() {
        return Vect.sub(getPosition(), getFocalPoint());
    }
    
    /** Getter for camera focalpoint position.
     * @return Value of property position.
     */
    public double[] getFocalPoint() {
        return cam.GetFocalPoint();
    }
    
    /** Setter for camera focalpoint position.
     * @param position New value of property position.
     *
     * @throws PropertyVetoException
     */
    public void setFocalPoint(double[] position) throws IllegalArgumentException {
        double[] oldPosition = getFocalPoint();
        
        //vetoableChangeSupport.fireVetoableChange("focalpoint", oldPosition, position);
        
        cam.SetFocalPoint(position[0], position[1], position[2]);
        cam.ComputeViewPlaneNormal();
        resetClippingRange();
        
        firePropertyChange ("focalpoint", oldPosition, position);
    }
    
    // its called by time-, CS-cahnge-listeners
    public void update() {
        double radius = Vect.absv(Vect.sub(getPosition(), getFocalPoint()));
        
        try {
            // update the Focal Point (specified by ViewTo)
            setFocalPoint(viewTo.getPosition());
            
            // update camera position
            
            switch (getViewFrom()) {
                case VIEW_CUSTOM: 
                    break;
                case VIEW_FROM_X:
                case VIEW_FROM_Y:
                case VIEW_FROM_MINUS_X:
                case VIEW_FROM_MINUS_Y:
                    setRelativePosition(Vect.multiply(views[getViewFrom()], radius));
                    cam.SetViewUp(0,0,1);
                    break;
                case VIEW_FROM_Z:
                    setDelta(90.);
                    cam.SetViewUp(1,0,0);
                    break;
                case VIEW_FROM_MINUS_Z:
                    setDelta(-90.);
                    cam.SetViewUp(1,0,0);
                    break;
                case VIEW_PERPENDICULAR_TO_ORBIT:
                    // we are in the sattelite following mode
                    // and look always perpendicular to orbit
                    Sat sat = (Sat)viewTo;
                    double[] n = normalToOrbit(sat);
                    
                    Trajectory tr = sat.getTrajectory();
                    double[] r = tr.get(getMjd()).get(getCS());
                    
                    // we can view at the orbit plane from 2 sides.
                    // determine from which side we are looking now and choose it.
                    // r_cam - vector starting at sat to current position
                    double[] r_cam = Vect.sub(getPosition(), r);
                    
                    // get Cos(alpha). - the angle between n and r_cam
                    // 0 < cos < 1  - camera is in n direction side (+)
                    // -1 < cos < 0 -  (-)
                    
                    double cos_alpha = Vect.cosAngle(r_cam, n);
                    int sign;
                    if (cos_alpha >= -1  &&  cos_alpha < 0) sign = -1;
                    else sign = 1;
                    
                    double[] cam_pos = Vect.add(r, Vect.multiply(n, radius * (double)sign));
                    
                    // camera position
                    setPosition(cam_pos);
                    break;
            }
        } catch (IllegalArgumentException e) { System.out.println("WASSUP?? Unexpected "+e);}
    }

    /** computes normal vector to orbit in specyfied point r */
public double[] normalToOrbit(Sat sat) {
    // workaround extream conditions
    Trajectory tr = sat.getTrajectory();
    double[] r = tr.get(getMjd()).get(getCS());
    TimeSet timeSet = getCore().getTimeSettings().getTimeSet();
    
    int pos = timeSet.indexOf(getMjd());
    double[] timeMap = timeSet.getValues();
    
    double mjd_a, mjd_b;
    if (pos == 0) {
        mjd_a = timeSet.get(2);
        mjd_b = timeSet.get(1);
    } else if (pos == timeSet.getNumberOfValues() - 1) {
        mjd_a = timeSet.get(timeSet.getNumberOfValues() - 2);
        mjd_b = timeSet.get(timeSet.getNumberOfValues() - 3);
    } else {
        mjd_a = timeSet.get(pos - 1);
        mjd_b = timeSet.get(pos + 1);
    }
    
    double[] r_a = tr.get(mjd_a).get(getCS());;
    double[] r_b = tr.get(mjd_b).get(getCS());;
    
    double[] a = Vect.sub(r_a, r);
    double[] b = Vect.sub(r_b, r);
    
    // compute the normalized vector, orthogonal to a and b
    double[] n = Vect.crossn(a, b);
    
    return n;
}

public void reset() {
    getRenderer().ResetCamera();
    setViewFrom(VIEW_CUSTOM);
    setViewTo(focalPoint); // hmm... let it be.
    firePropertyChange("position", null, null);
}

    /** Setter for property r.
     * @param r New value of property r.
     *
     * @throws IllegalArgumentException
     */
public void setR(double r) throws IllegalArgumentException {
    double oldR = getR();
    //Log.log("R="+r+ " oldR="+oldR);
    if (r < 1 && getViewTo().equals("Earth")) throw new IllegalArgumentException("R < 1");
    if (r < 0) throw new IllegalArgumentException("R < 0");
    //vetoableChangeSupport.fireVetoableChange("r", new Double (oldR), new Double (r));
    double[] r_delta_phi = Utils.rec2sph(getRelativePosition());
    r_delta_phi[0] = r;
    setRelativePosition(Utils.sph2rec(r_delta_phi));
    propertyChangeSupport.firePropertyChange ("r", new Double (oldR), new Double (r));
}
    /** Getter for property r.
     * @return Value of property r.
     */
public double getR() {
    return Utils.rec2sph(getRelativePosition())[0];
}

    /** Setter for property delta.
     * @param delta New value of property delta.
     *
     * @throws IllegalArgumentException
     */
public void setDelta(double delta) throws IllegalArgumentException {
    if ((delta - 90) % 180 == 0) { // avoid delta = 90
        if (delta>0) delta -= 0.0001;
        else delta += 0.0001;
    }
    double olddelta = getDelta();
    //vetoableChangeSupport.fireVetoableChange("delta", new Double (olddelta), new Double (delta));
    double[] r_delta_phi = Utils.rec2sph(getRelativePosition());
    r_delta_phi[1] = delta;
    setRelativePosition(Utils.sph2rec(r_delta_phi));
    propertyChangeSupport.firePropertyChange ("delta", new Double (olddelta), new Double (delta));
    cameraChanged(new CameraEvent());
}

    /** Getter for property delta.
     * @return Value of property delta.
     */
public double getDelta() {
    return Utils.rec2sph(getRelativePosition())[1];
}

    /** Setter for property phi.
     * @param phi New value of property phi.
     *
     * @throws IllegalArgumentException
     */
public void setPhi(double phi) throws IllegalArgumentException {
    double oldPhi = getPhi();
    //vetoableChangeSupport.fireVetoableChange("phi", new Double (oldPhi), new Double (phi));
    double[] r_delta_phi = Utils.rec2sph(getRelativePosition());
    r_delta_phi[2] = phi;
    setRelativePosition(Utils.sph2rec(r_delta_phi));
    propertyChangeSupport.firePropertyChange ("phi", new Double (oldPhi), new Double (phi));
    cameraChanged(new CameraEvent());
}

    /** Getter for property phi.
     * @return Value of property phi.
     */
public double getPhi() {
    return Utils.rec2sph(getRelativePosition())[2];
}

public void coordinateSystemChanged(CoordinateSystemEvent evt) {
    update();
}

public void timeChanged(TimeEvent evt) {
    update();
}

    /** Setter for property viewUp.
     * @param viewUp New value of property viewUp.
     *
     * @throws IllegalArgumentException
     */
public void setViewUp(double[] viewUp) throws IllegalArgumentException {
    double[] oldViewUp = getViewUp();
    //vetoableChangeSupport.fireVetoableChange("viewUp", oldViewUp, viewUp);
    cam.SetViewUp(viewUp[0], viewUp[1], viewUp[2]);
    propertyChangeSupport.firePropertyChange ("viewUp", oldViewUp, viewUp);
}

    /** Getter for property viewUp.
     * @return Value of property viewUp.
     */
public double[] getViewUp() {
    return cam.GetViewUp();
}

    /** Setter for property customizerVisible.
     * @param customizerVisible New value of property customizerVisible.
     */
public void setCustomizerVisible(boolean customizerVisible) {
    boolean oldCustomizerVisible = this.customizerVisible;
    if (!OVTCore.isServer()) {
        customizer.setVisible(customizerVisible);
        this.customizerVisible = customizerVisible;
        propertyChangeSupport.firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
    }
}
    /** Getter for property customizerVisible.
     * @return Value of property customizerVisible.
     */
public boolean isCustomizerVisible() {
    return customizerVisible;
}

/** This property should be marked as hidden - so it will be hidden for a server version */
public CameraCustomizer getCustomizerWindow() {
    return customizer;
}

    /** Setter for property view.
     * @param view New value of property view.
     */
public void setViewFrom(int view) throws IllegalArgumentException {
    // view == 0  means no special view - "Custom"
    int oldView = this.view;
    this.view = view;
    Log.log("viewFrom : " + view + " oldViewFrom: " + oldView, 8);
    if (view != VIEW_CUSTOM) update();
    propertyChangeSupport.firePropertyChange ("viewFrom", new Integer (oldView), new Integer (view));
}
    /** Getter for property view.
     * @return Value of property view.
     */
public int getViewFrom() {
    return view;
}


    /** Set the view to <CODE>Earth</CODE>, <CODE>SatName</CODE>, or <CODE>Custom</CODE>
     * @param viewTo New value of property viewTo.
     *
     * @throws IllegalArgumentException
     */
public void setViewTo(PositionSource viewTo) throws IllegalArgumentException {
    PositionSource oldViewTo = this.viewTo;
    Log.log("viewTo : " + viewTo + " oldViewTo: " + oldViewTo, 8);
    this.viewTo = viewTo;
    // change items in viewFromEditor if
    if (oldViewTo instanceof Sat) {
        if (!(viewTo instanceof Sat)) {
            // Sat -> Not Sat  =  remove "Orbit item"
            setViewFrom(VIEW_CUSTOM);
            viewFromEditor.setValues(Utils.getIndexes(viewNames));
            viewFromEditor.setTags(viewNames);
        }
    } else {
        if (viewTo instanceof Sat) {
            // Not Sat -> Sat  =  add "Orbit item"
            viewFromEditor.setValues(Utils.getIndexes(viewWithSatNames));
            viewFromEditor.setTags(viewWithSatNames);
        }
        
    } 
    
    if (viewTo.equals(getCore().getEarth()) && !viewTo.equals(getCore().getEarth())) {
        // change R bounds in R Slider Editor
        rEditor.setMinimumValue(1.2);
    } else if (oldViewTo.equals(getCore().getEarth()) && !viewTo.equals(getCore().getEarth())) {
        // change R bounds in R Slider Editor
        rEditor.setMinimumValue(0.01);
    } 
    
    if (viewTo instanceof GroundStation) { // if view is to Groundbased object
        
        setViewFrom(VIEW_CUSTOM);
        // if a ground object, view to this from up [  ).<---  ]
        double[] obj_pos = viewTo.getPosition();
        double cam_radius = Vect.absv(getPosition());
        if (cam_radius < 2) cam_radius = 2;
        double[] pos = Vect.multiply( obj_pos,  cam_radius / Vect.absv(obj_pos) );
        setPosition(pos);
    }
    
    update();
    propertyChangeSupport.firePropertyChange ("viewTo", oldViewTo, viewTo);
}
    /** Getter for property viewTo.
     * @return Value of property viewTo.
     */
public PositionSource getViewTo()
{ return viewTo; }

public void resetClippingRange() {
    //getRenderer().ResetCameraClippingRange(-1000,1000,-1000,1000,-1000,1000);
    //getRenderer().ResetCameraClippingRange();
    cam.SetClippingRange(0.0001, 1000.01);
    //System.out.println("Reset camera clipping");
}

public void cameraChanged(CameraEvent evt) {
    //System.out.println("Camera change");
    resetClippingRange();
    
    // this is not a good way to do..
    firePropertyChange("position", null, null);
    
    if ((getViewFrom() >= VIEW_FROM_X) && (getViewFrom() <= VIEW_FROM_MINUS_Z)) {
        double[] view = views[getViewFrom()];
        double[] pos = getRelativePosition();
        for (int i=0; i<3; i++) {
            if (view[i] == 0) {
                
                    if (Math.abs(pos[i]) > 0.005) {
                        //System.out.println("Set vievFrom=VIEW_CUSTOM because pos["+i+"]="+pos[i]);
                        setViewFrom(VIEW_CUSTOM);
                        break;
                    }
                
            }
        }
    }
    
    if ((getViewFrom() == VIEW_PERPENDICULAR_TO_ORBIT)) {
        Sat sat = (Sat)viewTo;
        double eps = 0.002;
        double[] n1 = normalToOrbit(sat);
        double[] n2 = Vect.multiply(n1, -1);
        double[] n = Vect.norm(Vect.sub(getPosition(), getFocalPoint()));
        if (!Vect.equal(n, n1, eps) && !Vect.equal(n, n2, eps)) {
                setViewFrom(VIEW_CUSTOM);
        }
    }
    
    if (!getViewTo().equals(focalPoint)) { // != "Custom"
        double[] foc = getFocalPoint();
        double[] obj_pos = viewTo.getPosition();
        for (int i=0; i<3; i++) {
            if (obj_pos[i] != foc[i]) {
                    //System.out.println("Set viewTo=Custom");
                    setViewTo(focalPoint); // "Custom"
                break;
            }
        }
    }
}

public PropertyChangeListener getViewToObjectsNameChangeListener() {
    return viewToObjects.objectNameChangeListener;
}

public PropertyChangeListener getViewToObjectsVisibilityChangeListener() {
    return viewToObjects.objectVisibilityChangeListener;
}

// ---------- quick views ------

/** for north z>1. For south hem z<-1 */
public void lookAtHemisphere(double z) {
        setViewTo(getCore().getEarth()); 
        double[] z_axe = {0, 0, z};
        double[] up = { 1, 0, 0};
        Matrix3x3 m3x3 = getCore().getTrans(this.getMjd()).trans_matrix(getPolarCS(), getCS());
        setPosition(m3x3.multiply(z_axe));
        //setViewUp(m3x3.multiply(up));
        setViewUp(up);
}

/** quick view at north hem. */
public void lookAtNothHemisphere() {
    lookAtHemisphere(getR());
}

/** quick view at south hem. */
public void lookAtSouthHemisphere() {
    lookAtHemisphere(-1.*getR());
}

/** Getter for property lightIntensity.
 * @return Value of property lightIntensity.
 */
public double getLightIntensity() {
    return light.GetIntensity();
}

/** Setter for property lightIntensity.
 * @param lightIntensity New value of property lightIntensity.
 */
public void setLightIntensity(double lightIntensity) {
    double oldLightIntensity = getLightIntensity();
    light.SetIntensity(lightIntensity);
    propertyChangeSupport.firePropertyChange("lightIntensity", new Double(oldLightIntensity), new Double(lightIntensity));
}

/** Getter for property projection.
 * @return Value of property projection.
 */
public int getProjection() {
    return this.projection;
}

/** Setter for property projection.
 * @param projection New value of property projection.
 */
public void setProjection(int projection) {
    int oldProjection = this.projection;
    if (projection == oldProjection) return; // no change 
    this.projection = projection;
    switch (projection) {
        case PARALLEL_PROJECTION    : 
            // derive scale from R
            double scale = getR()*Math.tan(Utils.toRadians(0.5*getViewAngle()));
            setParallelScale(scale);
            setR(R_MAX); // move camera away ! This cause STRANGE clipping of objects!
            cam.SetParallelProjection(1); 
            break;
        case PERSPECTIVE_PROJECTION : 
            // derive R from scale
            double r = getParallelScale()/Math.tan(Utils.toRadians(0.5*getViewAngle()));
            setR(r);
            cam.SetParallelProjection(0); 
            break;
        default: throw new IllegalArgumentException("Wrong projection ("+projection+")");
    }
     // 1 is on I hope
    propertyChangeSupport.firePropertyChange("projection", new Integer(oldProjection), new Integer(projection));
}

/** Getter for property parallelScale.
 * @return Value of property parallelScale.
 */
public double getParallelScale() {
    return cam.GetParallelScale();
}

/** Setter for property parallelScale.
 * @param parallelScale New value of property parallelScale.
 */
public void setParallelScale(double parallelScale) {
    double oldParallelScale = getParallelScale();
    cam.SetParallelScale(parallelScale);
    propertyChangeSupport.firePropertyChange("parallelScale", new Double(oldParallelScale), new Double(parallelScale));
}


/** Getter for viewUpAngle.
 * ViewUp angle is the angle between the camera view up vector 
 * and the perpendicular to the direction of view component of Z axis (0,0,1) 
 * @return viewUpAngle in degrees.
 */
public double getViewUpAngle() {
    double[] Z = {0,0,1};
    double[] viewDirection = Vect.sub(cam.GetFocalPoint(),cam.GetPosition());
    Log.log("viewDirection="+Vect.toString(viewDirection), DEBUG);
    // 3-rd basis vector
    double[] e3 = Vect.norm(viewDirection);
    Log.log("e3="+Vect.toString(e3), DEBUG);
    // get perpendicular to viewDirection component of Z
    // Zperp = Z - e3(Z*e3)
    double[] Zperp = Vect.sub(Z,Vect.multiply(e3,Vect.dot(Z,e3)));
    double[] e1 = Vect.norm(Zperp);
    // e2
    //double[] e2 = Vect.crossn(e3,e1);
    
    double[] viewUp = cam.GetViewUp();
    Log.log("cam.GetViewUp()="+Vect.toString(viewUp), DEBUG);
    Log.log("Vect.dot(viewUp,e3)"+Vect.dot(viewUp,e3),DEBUG);
    // compute viewUp in e1-e2 plane
    double[] viewUp_e1e2 = Vect.sub(viewUp, Vect.multiply(e3,Vect.dot(viewUp,e3)));
    Log.log("viewUp_e1e2="+Vect.toString(viewUp_e1e2), DEBUG);
    
    if (Vect.absv2(viewUp_e1e2) == 0) {
        if (Vect.dot(viewUp,e3) > 0) return 0;
        else return 180.;
    }
    
    double angleRad = Vect.angleOf2vect(e1, viewUp_e1e2,viewDirection);
    Log.log("ViewUpAngleRad="+angleRad, DEBUG);
    // the result is in (-pi,pi) region, but wee need (0,360)
    //Log.log("anle="+Utils.toDegrees(angleRad)+"");
    //if (angleRad <0) angleRad += 2.*Math.PI;
    //Log.log("afteranle="+Utils.toDegrees(angleRad)+"");
    return Utils.toDegrees(angleRad);
}

/** Setter for viewUpAngle (in degrees).
 * ViewUp angle is the angle between the camera view up vector
 * and the perpendicular to the direction of view component of Z axis (0,0,1) 
 * @param viewUpAngle (in degrees)
 */
public void setViewUpAngle(double viewUpAngle) {
    double oldViewUpAngle = getViewUpAngle();
    
    double angleRad = Utils.toRadians(viewUpAngle);
    
    double[] Z = {0,0,1};
    double[] viewDirection = Vect.sub(cam.GetFocalPoint(),cam.GetPosition());
    // 3-rd basis vector
    double[] e3 = Vect.norm(viewDirection);
    // get perpendicular to viewDirection component of Z
    // Zperp = Z - e3(Z*e3)
    double[] Zperp = Vect.sub(Z,Vect.multiply(e3,Vect.dot(Z,e3)));
    double[] e1 = Vect.norm(Zperp);
    // e2
    double[] e2 = Vect.crossn(e3,e1);
    
    double[] viewUp = Vect.add(Vect.multiply(e1,Math.cos(angleRad)),
                               Vect.multiply(e2,Math.sin(angleRad)));
    
    cam.SetViewUp(viewUp[0],viewUp[1],viewUp[2]);
    
    propertyChangeSupport.firePropertyChange("viewUpAngle", new Double(oldViewUpAngle), new Double(viewUpAngle));
}


/** Getter for property viewAngle.
 * @return Value of property viewAngle.
 */
public double getViewAngle() {
  return cam.GetViewAngle();
}

/** Setter for property viewAngle.
 * @param viewAngle New value of property viewAngle.
 */
public void setViewAngle(double viewAngle) {
  double oldViewAngle = getViewAngle();
  cam.SetViewAngle(viewAngle);
  propertyChangeSupport.firePropertyChange ("viewAngle", new Double (oldViewAngle), new Double (viewAngle));
}

}


/* *******************************************************************/
/* *******************************************************************/
/* *******************************************************************/
class ViewToObjects {
    protected Camera cam;
    /** Contains Camera FocalPoint, Earth, all visible Sats, GroundbasedStations */
    protected Vector visibleObjects = new Vector();
    
    /** Constructor */
    ViewToObjects(Camera cam) {
        this.cam = cam;
        // add stationary objects
        visibleObjects.add(cam.focalPoint); // "Custom ViewTo"
        visibleObjects.add(cam.getCore().getEarth()); 
        // take care about dynamic objects - Sats, GroundBasedStations
        cam.getCore().getSats().getChildren().addChildrenListener(satsChildrenListener);
        cam.getCore().getSats().getClusterSats().getChildren().addChildrenListener(satsChildrenListener);
        
        cam.getCore().getGroundBasedStations().getChildren().addChildrenListener(groundbasedChildrenListener);
        //listenToAllGroundBasedStationsChildrenChanges(cam.getCore().getGroundBasedStations());
        //updateViewToComboBoxEditorList();
    }
    
    /** obj is a start object form the tree */
    private void watchVisibilityOnAllPositionSourceObjectsIn(OVTObject root) {
        //Log.log("watchVisibilityOnAllPositionSourceObjectsIn("+root.getName()+")");
        // register myself as a listener to "visible" property of each
        // visualobject which implements PositionSource interface
        Enumeration e = root.getVisualChildren().elements();
        while (e.hasMoreElements()) {
            VisualObject obj = (VisualObject) e.nextElement();
            if (obj instanceof PositionSource) {
                // listen to all visual objects "visibility" state
                obj.addPropertyChangeListener("visible", objectVisibilityChangeListener);
                // listen to objects "name" change
                obj.addPropertyChangeListener("name", objectNameChangeListener);
                if (obj.isVisible() && !visibleObjects.contains(obj)) 
                    visibleObjects.addElement(obj);
            }
        }
    }
    
    
    private void listenToAllGroundBasedStationsChildrenChanges(GroundStations gbs) {
        //Log.log("listenToAllGroundBasedStationsChildrenChanges("+gbs.getName()+")");
        //Log.log("\t\t size="+gbs.getChildren().size());
        gbs.getChildren().addChildrenListener(groundbasedChildrenListener);
        Enumeration e = gbs.getChildren().elements();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            if (obj instanceof GroundStations) 
                listenToAllGroundBasedStationsChildrenChanges((GroundStations)obj);
        }
    }

    /** */
    private void updateViewToComboBoxEditorList() {
        cam.viewToEditor.setValues(visibleObjects.toArray());
        cam.viewToEditor.setTags(getList());
    }
    
    /*public Sat getSat(String name) {
        return (Sat)visibleObjects.get(name);
    }*/
    
    public String[] getList() {
        int n = visibleObjects.size();
        String[] list = new String[visibleObjects.size()];
        Enumeration e = visibleObjects.elements();
        e.nextElement(); // skip cam.focalPoint - "Custom"
        list[0] = "Custom";
        int i=1;
        while(e.hasMoreElements())
            list[i++] = ((OVTObject)e.nextElement()).getName();
        return list;
    }
    
    
    public PropertyChangeListener objectNameChangeListener  = new PropertyChangeListener() {
        
    /** If the object's name changes - update it in the list.
    */
        public void propertyChange(PropertyChangeEvent evt) {
            updateViewToComboBoxEditorList();
        }
    };
    
    public PropertyChangeListener objectVisibilityChangeListener  = new PropertyChangeListener() {
        
        /** If the object becomes visible - add it to the list of visible visual objects,
         * else - remove.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            VisualObject obj = (VisualObject)evt.getSource();
            boolean visible = ((Boolean)evt.getNewValue()).booleanValue();
            
            // if some sat comes visible - add it.
            if (visible) {
                //System.out.println("Adding item -" + obj.getName());
                if (!visibleObjects.contains(obj)) visibleObjects.addElement(obj);
            } else {
                // if we remove sat from list we set watched object to earth
                if (ViewToObjects.this.cam.getViewTo().equals(obj)) {
                        ViewToObjects.this.cam.setViewTo(
                            ViewToObjects.this.cam.getCore().getEarth()
                        );
                }
                visibleObjects.removeElement(obj);
                
                // if no sats left, and user was watching the sat
                if (visibleObjects.size() == 0  &&  ViewToObjects.this.cam.getViewFrom() == ViewToObjects.this.cam.VIEW_PERPENDICULAR_TO_ORBIT) {
                        ViewToObjects.this.cam.setViewFrom(ViewToObjects.this.cam.VIEW_CUSTOM);
                }
            }
            updateViewToComboBoxEditorList();
        }
};

    private ChildrenListener satsChildrenListener = new ChildrenListener() {
            /** fired when child was added */
            public void childAdded(ChildrenEvent evt) {
                watchVisibilityOnAllPositionSourceObjectsIn(evt.getChild());
                updateViewToComboBoxEditorList();
            }

            /** fired when child was removed */
            public void childRemoved(ChildrenEvent evt) {
                // do nothing here. The object will remove us from listeners by itself
            }

            /** fired when children/children number was changed */
            public void childrenChanged(ChildrenEvent evt) {
                watchVisibilityOnAllPositionSourceObjectsIn(((Children)evt.getSource()).getParent());
                updateViewToComboBoxEditorList();
            }
        };
    private ChildrenListener groundbasedChildrenListener = new ChildrenListener() {
            /** fired when child was added */
            public void childAdded(ChildrenEvent evt) {
                if (evt.getChild() instanceof GroundStations) {
                    evt.getChild().getChildren().addChildrenListener(
                        ViewToObjects.this.groundbasedChildrenListener 
                    );
                }
                watchVisibilityOnAllPositionSourceObjectsIn(evt.getChild());
                updateViewToComboBoxEditorList();
            }

            /** fired when child was removed */
            public void childRemoved(ChildrenEvent evt) {
                // do nothing here. The object will remove us from listeners by itself
            }

            /** fired when children/children number was changed */
            public void childrenChanged(ChildrenEvent evt) {
                // children of GroundbaseStations could only have changed
                // GroundbasedStation has no children by def. :o)
                listenToAllGroundBasedStationsChildrenChanges((GroundStations)((Children)evt.getSource()).getParent());
                watchVisibilityOnAllPositionSourceObjectsIn(((Children)evt.getSource()).getParent());
                updateViewToComboBoxEditorList();
            }
        };
}

