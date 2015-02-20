/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/OutputLabel.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.9 $
 
 
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
import ovt.interfaces.*;
import ovt.beans.*;
import ovt.gui.*;
import ovt.datatype.*;

import vtk.*;

import java.util.*;
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author  Oleg
 * @version
 */
public class OutputLabel extends VisualObject implements MenuItemsSource,
            BeansSource, TimeChangeListener {
    
    private vtkActor2D    actor  = null;
    private vtkTextMapper mapper = null;

    private OutputLabelCustomizer customizer = null;
    
    protected static final int MAX_X  = 100;
    protected static final int MAX_Y  = 100;

    /** Left justification value from vtkTextMapper.h */
    public static final int LEFT   = 0;
    /** Center justification value */
    public static final int CENTER = 1;
    /** Right justification value */
    public static final int RIGHT  = 2;
    /** Bottom justification value */
    public static final int BOTTOM = 0;
    /** Top justification value */
    public static final int TOP    = 2;
    
/** values for justification combobox
 */    
    protected static final int[]    justificationValues = { LEFT,   CENTER,   RIGHT };
/** tags for justification combobox
 */    
    protected static final String[] justificationTags   = {"Left", "Center", "Right"};
    
    /** Holds value of property customizerVisible. */
    private boolean customizerVisible;
    
    /** Holds value of bean font. */
    public OVTFont font = new OVTFont(this);
    
    /** Holds value of property labelText. */
    private String labelText;
    
    /** Holds value of property x. */
    private int x = MAX_X;
    
    /** Holds value of property y. */
    private int y = 0;
    
    /** Holds value of property justification. */
    private int justification = RIGHT;
    
    /** Holds value of property color. */
    private Color color = Color.black;
    
    private BeansCollection beans = null;
    
    /** Creates new OutputLabel
     * @param core ovt core object
 */
    public OutputLabel(OVTCore core) {
        //super(core, "Output label");
        super(core, "Caption");
        setLabelText(getDefaultLabel());
        addPropertyChangeListener(this);    // to re-create actor after property change
    }

    
/** Generates default label, with all wisible sats enumeration and time range,
 * for example: "Astrid, Polar, 2000-01-01 00:00 - 2000-02-03 00:00"
 * @return Generated label
 */    
    public String getDefaultLabel() {
        StringBuffer label = new StringBuffer();
        Enumeration enSats = getCore().getSats().getChildren().elements();
        boolean first = true;
        while (enSats.hasMoreElements()) {
            VisualObject sat = (VisualObject) enSats.nextElement();
            if (sat.isVisible()) {
                if (sat instanceof ClusterSats) {
                    Enumeration enCluster = sat.getChildren().elements();
                    while (enCluster.hasMoreElements()) {
                        VisualObject clusterSat = (VisualObject) enCluster.nextElement();
                        if (clusterSat.isVisible()) {
                            if (!first) label.append(","); first = false;
                            label.append(clusterSat.getName());
                        }
                    }   
                }
                else {
                    if (!first) label.append(","); first = false;
                    label.append(sat.getName());
                }
            }
        }
        if (!first) label.append(", ");
        TimeSet timeSet = getCore().getTimeSettings().getTimeSet();
        label.append(new Time(timeSet.getStartMjd()).toString()).append(" - ");
        Time start = new Time(timeSet.getStartMjd());
        Time stop = new Time(timeSet.getStopMjd());
        int[] index = { Time.YEAR, Time.MONTH, Time.DAY, Time.HOUR };
        int startFrom = Time.YEAR; // = 0 
        if (start.getYear() != stop.getYear()) { 
            startFrom = Time.YEAR;
        } else {     
            if (start.getMonth() != stop.getMonth()) { 
                startFrom = Time.MONTH; 
            } else {
                if (start.getDay() != stop.getDay())  
                    startFrom = Time.DAY; 
                else startFrom = Time.HOUR; 
            }
        }
        String hh_mm_ss = stop.getAsText(Time.HOUR)+":"+stop.getAsText(Time.MINUTE)+":"+stop.getAsText(Time.SECOND);
        switch (startFrom) {
            case Time.YEAR  : label.append(stop.getAsText(Time.YEAR)+"-");
            case Time.MONTH : label.append(stop.getAsText(Time.MONTH)+"-");
            case Time.DAY   : label.append(stop.getAsText(Time.DAY)+" ");
            case Time.HOUR  : label.append(hh_mm_ss);
        }
        
        return label.toString();
    }
    
/** Getter for property descriptors
 * @return Descriptors from this object
 */    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = super.getDescriptors();
                BasicPropertyDescriptor pd;
                GUIPropertyEditor editor;
                
            /* color property descriptor */
                pd = new BasicPropertyDescriptor("color", this);
                pd.setDisplayName(getName() + " color");
                pd.setLabel("Color");
                ComponentPropertyEditor cpEditor = new ColorPropertyEditor(pd);
                cpEditor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
                    public void editingFinished(GUIPropertyEditorEvent evt) {
                        Render();
                    }
                });
                addPropertyChangeListener("color", cpEditor);
                pd.setPropertyEditor(new WindowedPropertyEditor(cpEditor, getCore().getXYZWin(), "Close"));
                descriptors.put(pd);
                
            /* horizJustification property descriptor */
                pd = new BasicPropertyDescriptor("justification", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Horizontal justification");
                editor = new ComboBoxPropertyEditor(pd, justificationValues, justificationTags);
                addPropertyChangeListener("justification", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);
                
            /* x property descriptor */
                pd = new BasicPropertyDescriptor("x", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Horizontal");
                SliderPropertyEditor sEditor = new SliderPropertyEditor(pd, 0, MAX_X);
                addPropertyChangeListener("x", sEditor);
                pd.setPropertyEditor(sEditor);
                descriptors.put(pd);
                
            /* x property descriptor */
                pd = new BasicPropertyDescriptor("y", this);
                pd.setTextOnlyAccessible();
                pd.setLabel("Vertical");
                sEditor = new SliderPropertyEditor(pd, 0, MAX_Y);
                pd.setPropertyEditor(sEditor);
                addPropertyChangeListener("y", sEditor);
                descriptors.put(pd);
                
            /* label property descriptor  */
                pd = new BasicPropertyDescriptor("labelText", this);
                pd.setTextOnlyAccessible();
                pd.setDisplayName("Label text");
                editor = new TextAreaEditor(pd, 4, 25);
                addPropertyChangeListener("labelText", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);
                
            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
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
        this.customizerVisible = customizerVisible;
        getCustomizer().setVisible(customizerVisible);
    }
    
    /** Popups customizer modal */
    public void popupCustomizer() {
        getCustomizer().setModal(true);
        setCustomizerVisible(true);
        getCustomizer().setModal(false);
    }

    /** Getter for bean font
     * @return Bean font.
     */
    public OVTFont getFont() {
        return font;
    }
    
    
    /** Getter for property labelText.
     * @return Value of property labelText.
     */
    public String getLabelText() {
        return labelText;
    }
    
    /** Setter for property labelText.
     * @param labelText New value of property labelText.
     */
    public void setLabelText(String labelText) {
        String oldLabelText = this.labelText;
        this.labelText = labelText;
        propertyChangeSupport.firePropertyChange ("labelText", oldLabelText, labelText);
    }
    
    /** Getter for property x.
     * @return Value of property x.
     */
    public int getX() {
        return x;
    }
    
    /** Setter for property x.
     * @param x New value of property x.
     */
    public void setX(int x) {
        int oldX = this.x;
        this.x = x;
        propertyChangeSupport.firePropertyChange ("x", new Integer (oldX), new Integer (x));
    }
    
    /** Getter for property y.
     * @return Value of property y.
     */
    public int getY() {
        return y;
    }
    
    /** Setter for property y.
     * @param y New value of property y.
     */
    public void setY(int y) {
        int oldY = this.y;
        this.y = y;
        propertyChangeSupport.firePropertyChange ("y", new Integer (oldY), new Integer (y));
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
        Color oldColor = this.color;
        this.color = color;
        propertyChangeSupport.firePropertyChange ("color", oldColor, color);
    }
    
    /** Getter for property justification.
     * @return Value of property justification.
     */
    public int getJustification() {
        return justification;
    }
    
    /** Setter for property justification.
     * @param justification New value of property justification.
     */
    public void setJustification(int justification) {
        int oldJustification = this.justification;
        this.justification = justification;
        propertyChangeSupport.firePropertyChange ("justification", new Integer (oldJustification), new Integer (justification));
    }
    
    /** Getter for text mapper.
     * @return output label text mapper object
     */
    protected vtkTextMapper getMapper() {
        if (mapper == null) {
            mapper = new vtkTextMapper();
        
            mapper.SetInput(getLabelText());
            mapper.GetTextProperty().SetFontSize(getFont().getFontSize());
            mapper.GetTextProperty().SetFontFamily(getFont().getFontFamily());
            
            mapper.GetTextProperty().SetBold  (getFont().bold());
            mapper.GetTextProperty().SetItalic(getFont().italic());
            mapper.GetTextProperty().SetShadow(getFont().shadow());
            
            mapper.GetTextProperty().SetJustification(getJustification());
            mapper.GetTextProperty().SetVerticalJustification(BOTTOM);
        }
        return mapper;
    }
    
    /** Getter for actor.
     * @return output label actor object
     */
    protected vtkActor2D getActor() {
        if (actor == null) {
            actor = new vtkActor2D();
            actor.SetMapper(getMapper());

            float[] rgb = ovt.util.Utils.getRGB(getColor());
            actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
            
            //actor.GetPositionCoordinate().SetCoordinateSystemToNormalizedDisplay();
            updatePosition();
        }
        return actor;
    }
    
    /** adds actor to renderer */
    protected void show() {
        getRenderer().AddActor(getActor());
    }
    
    /** removes actor to renderer */
    protected void hide() {
        getRenderer().RemoveActor(getActor());
    }
    
    /** Setter for property visible.
     * @param visible New value of property visible.
     */
    public void setVisible(boolean visible) {
        if (visible != isVisible()) {
            if (visible) show();
            else hide();
            Render();
            super.setVisible(visible);
        }
    }
    
    /** Creates array of JMenuItems, specyfic to this object.
     * @return array of JMenuItems.
     */
    public JMenuItem[] getMenuItems() {
        JMenuItem item[] = new JMenuItem[2];
        item[0] = new JMenuItem("Reset");
        item[0].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetLabelText();
            }
        });
        item[0].setFont(Style.getMenuFont());
        
        item[1] = new JMenuItem("Properties...");
        item[1].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setCustomizerVisible(true);
            }
        });
        item[1].setFont(Style.getMenuFont());
        return item;
    }
    
