/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/VisualObject.java,v $
  Date:      $Date: 2003/09/28 17:52:52 $
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

/*
 * VisualObject.java
 *
 * Created on March 10, 2000, 6:27 PM
 */
 
package ovt.object;

import ovt.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.interfaces.*;

import vtk.*;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.awt.*;
import javax.swing.*;

/** 
 * The Visual Object is an object, that deals with Visualization.
 * @author  root
 * @version 
 */
public class VisualObject extends BasicObject implements PropertyChangeListener {

  /** Holds value of property visible. */
  private boolean visible = false;
  
  /** Indicates weather the object will have visual children. 
   * If <CODE>containsVisualChildren</CODE> is <CODE>true</CODE> -
   * <CODE>vidibilityPropertyDescriptor.isDerived()</CODE> returns <CODE>true</CODE>
   * By default - <CODE>false</CODE>.
   */
  private boolean containsVisualChildren = false;

  /** Creates new VisualObject */
  public VisualObject(OVTCore core, String name, boolean containsVisualChildren) {
    super(core, name);
    this.containsVisualChildren = containsVisualChildren;
  }
  
  /** Creates new VisualObject with name, which will not have VisualObject children */
  public VisualObject(OVTCore core, String name) {
    super(core, name);
  }
  
  /** Creates new VisualObject with name and Icon, which will not have VisualObject children*/
  public VisualObject(OVTCore core, String name, String iconFilename) {
    super(core, name);
    try {
      setIcon(new ImageIcon(Utils.findResource(iconFilename)));
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); }
  }
  
  /** Creates new VisualObject with name and Icon */
  public VisualObject(OVTCore core, String name, String iconFilename, boolean containsVisualChildren) {
    super(core, name);
    try {
      setIcon(new ImageIcon(Utils.findResource(iconFilename)));
    } catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
    this.containsVisualChildren = containsVisualChildren;
  }
  
  public Descriptors getDescriptors() {
    if (descriptors == null) {
    // Add default property descriptor for visible property.
    // each visual object can be hidden or shown.
      try {
      
        descriptors = new Descriptors();
        BasicPropertyDescriptor pd = new BasicPropertyDescriptor("visible", this);
        pd.setDisplayName(getName());
        
        if (containsVisualChildren) pd.setDerived(true);
        
        BasicPropertyEditor editor = new VisibilityEditor(pd);
        addPropertyChangeListener("visible", editor);
        addPropertyChangeListener("enabled", editor);
        pd.setPropertyEditor(editor);
        descriptors.put(pd);
        
      } catch (IntrospectionException e2) {
        System.out.println(getClass().getName() + " -> " + e2.toString());
        System.exit(0);
      }
    }
    return descriptors;
  }
  
  
  /** Getter for property visible.
   * @return Value of property visible.
   */
  public boolean isVisible() {
    int type = hasVisualChildEx();
    if (type == 0) return visible;
    boolean real_visible = (type == 2);     // if type = 2 -> has visible leafs
    if (visible != real_visible) {
        boolean oldVisible = visible;
        visible = real_visible;
        firePropertyChange("visible", new Boolean (oldVisible), new Boolean (visible));
    }
    return real_visible;
  }


  /** Setter for property visible.
   * @param visible New value of property visible.
   *
   * @throws PropertyVetoException
   */
  public void setVisible(boolean visible) {
    boolean oldVisible = this.isVisible();
    if (oldVisible == visible) return; // nothing to change
    
    //if (!isEnabled()) return; // object is not enabled - no motion. ;)
    if (visible && !isEnabled()) 
        throw new IllegalArgumentException("Attempt to show the object while it is not enabled! ");
    

    Vector vVisual = getVisualLeafs();
    Enumeration eVisual;

    if (vVisual.size() > 0) {

        if (visible) { // show event
            boolean some_restored = false;
            if (stored) {
                stored = false; // only once can restore once stored values
                eVisual = vVisual.elements();
                while(eVisual.hasMoreElements()) {
                    VisualObject obj = (VisualObject) eVisual.nextElement();
                    some_restored |= obj.restoreVisible();
                }
            }
            if (!some_restored) { // all children where hidden. Now - show them!
                eVisual = vVisual.elements();
                while(eVisual.hasMoreElements()) {
                    VisualObject obj = (VisualObject) eVisual.nextElement();
                    obj.setVisible(true);
                }
            }
        }
        else { // hide event
            Enumeration eVisible = getVisibleLeafs().elements();

            boolean or_mask = false;
            if (!eVisible.hasMoreElements()) or_mask = true; // if no leafs are visible
        
            // store old visible propery values
            eVisual = vVisual.elements();
            while(eVisual.hasMoreElements()) {
                VisualObject obj = (VisualObject) eVisual.nextElement();
                obj.storeVisible(or_mask);
            }
            stored = true;

            while(eVisible.hasMoreElements()) {
                VisualObject obj = (VisualObject) eVisible.nextElement();
                obj.setVisible(false);
            }
        }
    }
    this.visible = visible;
    firePropertyChange ("visible", new Boolean (oldVisible), new Boolean (visible));
  }
  
  private boolean was_visible = true;
  private boolean stored = false;

  public void storeVisible(boolean or_mask) {
    was_visible = this.isVisible() | or_mask;
  }

  public boolean restoreVisible() {
    if (was_visible == true) {
        this.setVisible(true);
    }
    return was_visible;
  }
  
  /** hides the object if enabled=false and calls superclass method setEnabled */
  public void setEnabled(boolean enabled) {
    if (!enabled && isVisible()) setVisible(false);
    super.setEnabled(enabled);
  }
  
    /** Listens to events from parent */
  public void propertyChange(PropertyChangeEvent evt) {
    //System.out.println("Parent is " + getParent());
    //System.out.print("Is this event from my parent ... ");
    if (evt.getSource() == getParent()) { 
      // this is event from my parent
      //System.out.println("yes!");
      if (evt.getPropertyName().equals("enabled")) { // Sat.enabled property changed
        boolean satEnabled = ((Boolean)evt.getNewValue()).booleanValue();
        if (satEnabled != isEnabled())  setEnabled(satEnabled);
      }
      
    } //else System.out.println("no :(");
  }


}
