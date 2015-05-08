/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/GroundStation.java,v $
  Date:      $Date: 2003/09/28 17:52:48 $
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

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

/**
 *
 * @author  oleg
 * @version
 */
public class GroundStation extends VisualObject
    implements MenuItemsSource, PositionSource,
        TimeChangeListener, CoordinateSystemChangeListener {

    private GroundStations parent;
    private GroundStationCustomizer customizer = null;

    private vtkActor actor = null;          // ground object actor
    private vtkFollower titleActor = null;  // ground object title actor
    private vtkVectorText title = null;     // ground object title
    private double[] loc = null;            // ground object location: [0]=x, [1]=y, [2]=z
    static final double tr = 1.07;          // title rendering radius

    /** The size of the actor for scale=1 */
    private static final double normalActorSize = 0.01;
    private double scale = 1;

    /** Indicates the type of ground based station : UNKNOWN, RADAR or MAGNETOMETER */
    private int type = UNKNOWN;

    public static final int UNKNOWN = 0;
    public static final int RADAR = 1;
    public static final int MAGNETOMETER = 2;


  /** Holds value of property latitude */
    private double latitude = 0;

  /** Holds value of property longitude */
    private double longitude = 0;

  /** Holds value of property customizerVisible */
    private boolean customizerVisible;


  /* Holds value of property color. */
    private Color color = Color.black;

    /** Creates new GroundStation */
    public GroundStation(GroundStations grStations) {
        super(grStations.getCore(), "", "images/gb_station.gif");
        setParent(grStations); // this is not necesarry (Children.addChild(..) does it for us :-)
    }

  /** Creates new GroundStation */
    public GroundStation(GroundStations grStations, String name) {
        super(grStations.getCore(), name, "images/gb_station.gif");
        //parent = grStations;
    }

    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                GUIPropertyEditor editor;
                descriptors = super.getDescriptors();

        /* add property descriptor for name*/
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("name", this);
                pd.setDisplayName("Name");
                editor = new TextFieldEditor(pd);
                // Render each time user makes change by means of gui
                editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                //editor.setEditCompleteOnKey(true);
                addPropertyChangeListener("name", editor);
                pd.setPropertyEditor(editor);
                pd.setTextOnlyAccessible();
                descriptors.put(pd);

        /* add property descriptor for type*/
                pd = new BasicPropertyDescriptor("type", this);
                pd.setDisplayName("Type");
                editor = new ComboBoxPropertyEditor(pd, new int[]{UNKNOWN, RADAR, MAGNETOMETER}, new String[]{"Unknown", "Radar", "Magnetometer"});
                // Render each time user makes change by means of gui
                editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                //editor.setEditCompleteOnKey(true);
                addPropertyChangeListener("type", editor);
                pd.setPropertyEditor(editor);
                pd.setTextOnlyAccessible();
                descriptors.put(pd);

        /* add property descriptor for longitude*/
                pd = new BasicPropertyDescriptor("longitude", this);
                pd.setDisplayName("Longitude");
                //editor = new DegreesEditor(pd, 0, 360);
                 TextFieldEditor ed = new DoubleEditor(pd, 3);
                // Render each time user makes change by means of gui
                ed.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                ed.setEditCompleteOnKey(true);
                addPropertyChangeListener("longitude", ed);
                pd.setPropertyEditor(ed);
                pd.setTextOnlyAccessible();
                descriptors.put(pd);

        /* add property descriptor for latitude*/
                pd = new BasicPropertyDescriptor("latitude", this);
                pd.setDisplayName("Latitude");
                //editor = new DegreesEditor(pd, -90, 90);
                ed = new DoubleEditor(pd, 3);
                // Render each time user changes time by means of gui
                editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                ed.setEditCompleteOnKey(true);
                addPropertyChangeListener("latitude", ed);
                pd.setPropertyEditor(ed);
                pd.setTextOnlyAccessible();
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

                // scale property editor

                pd = new BasicPropertyDescriptor("scale", this);
                pd.setLabel("Size");
                pd.setDisplayName(getParent().getName()+" : "+ getName() + " size");
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

    public boolean isCustomizerVisible() {
        return this.customizerVisible;
    }

    public void setCustomizerVisible(boolean customizerVisible)
    {
        boolean oldCustomizerVisible = this.customizerVisible;
        this.customizerVisible = customizerVisible;
        if (customizerVisible  &&  customizer == null) customizer = new GroundStationCustomizer(this);
        if (customizer != null) customizer.setVisible(customizerVisible);
        propertyChangeSupport.firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
    }

    public int getType()  { return type;  }

    public void setType(int value)  {
        int old_value = value;
        this.type = value;
        propertyChangeSupport.firePropertyChange ("type", new Integer(old_value), new Integer(value));
    }

    public double getLatitude()  { return latitude;  }
    public void setLatitude(double value)
    {
        double old_value = latitude;
        latitude  = value;
        if (old_value != value)
        {
            loc = null;
            updateActorPosition();
            updateTitleActorPosition();
        }
        propertyChangeSupport.firePropertyChange ("latitude", new Double(old_value), new Double(value));
    }

    public double getLongitude() { return longitude; }
    public void setLongitude(double value)
    {
        double old_value = longitude;
        longitude = value;
        if (old_value != value) {
            loc = null;
            updateActorPosition();
            updateTitleActorPosition();
        }
        propertyChangeSupport.firePropertyChange ("longitude", new Double(old_value), new Double(value));
    }

    public JMenuItem[] getMenuItems() {
        final int ITEM_COUNT = 5;
        JMenuItem[] item = new JMenuItem[ITEM_COUNT];
        int i=0;
        item[i] = new JMenuItem("Look at");
        item[i].setEnabled(isEnabled());
        item[i].addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                if (!isVisible()) setVisible(true);
                getCore().getCamera().setViewTo(GroundStation.this);
                getCore().Render();
            }
        });

        item[++i] = null; // Separator

        item[++i] = new JMenuItem("Remove");
        item[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isVisible()) {
                    setVisible(false);
                }
                if (customizer != null) {
                    customizer.dispose();
                    customizer = null;
                }
                removeSelf();
            }
        });

        item[++i] = null; // Separator

        item[++i] = new JMenuItem("Properties...");
        item[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCustomizerVisible(true);
            }
        });

        for (i=0; i<ITEM_COUNT; i++) {
            if (item[i] != null) item[i].setFont(Style.getMenuFont());
        }
        return item;
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


    protected vtkActor getActor() {
        if (actor == null) {
            actor = new vtkActor();
            // create sphere geometry
            // may be Cone is better???
            vtkSphereSource sphere = new vtkSphereSource();
            sphere.SetRadius(1.);
            sphere.SetThetaResolution(8);
            sphere.SetPhiResolution(8);
            // map to graphics library
            vtkPolyDataMapper map = new vtkPolyDataMapper();
            map.SetInputData(sphere.GetOutput());
            map.SetInputConnection(sphere.GetOutputPort());

            // actor coordinates geometry, properties, transformation
            actor.SetMapper(map);
                float[] rgb = ovt.util.Utils.getRGB(getColor());
                actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
                actor.SetScale(normalActorSize * scale);
            updateActorPosition();
            rotateActor();
        }
        return actor;
    }

    protected vtkFollower getTitleActor() {
        if (titleActor == null)
        {
            title = new vtkVectorText();
            title.SetText(getName());
            vtkPolyDataMapper map = new vtkPolyDataMapper();
            //map.SetInputData(title.GetOutput()); FKJN 08052015
            map.SetInputConnection(title.GetOutputPort());

            titleActor = new vtkFollower();
            titleActor.SetMapper(map);
            titleActor.SetScale(0.02);
            titleActor.GetProperty().SetColor(0, 0, 0);
            titleActor.SetCamera(getRenderer().GetActiveCamera());
            updateTitleActorPosition();
        }
        return titleActor;
    }

    private double[] latlon2geo(double lat, double lon) {
        double r[] = new double[3];
        lat *= Math.PI / 180;
        lon *= Math.PI / 180;
        r[0] = Math.cos(lon) * Math.cos(lat);
        r[1] = Math.sin(lon) * Math.cos(lat);
        r[2] = Math.sin(lat);
        return r;
    }

    protected void show() {
        getRenderer().AddActor(getActor());
        getRenderer().AddActor(getTitleActor());
    }

    protected void hide() {
        getRenderer().RemoveActor(actor);
        getRenderer().RemoveActor(titleActor);
    }

    public void setVisible(boolean visible) {
        if (visible != isVisible()) {
            if (visible) {
                // listen to time and cs change only when visible! ;-)
                // do not forget to unregister myself in dispose() method
                getCore().getTimeSettings().addTimeChangeListener(this);
                getCore().getCoordinateSystem().addCoordinateSystemChangeListener(this);
                show();
            } else {
                getCore().getTimeSettings().removeTimeChangeListener(this);
                getCore().getCoordinateSystem().removeCoordinateSystemChangeListener(this);
                hide();
            }
            super.setVisible(visible);
        }
    }

    public void timeChanged(TimeEvent evt) {
        rotateActor();
        updateTitleActorPosition();
    }

    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        rotateActor();
        updateTitleActorPosition();
    }

    private void updateActorPosition() {
        if (actor != null) {
            if (loc == null) loc = latlon2geo(getLatitude(), getLongitude());
            actor.SetPosition(loc[0], loc[1], loc[2]);
        }
    }

    private void updateTitleActorPosition()
    {
        if (titleActor != null) {
            if (loc == null) loc = latlon2geo(getLatitude(), getLongitude());
            Matrix3x3 m3x3 = getTrans(getMjd()).geo_trans_matrix(getCS());
            double real_loc[] = m3x3.multiply(loc);
            titleActor.SetPosition(real_loc[0]*tr, real_loc[1]*tr, real_loc[2]*tr);
        }
    }

    /** sets actor transform matrix */
    private void rotateActor() {
        if (actor != null) {
            Matrix3x3 m3x3 = getTrans(getMjd()).geo_trans_matrix(getCS());
            actor.SetUserMatrix(m3x3.getVTKMatrix());
        }
    }

    public void setName(String value)
    {
        super.setName(value);
        //boolean oldVisible = isVisible();
        //if (oldVisible) setVisible(false);
        if (title != null) {
            title.SetText(value);
        }
        //if (oldVisible) setVisible(true);
    }

    public double[] getPosition() {
        if (loc == null) loc = latlon2geo(getLatitude(), getLongitude());
        Matrix3x3 m3x3 = getTrans(getMjd()).geo_trans_matrix(getCS());
        return m3x3.multiply(loc);
    }

    public void removeSelf() { // by oleg
        //DBG*    System.out.println("OVTObject::removeSelf()");
        OVTObject parent = this.getParent();
        if (parent == null) {
            System.err.println("OVTObject::removeSelf(): can't remove node (parent == null).");
            return;
        }
        dispose();
        parent.getChildren().remove(this);
        parent.getChildren().fireChildRemoved(this);
    }

    public void dispose() {
        setVisible(false); // this will also help to unregister as time and cs listener
        if (customizer != null) customizer.dispose();
    }
}
