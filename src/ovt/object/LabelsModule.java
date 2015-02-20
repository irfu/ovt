/*=========================================================================
Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/LabelsModule.java,v $
Date:      $Date: 2006/02/20 16:06:39 $
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

/*
 * LablesModule.java
 *
 * Created on November 3, 2000, 8:59 PM
 */

package ovt.object;

import vtk.*;

import ovt.*;
import ovt.gui.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class LabelsModule extends SingleActorSatModule implements MenuItemsSource {
    Vector labels = new Vector();
    Vector marks = new Vector();
    LabelsModuleCustomizer customizer = null;
    ComboBoxPropertyEditor gapEditor = null;
    
    /* Bean: Font */
    //private OVTFont font = new OVTFont(this);

    /** Holds value of property customizerVisible. */
    private boolean customizerVisible = false;
    
    /** Holds value of property gap. */
    private int gap = 1;
    private double scale = 1;
    
    /** The size of the actor for scale=1 */
    protected double normalActorSize = 0.1;
    
    
  /** Creates new LabelsModule */
    public LabelsModule(Sat satellite) {
        super(satellite, "Labels", "images/orbit_labels.gif");
        setColor(Color.black);
    }
    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = super.getDescriptors();
                
        /* scale Property Descriptor */
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("scale", this);
                pd.setDisplayName("Labels size");
                pd.setMenuAccessible(false);
                ExponentialSliderPropertyEditor scaleEditor = new ExponentialSliderPropertyEditor(pd, 
                    1./8., 8., new double[]{1./8.,1./2., 1, 2, 8});
                scaleEditor.setFrameOwner(getCore().getXYZWin());    // for better initial position
                scaleEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                addPropertyChangeListener("scale", scaleEditor);
                pd.setPropertyEditor(scaleEditor);
                descriptors.put(pd);
                
        /* gap Property Descriptor */
                pd = new BasicPropertyDescriptor("gap", this);
                pd.setDisplayName("Label each ");
                pd.setMenuAccessible(false);
                gapEditor = new ComboBoxPropertyEditor(pd,  gapValues(), gapTags());
                gapEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                addPropertyChangeListener("gap", gapEditor);
                pd.setPropertyEditor(gapEditor);
                descriptors.put(pd);

            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }

    //public OVTFont getFont() {
    //    return font;
    //}
    
    protected void show() {
        if (!isValid()) validate();
        Enumeration e = labels.elements();
        while (e.hasMoreElements())
            getRenderer().AddActor((vtkFollower)e.nextElement());
        e = marks.elements();
        while (e.hasMoreElements())
            getRenderer().AddActor((vtkActor)e.nextElement());
    }
    
    protected void hide() {
        Enumeration e = labels.elements();
        while (e.hasMoreElements())
            getRenderer().RemoveActor((vtkFollower)e.nextElement());
        e = marks.elements();
        while (e.hasMoreElements())
            getRenderer().RemoveActor((vtkActor)e.nextElement());
    }
    
    public void timeChanged(TimeEvent evt) {
        if (evt.timeSetChanged()) {
            if (gapEditor != null) {
                gapEditor.setValues(gapValues());
                gapEditor.setTags(gapTags());
                int maxGap = getTimeSet().getNumberOfValues()-1;
                if (getGap() > maxGap) setGap(maxGap);
            }
            invalidate();
            if (isVisible()) {
                hide();
                show();
            }
        }
    }
    
    public void coordinateSystemChanged(CoordinateSystemEvent evt) {
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
    }
    
    
    protected void validate() {
        labels.removeAllElements();
        //System.out.println("Setting lables...");
        double[] map = getTimeSet().getValues();
        String[] labs = getLabels(map);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        
        for (int i=0; i<map.length; i += getGap()) {
            //System.out.println("i= " + i + " length = " + map.length);
            /*
            vtkTextMapper mapper = new vtkTextMapper();
            
            mapper.SetInput(labs[i]);
            mapper.GetTextProperty().SetFontSize(getFont().getFontSize());
            mapper.GetTextProperty().SetFontFamily(getFont().getFontFamily());
            
            mapper.GetTextProperty().SetBold  (getFont().bold());
            mapper.GetTextProperty().SetItalic(getFont().italic());
            mapper.GetTextProperty().SetShadow(getFont().shadow());
            
            mapper.GetTextProperty().SetJustification(getJustification());
            mapper.GetTextProperty().SetVerticalJustification(BOTTOM);
            */
            
            vtkVectorText atext = new vtkVectorText();
            atext.SetText(labs[i]);
            vtkPolyDataMapper mapper = new vtkPolyDataMapper();
            mapper.SetInputData(atext.GetOutput());
            vtkFollower actor = new vtkFollower();
            actor.SetMapper(mapper);
            actor.SetScale(getScale() * normalActorSize);

            double[] r = getPosition(map[i]);
            actor.AddPosition(r[0], r[1], r[2]);
            actor.SetCamera(getRenderer().GetActiveCamera());
            
            actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
            actor.GetProperty().SetAmbientColor(rgb[0], rgb[1], rgb[2]);
            actor.GetProperty().SetSpecularColor(rgb[0], rgb[1], rgb[2]);
            actor.GetProperty().SetDiffuseColor(rgb[0], rgb[1], rgb[2]);

            labels.addElement(actor);
        }

        // create marks
        marks.removeAllElements();
        for (int i=0; i<map.length; i++) {
            // create sphere geometry
            vtkActor act = new vtkActor();
            vtkSphereSource sphere = new vtkSphereSource();
            sphere.SetRadius(0.15);
            sphere.SetThetaResolution(2);
            sphere.SetPhiResolution(2);
            
            // map to graphics library
            vtkPolyDataMapper mapper = new vtkPolyDataMapper();
            mapper.SetInputData(sphere.GetOutput());
            
            // actor coordinates geometry, properties, transformation
            act.SetMapper(mapper);
            act.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
            double[] r = getPosition(map[i]);
            act.SetPosition(r[0], r[1], r[2]);
            act.SetScale(getScale() * normalActorSize);
            marks.addElement(act);
        }
        
        valid = true;
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
        this.customizerVisible = customizerVisible;
        if (customizerVisible && customizer == null) customizer = new LabelsModuleCustomizer(this);
        if (customizer != null) customizer.setVisible(customizerVisible);
        firePropertyChange("customizerVisible", new Boolean(oldCustomizerVisible), new Boolean(customizerVisible));
    }
    
