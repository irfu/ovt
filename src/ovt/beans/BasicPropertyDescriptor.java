/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/BasicPropertyDescriptor.java,v $
  Date:      $Date: 2003/09/28 17:52:32 $
  Version:   $Revision: 2.4 $


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
 * ovtPropertyDescriptor.java
 *
 * Created on February 29, 2000, 2:06 PM
 */
 
package ovt.beans;

import ovt.*;
import ovt.util.*;
import ovt.object.*;
import ovt.interfaces.*;

import java.beans.*;


/** 
 * This OVT-oriented implementation of  java.beans.PropertyDescriptor
 * This object is used to get properties editor and 
 * to know the properties name, description.
 * @author  mykola
 * @version 
 */
public class BasicPropertyDescriptor extends PropertyDescriptor {

  private Object bean = null;
  
  private OVTPropertyEditor editor = null;
  
  /** Holds value of property label. */
  private String label = "";
    /** Holds value of property textAccessible. */
  private boolean textAccessible = true;
  /** Holds value of property componentAccessible. */
  private boolean componentAccessible = true;
  /** Holds value of property windowAccessible. */
  private boolean windowAccessible = true;
  
  /** Holds value of property menuAccessible. */
  private boolean menuAccessible = true;
  
  /** Holds value of property derived. */
  private boolean derived = false;  
  
  /** Holds value of property tooltipText. */
  private String toolTipText = null;
  
  /** Utility field used by bound properties. */
  private OVTPropertyChangeSupport propertyChangeSupport =  new OVTPropertyChangeSupport (this);
  
  /** Creates new ovtPropertyDescriptor */
  public BasicPropertyDescriptor(String fieldName, Object bean) throws IntrospectionException {
    super(fieldName, bean.getClass());
    this.bean = bean;
   }
  
  public Object getBean() {
    return bean;
  }
  
  public void setPropertyEditor(OVTPropertyEditor editor) {
    this.editor = editor;
  }
  
  public OVTPropertyEditor getPropertyEditor() {
    return editor;
  }
  
  /** Getter for label.
   * @return Value of property label.
   */
  public String getLabel() {
    //System.out.println("getLable - "+label);
    //if (label.equals("")) return getName();
    //else 
    return label;
  }
  /** Setter for property label.
   * @param label New value of property label.
   */
  public void setLabel(String label) {
      //ovt.util.Log.log("Setting new label="+label);
      String oldLabel = this.label;
      this.label = label;
      propertyChangeSupport.firePropertyChange ("label", oldLabel, label);
  }
  
  
  /*
  public int getAccessType() {
    return access;
  }
  
  public void setAccessType(int access_type) throws IllegalArgumentException {
    if ((access_type == TEXT) || (access_type == COMPONENT) || (access_type == WINDOW)
      accessType = access_type;
    else throw IllegalArgumentException(getClass()+"-> There is no access type for acces_type=" + access_type);
  }*/
  
  public void setAccessType(boolean textAccessible, boolean componentAccessible, boolean windowAccessible) {
    setTextAccessible(textAccessible);
    setComponentAccessible(componentAccessible);
    setWindowAccessible(windowAccessible);
  }
  
  public void setAccessType(boolean textAccessible, boolean menuAccessible, boolean componentAccessible, boolean windowAccessible) {
    setTextAccessible(textAccessible);
    setMenuAccessible(menuAccessible);
    setComponentAccessible(componentAccessible);
    setWindowAccessible(windowAccessible);
  }
  
  public void setTextOnlyAccessible() {
    setTextAccessible(true);
    setMenuAccessible(false);
    setComponentAccessible(false);
    setWindowAccessible(false);
  }
  
  /** Getter for property textAccessible.
   * @return Value of property textAccessible.
   */
  public boolean isTextAccessible() {
    return textAccessible;
  }
  /** Setter for property textAccessible.
   * @param textAccessible New value of property textAccessible.
   */
  public void setTextAccessible(boolean textAccessible) {
    this.textAccessible = textAccessible;
  }
  /** Getter for property componentAccessible.
   * @return Value of property componentAccessible.
   */
  public boolean isComponentAccessible() {
    return componentAccessible;
  }
  /** Setter for property componentAccessible.
   * @param componentAccessible New value of property componentAccessible.
   */
  public void setComponentAccessible(boolean componentAccessible) {
    this.componentAccessible = componentAccessible;
  }
  /** Getter for property windowAccessible.
   * @return Value of property windowAccessible.
   */
  public boolean isWindowAccessible() {
    return windowAccessible;
  }
  /** Setter for property windowAccessible.
   * @param windowAccessible New value of property windowAccessible.
   */
  public void setWindowAccessible(boolean windowAccessible) {
    this.windowAccessible = windowAccessible;
  }
  /** Getter for property menuAccessible.
   * @return Value of property menuAccessible.
   */
  public boolean isMenuAccessible() {
    return menuAccessible;
  }
  /** Setter for property menuAccessible.
   * @param menuAccessible New value of property menuAccessible.
   */
  public void setMenuAccessible(boolean menuAccessible) {
    this.menuAccessible = menuAccessible;
  }

  public String getPropertyPathString() {
    return ((OVTObject)getBean()).getPathString() + "." + getName();
  }
  
/** Indicates if the property is derived. Is used for saving object's
 * state. By default <CODE>false</CODE>
 * @return Value of property derived.
 */
public boolean isDerived() {
    return derived;
}
  
/** Setter for property derived.
 * @param derived New value of property derived.
 */
public void setDerived(boolean derived) {
  this.derived = derived;
}
  
/** Getter for property tooltipText.
 * @return Value of property tooltipText.
 */
public String getToolTipText() {
  return toolTipText;
}

/** Setter for property tooltipText.
 * @param tooltipText New value of property tooltipText.
 */
public void setToolTipText(String toolTipText) {
  this.toolTipText = toolTipText;
}

/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener (l);
}

/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
public void addPropertyChangeListener(String property, java.beans.PropertyChangeListener l) {
    propertyChangeSupport.addPropertyChangeListener (property, l);
}


/** Removes a PropertyChangeListener from the listener list.
 * @param l The listener to remove.
 */
public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    propertyChangeSupport.removePropertyChangeListener (l);
}

}
