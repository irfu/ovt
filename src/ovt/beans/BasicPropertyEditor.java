/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/beans/BasicPropertyEditor.java,v $
  Date:      $Date: 2003/09/28 17:52:32 $
  Version:   $Revision: 2.5 $


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
 * OVTPropertyEditor.java
 *
 * Created on February 29, 2000, 2:30 PM
 */

package ovt.beans;

import ovt.gui.*;
import ovt.util.*;
import ovt.interfaces.*;

import java.beans.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import java.lang.Cloneable;
import java.lang.reflect.*;

/**
 * A superclass of all PropertyEditors, has {@link #getAsText} and {@link #setAsText} 
 * methods. It is a OVT-oriented implementation of <CODE>java.beans.PropertyEditorSupport</CODE>
 * It can edit properties of all basic types: int, double, boolean, etc. and String.
 * Besides that, it can edit index'ed properties. One has to specify <CODE>tags</CODE>
 * and <CODE>values</CODE> to be able to do this.
 * @author  mykola
 */
public class BasicPropertyEditor implements OVTPropertyEditor, 
            PropertyChangeListener {

  private static final int DEBUG = 8;              
                
  public final int TEXT = 1;
  public final int COMPONENT = 2;
  public final int WINDOW = 4;
  
  private BasicPropertyDescriptor propertyDescriptor = null;
  
  private Vector beanCollection = new Vector();
  protected OVTPropertyChangeSupport propertySupport = new OVTPropertyChangeSupport ( this );
  private String[] tags = null;
  private Object[] values = null;
  
  /** Holds value of property enabled. */
  private boolean enabled = true;

/** Creates new BasicPropertyEditor */
  public BasicPropertyEditor(BasicPropertyDescriptor propertyDescriptor) {
    this.propertyDescriptor = propertyDescriptor;
    addBean(propertyDescriptor.getBean());
  }

/** Creates new BasicPropertyEditor */
  public BasicPropertyEditor(BasicPropertyDescriptor propertyDescriptor, Object[] values, String[] tags) {
    this.propertyDescriptor = propertyDescriptor;
    addBean(propertyDescriptor.getBean());
    setValues(values);
    setTags(tags);
  }

/** Creates new BasicPropertyEditor */
  public BasicPropertyEditor(BasicPropertyDescriptor propertyDescriptor, int[] values, String[] tags) {
    this.propertyDescriptor = propertyDescriptor;
    addBean(propertyDescriptor.getBean());
    setValues(values);
    setTags(tags);
  }



  public BasicPropertyDescriptor getPropertyDescriptor()
    { return propertyDescriptor; }

  /**
   * @return the name of property
   */
  public String getPropertyName() {
    return getPropertyDescriptor().getName();
  }

  public String getPropertyDisplayName() {
    return getPropertyDescriptor().getDisplayName();
  }
  
  public String getPropertyLabel() {
    return getPropertyDescriptor().getLabel();
  }
  
  public void addBean(Object bean) {
    // add bean
    beanCollection.addElement(bean);
    // if the bean is disabled - the editor should be disabled too
    try {
      Enableable en = (Enableable)bean;
      setEnabled(en.isEnabled());
    } catch (ClassCastException e2) {
      //System.out.println("Bean doesn't implement Enableable");
    }
    
  }

  public void removeBean(Object bean) {
    beanCollection.removeElement(bean);
  }

  protected Vector getBeans() {
    return beanCollection;
  }

  protected Object getBean() {
    return getBeans().firstElement();
  }
  
  public void addPropertyChangeListener (PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener (listener);
  }

  
  public void addPropertyChangeListener (String property, PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener (property, listener);
  }
  
  public void removePropertyChangeListener (PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener (listener);
  }


  public Object getValue() {
    
    // no bean - no value ;)
    if (getBeans().size() == 0) return null;

    Object value = null;
    Method method = getPropertyDescriptor().getReadMethod();
    if ((supportsMultipleEditing()) && (getBeans().size() > 1)) {
      // edit properties of many beans
      Object[] beans = Vect.toArray(getBeans());
      Object[] values = new Object[beans.length];
      // get values
      for (int i=0; i<beans.length; i++) {
        try {
          values[i] = method.invoke(beans[i], null);
        } catch (IllegalAccessException e1) { e1.printStackTrace();
        } catch (IllegalArgumentException e2) { e2.printStackTrace();
        } catch (InvocationTargetException e3) { e3.printStackTrace();
        }
      }
      // compare values
      boolean equal = true;

      for(int i=1; (i < beans.length) && (equal); i++)
      equal = values[0].equals(values[i]);

      if (equal) value = values[0];

    } else {
      // edit property of one bean
      try {
        value = method.invoke(getBeans().firstElement(), null);
      } catch (IllegalAccessException e1) { 
          System.err.println("Problem in "+propertyDescriptor.getName()); 
          e1.printStackTrace(System.err);
      } catch (IllegalArgumentException e2) { e2.printStackTrace();
      } catch (InvocationTargetException e3) {
        Throwable targetException = e3.getTargetException();
        //if (targetException instanceof PropertyVetoException) throw (PropertyVetoException)targetException;
        System.out.println("The recieved exception is not a propertyVetoException!!!\n");
        e3.getTargetException().printStackTrace();
      }
    }
    return value;
  }

/** Sets the property value using String.
 * Can be used for the properties of standard java.lang.* classes, like <CODE>int</CODE>, <CODE>double</CODE>, <CODE>boolean</CODE>, <CODE>String</CODE>, etc.
 *
 * May raise
 *   *      java.lang.IllegalArgumentException if either the String is badly formatted or if
 *   *      this kind of property can't be expressed as text.
 * @param value
 * @throws PropertyVetoException
 */  
  public void setValue(Object value) throws PropertyVetoException {
    setValue(value, getBeans().firstElement());
  }
  
  public void setValue(Object value, Object bean) throws PropertyVetoException {
      // set a property of one bean
    Method method = getPropertyDescriptor().getWriteMethod();
       try {
        method.invoke(bean, new Object[]{value});
      } catch (IllegalAccessException e1) { System.out.println(e1.toString());
      } catch (IllegalArgumentException e2) { 
        // pay atention to this exception!
        // this is incapsulated exception, that carries exception, thrown by the WriteMethod.
      } catch (InvocationTargetException e3) {
        Throwable targetException = e3.getTargetException();
        if (targetException instanceof PropertyVetoException) throw (PropertyVetoException)targetException;
        if (targetException instanceof IllegalArgumentException) throw new PropertyVetoException(((IllegalArgumentException)targetException).getMessage(), null);
        System.out.println("The recieved exception is not a propertyVetoException!\n");
        e3.getTargetException().printStackTrace();
      }
  }

  /** Gets the property value as a string suitable for presentation to a human to edit.
   * @return The property value as a string suitable for presentation to a human to edit.
   *
   * <B>"null" is the value can't be expressed as a string!</B>
   * It means, that only CustomEditor can edit this value.
   *  If a non-null value is returned, then the PropertyEditor should be
   *   prepared to parse that string back in setAsText().
   */
  public String getAsText() {
    String result = null;
    // if tags and values are set
    if ((values != null) && (tags != null)) {
      Object obj = null;
      int length = (values.length > tags.length) ? tags.length : values.length;
      for (int i=0; ((i<length) && (result == null)); i++) {
        Log.log("getAsText() value["+i+"]='"+values[i]+"'", DEBUG);
        obj = values[i];
        if (obj != null && obj.equals(getValue())) result = tags[i];
      }
    } else {
    // in other case look at the read method return type
    // and cast the getValue() object to a corresponding class.
        String returnType = propertyDescriptor.getReadMethod().getReturnType().getName();
        //System.out.println("paramClass="+ returnType);
        if (returnType.equals("boolean")) {
            return "" + ((Boolean)getValue()).booleanValue();
        } else if (returnType.equals("int")) {
            return "" + ((Integer)getValue()).intValue();
        } else if (returnType.equals("double")) {
            return "" + ((Double)getValue()).doubleValue();
        } else if (returnType.equals("float")) { 
            return "" + ((Float)getValue()).floatValue();
        } else if (returnType.equals("long")) {
            return "" + ((Long)getValue()).longValue();
        } else if (returnType.equals("java.lang.String")) {
            return (String)getValue(); 
        } 
    }
    return result;
  }

  /** Sets the property value by parsing a given String. May raise
 *     java.lang.IllegalArgumentException if either the String is badly formatted or if
 *     this kind of property can't be expressed as text.
 * @param text Text to be set
 * @throws PropertyVetoException  */
  public void setAsText(String text) throws PropertyVetoException  {
    if ((values != null) && (tags != null)) {
      String tag = null;
      int length = (values.length > tags.length) ? tags.length : values.length;
      for (int i=0; (i<length); i++) {
        tag = tags[i];
        //DBG*/System.out.println("tag: "+tag);
        if (tag.equals(text)) { 
          //System.out.println("Equals!! Trying to set "+values[i]);
          setValue(values[i]); 
          return; 
        }
      }
      // nothing was found ...
      throw new PropertyVetoException("setAsText: the value corresponding to '" + text + "' was not found in the list.", null);
    } else {
        // look at the write method parameter type
        // and make and set a corresponding object from text
        String inputType = propertyDescriptor.getWriteMethod().getParameterTypes()[0].getName();
        try {
            if (inputType.equals("int")) {
                setValue(new Integer(text)); return;
            } else if (inputType.equals("double")) {
                setValue(new Double(text)); return;
            } else if (inputType.equals("java.lang.String")) {
                setValue(text); return;
            } else if (inputType.equals("boolean")) {
                setValue(new Boolean(text)); return;
            } else if (inputType.equals("float")) { 
                setValue(new Float(text)); return;
            } else if (inputType.equals("long")) {
                setValue(new Long(text)); return;
            }  
        } catch (NumberFormatException e2) {
            throw new PropertyVetoException("Invalid format!", null);
        }
    }
  }

  /** If the property value must be one of a set of known tagged values, then this
   *      method should return an array of the tag values. This can be used to represent
   *      (for example) enum values. If a PropertyEditor supports tags, then it should
   *      support the use of setAsText with a tag value as a way of setting the value.
   * @return The tag values for this property.
   */
  
  public String[] getTags() {
    return tags;
  }
  
    
  public void setTags(String[] newtags) {
    String[] oldtags = tags;
    tags = newtags;
    propertySupport.firePropertyChange("tags", oldtags, tags);
  }
  
  
  public Object[] getValues() {
    return values;
  }

  public void setValues(int[] values) {
    Integer[] intValues = new Integer[values.length];
    for (int i=0; i<values.length; i++) 
        intValues[i] = new Integer(values[i]);
    setValues(intValues);
  }

  public void setValues(double[] values) {
    Double[] dValues = new Double[values.length];
    for (int i=0; i<values.length; i++) 
        dValues[i] = new Double(values[i]);
    setValues(dValues);
  }

  public void setValues(Object[] values) {
    Object[] oldvalues = this.values;
    this.values = values;
    propertySupport.firePropertyChange("values", oldvalues, values);
  }
  
  public void propertyChange(PropertyChangeEvent evt) {
    //System.out.println(getClass().getName()+".prpropertyChange event : "+ evt.getPropertyName());
    if (evt.getPropertyName().equals("enabled")) {
      //System.out.println("Recieved event enabled!!!!!!!!");
      boolean enabled = ((Boolean)evt.getNewValue()).booleanValue();
      setEnabled(enabled); 
    } else
    // Tell all editor, to update itself.
     propertySupport.firePropertyChange(evt);
  }



  //---------------- For multiple bean editing -----------------

  /** If the editor can edit similar properties of several beans at once,
   * it should return true.
   * The property, being edited must override @link java.Object.equals(Object)
   * and @link java.Object.clone(void) methods.
   * @return true if the editor can edit similar properties of several beans at once.
   * By default it returns false.
   */
  public boolean supportsMultipleEditing() {
    return false;
  }

  /** Getter for property enabled.
   * @return Value of property enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }
  /** Setter for property enabled.
   * @param enabled New value of property enabled.
   */
  public void setEnabled(boolean enabled) {
    boolean oldValue = this.enabled;
    this.enabled = enabled;
    propertySupport.firePropertyChange("enabled", new Boolean(oldValue), new Boolean(enabled));
  }
}