/** Getter for property gap.
 * @return Value of property gap.
 */
    public int getGap() {
        return gap;
    }
    
/** Setter for property gap.
 * @param gap New value of property gap.
 *
 * @throws PropertyVetoException
 */
    public void setGap(int gap) {
        //System.out.println("New Gap = " + gap);
        int oldGap = this.gap;
        this.gap = gap;
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
        firePropertyChange("gap", new Integer(oldGap), new Integer(gap));
    }
    
    private String[] gapTags() {    // returning null (???) look at this
        int n = getTimeSet().getNumberOfValues()-1;
        String[] s = new String[n];
        double stepMjd = getTimeSet().getStepMjd();
        for (int i=0; i<n; i++)
            s[i] = (new Interval( stepMjd * (i+1) )).toString();
        return s;
    }
    
    private int[] gapValues() {
        int n = getTimeSet().getNumberOfValues()-1;
        int[] a = new int[n];
        for (int i=0; i<n; i++) a[i] = i+1;
        return a;
    }
    
    public void setColor(Color color) {
        super.setColor(color);
        float[] rgb = ovt.util.Utils.getRGB(getColor());
        Enumeration e = labels.elements();
        while (e.hasMoreElements())
            ((vtkFollower)e.nextElement()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
        e = marks.elements();
        while (e.hasMoreElements())
            ((vtkActor)e.nextElement()).GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
    }
    
    String[] getLabels(double[] map) {
        String[] res = new String[map.length];
        double step = getTimeSet().getStepMjd();
        int type;
        
        if (step * 24. >= 1.) { // lable each HOUR
            type = Time.HOUR;
        } else if (step * 24. * 60. > 1. ) { // lable each MINTE
            type = Time.MINUTE;
        } else type = Time.SECOND;
        
        for (int i=0; i<map.length; i++)
            res[i] = getLabel(new Time(map[i]), type);
        
        return res;
    }
    
    String getLabel(Time time, int type) throws IllegalArgumentException {
        switch (type) {
            case Time.YEAR :
                return time.getAsText(Time.YEAR);
            case Time.MONTH     : return time.getAsText(Time.MONTH);
            case Time.DAY       : return time.getAsText(Time.MONTH) + "/" + time.getAsText(Time.DAY);
            case Time.HOUR      : if (time.getHour() == 0) return getLabel(time, Time.DAY);
            else return time.getAsText(Time.HOUR) + ":" + time.getAsText(Time.MINUTE);
            case Time.MINUTE    : if (time.getMinutes() == 0) return getLabel(time, Time.HOUR);
            else return time.getAsText(Time.MINUTE);
            case Time.SECOND    : if (time.getSeconds() == 0) return getLabel(time, Time.MINUTE);
            else return time.getAsText(Time.SECOND) + "sec";
            default: throw new IllegalArgumentException("Invalid type : " + type);
        }
    }
    
    public double getScale() {
        return scale;
    }
    
    public void setScale(double scale) {
        //Log.log("new scale: " + scale);
        double oldScale = this.scale;
        this.scale = scale;
        
        if (marks != null) {    // change marks Scale
            Enumeration e = marks.elements();
            while(e.hasMoreElements())
                ((vtkActor)e.nextElement()).SetScale(scale * normalActorSize);
        }

        if (labels != null) {   // change labels scale
            Enumeration e = labels.elements();
            while(e.hasMoreElements())
                ((vtkActor)e.nextElement()).SetScale(scale * normalActorSize);
        }
        firePropertyChange("scale", new Double(oldScale), new Double(scale));
    }
    
    public JMenuItem[] getMenuItems() {
        JMenuItem item = new JMenuItem("Properties...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {       
                setCustomizerVisible(true);
            }
        });
        item.setFont(Style.getMenuFont());
        return new JMenuItem[] {item};
    }

    /*public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("LabelsModule: Property changed: " + evt.getPropertyName());
        invalidate();
        if (isVisible()) {
            hide();
            show();
        }
    }*/
}
