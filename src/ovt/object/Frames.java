/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/Frames.java,v $
  Date:      $Date: 2005/12/14 18:33:46 $
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
import ovt.event.*;
import ovt.beans.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import vtk.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Frames extends VisualObject
implements MenuItemsSource {

    private FrameModule xoy, xoz, yoz;
    private FramesCustomizer customizer = null;
    
    vtkActor YOZActor = null, XOZActor = null, XOYActor = null;
    
    boolean isInitialised = false;
    
    /** Holds value of property customizerVisible. */
    private boolean customizerVisible;
    
    /** Holds value of property cellsNumber. */
    private int cellsNumber = 10;
    
    /** Holds value of property cellSize. */
    private int cellSize = 5;

    /** Holds value of property yozPosition. */
    private int YOZPosition = -5;
    
    public Frames(OVTCore core) {
        super(core, "Frames", "images/frame.gif", true);
        xoy = new FrameModule(this, FrameModule.XOYPLANE);
        xoz = new FrameModule(this, FrameModule.XOZPLANE);
        yoz = new FrameModule(this, FrameModule.YOZPLANE);
        addChild(xoy);
        addChild(xoz);
        addChild(yoz);
        addPropertyChangeListener(xoy);
        addPropertyChangeListener(xoz);
        addPropertyChangeListener(yoz);
    }
    
    public void timeChanged(TimeEvent e) {}
    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            descriptors = super.getDescriptors();
            
            try {
            /* cellsNumber Property Descriptor */
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("cellsNumber", this);
                pd.setMenuAccessible(false);
                pd.setDisplayName("Number of cells");
                SliderPropertyEditor sliderEditor = new SliderPropertyEditor(pd, 2, 10, 2, 2);
                addPropertyChangeListener("cellsNumber", sliderEditor);
                pd.setPropertyEditor(sliderEditor);
                descriptors.put(pd);
                
            /* cellSize Property Descriptor */
                pd = new BasicPropertyDescriptor("cellSize", this);
                pd.setMenuAccessible(false);
                pd.setDisplayName("Cell size (RE)");
                sliderEditor = new SliderPropertyEditor(pd, 1, 10, 1, 1);
                addPropertyChangeListener("cellSize", sliderEditor);
                pd.setPropertyEditor(sliderEditor);
                descriptors.put(pd);
                                
            /* YOZPosition Property Descriptor */
                pd = new BasicPropertyDescriptor("YOZPosition", this);
                pd.setMenuAccessible(false);
                pd.setDisplayName("YOZ position");
                sliderEditor = new SliderPropertyEditor(pd, -5, 5, 1, 1);
                addPropertyChangeListener("YOZPosition", sliderEditor);
                pd.setPropertyEditor(sliderEditor);
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
        if (customizer == null) {
            customizer = new FramesCustomizer(getThis());
            addPropertyChangeListener("yozPosLabel", customizer);
        }
        customizer.setVisible(customizerVisible);
    }
    
    public JMenuItem[] getMenuItems() {
       JMenuItem item = new JMenuItem();
        item = new JMenuItem("Properties...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {       
                setCustomizerVisible(true);
            }
        });
        item.setFont(Style.getMenuFont());
        return new JMenuItem[] {item};
    }
    
    Frames getThis() {return this;}
    
    /** Getter for property cellsNumber.
     * @return Value of property cellsNumber.
     */
    public int getCellsNumber() {
        return cellsNumber;
    }
    
    /** Setter for property cellsNumber.
     * @param cellsNumber New value of property cellsNumber.
     */
    public void setCellsNumber(int cellsNumber) {
        if (cellsNumber % 2 != 0) return;
        int oldCellsNumber = this.cellsNumber;
        this.cellsNumber = cellsNumber;
        propertyChangeSupport.firePropertyChange ("cellsNumber", new Integer (oldCellsNumber), new Integer (cellsNumber));
    }
    
    /** Getter for property cellSize.
     * @return Value of property cellSize.
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /** Setter for property cellSize.
     * @param cellSize New value of property cellSize.
     */
    public void setCellSize(int cellSize) {
        int oldCellSize = this.cellSize;
        this.cellSize = cellSize;
        propertyChangeSupport.firePropertyChange ("cellSize", new Integer (oldCellSize), new Integer (cellSize));
        propertyChangeSupport.firePropertyChange ("yozPosLabel", null, null);   // TO CUSTOMIZER
    }
    
    /** Getter for property YOZPosition.
     * @return Value of property YOZPosition.
     */
    public int getYOZPosition() {
        return this.YOZPosition;
    }
    
    /** Setter for property YOZPosition.
     * @param YOZPosition New value of property YOZPosition.
     */
    public void setYOZPosition(int YOZPosition) {
        int oldYOZPosition = this.YOZPosition;
        this.YOZPosition = YOZPosition;
        propertyChangeSupport.firePropertyChange ("YOZPosition", new Integer (oldYOZPosition), new Integer (YOZPosition));
        propertyChangeSupport.firePropertyChange ("yozPosLabel", null, null);   // TO CUSTOMIZER
    }
    
    /** for XML */
    public FrameModule getXOYFrame() { return xoy; }
    /** for XML */
    public FrameModule getXOZFrame() { return xoz; }
    /** for XML */
    public FrameModule getYOZFrame() { return yoz; }
}