/**
 * @param evt  */    
    public void propertyChange(PropertyChangeEvent evt) {
        if (!isVisible()) {
            actor = null;
            mapper = null;
            return;
        }

        String prop = evt.getPropertyName();
        if (prop == "color") {
            if (actor != null) {
                float[] rgb = ovt.util.Utils.getRGB(getColor());
                actor.GetProperty().SetColor(rgb[0], rgb[1], rgb[2]);
            }
        }
        if (mapper != null) {
            if (prop == "labelText") {
                mapper.SetInput(getLabelText());
            }
            if (prop == "bold") {
                mapper.GetTextProperty().SetBold(getFont().bold());
            }
            if (prop == "italic") {
                mapper.GetTextProperty().SetItalic(getFont().italic());
            }
            if (prop == "shadow") {
                mapper.GetTextProperty().SetShadow(getFont().shadow());
            }
            if (prop == "fontSize") {
                mapper.GetTextProperty().SetFontSize(getFont().getFontSize());
            }
            if (prop == "fontFamily") {
                mapper.GetTextProperty().SetFontFamily(getFont().getFontFamily());
            }
            if (prop == "justification") {
                mapper.GetTextProperty().SetJustification(getJustification());
            }
        }
        updatePosition();
        Render();
    }
    
/** updates position of output label, called if label or window size changes ocures
 */    
    public void updatePosition() {
        //System.out.println("OutputLabel Position ");
        if (actor != null) {

            vtkViewport vp = (vtkViewport)getCore().getRenderer();
            int textSize[] = new int[2];
            
            int ts_x = mapper.GetWidth(vp);     // x size of text
            int ts_y = mapper.GetHeight(vp);    // y size od text
            
            int vpSize[] = vp.GetSize();        // size of viewport
            
            int x = (int) ( getX() / (double)MAX_X * (vpSize[0] - ts_x - 1)); // x position
            int y = (int) ( getY() / (double)MAX_Y * (vpSize[1] - ts_y - 1)); // y position

            x = x < 2 ? 2 : x;
            y = y < 4 ? 4 : y;

            switch(getJustification()) {
                case CENTER: x += ts_x/2+1; break;
                case RIGHT:  x += ts_x  +1; break;
            }

            //actor.GetPositionCoordinate().SetValue((double)x/vpSize[0], (double)y/vpSize[1]);
            actor.GetPositionCoordinate().SetValue(x,y);
        }
    }
    
/** Getter for customizer
 * @return customizer
 */    
    public OutputLabelCustomizer getCustomizer() {
        if (customizer == null)
            customizer = new OutputLabelCustomizer(this, getCore().getXYZWin());
        return customizer;
    }
    
    /** Set's labelText to getDefaultLabel() */
    public void resetLabelText() {
        setLabelText(getDefaultLabel());
    }
    
public BeansCollection getBeanDesriptors() {
        if (beans == null)
        {
            beans = new BeansCollection();
            try {
                beans.put(new BasicBeanDescriptor("Font", "font", getClass()));
            } catch (NoSuchFieldException e2) { e2.printStackTrace(); System.exit(-1); }
        }
        return beans;
}
    
public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
        // time period have changed
        resetLabelText();
    }
}
    
}
