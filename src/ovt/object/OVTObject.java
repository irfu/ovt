/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/OVTObject.java,v $
  Date:      $Date: 2003/09/28 17:52:50 $
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
 * OVTObject.java
 *
 * Created on March 15, 2000, 10:47 AM
 */

package ovt.object;

import ovt.beans.*;
import ovt.util.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import javax.swing.*;

/**
 * The root object for all OVT objects
 * @author  Mykola Khotyaintsev
 * @version
 */
public class OVTObject implements NamedObject, OVTTreeNode,
            DescriptorsSource, Enableable {
    
    
  /** Holds value of property name. */
    protected String name = "no name";
  /** Holds value of property parent. */
    protected OVTObject parent = null;
  /** Holds value of property children. */
    protected Children children = new Children(this);
  /** Utility field used by bound properties. */
    protected final OVTPropertyChangeSupport propertyChangeSupport = new OVTPropertyChangeSupport (this);
  /** Utility field used by constrained properties. */
    protected java.beans.VetoableChangeSupport vetoableChangeSupport = new java.beans.VetoableChangeSupport (this);
  /** Holds value of property descriptors. */
    protected Descriptors descriptors = null;
  /** Holds value of property beans. */
    protected BeansCollection beans = null;
  /** Holds value of property valid. */
    protected boolean valid = false;
  /** Holds value of property enabled. */
    private boolean enabled = true;
  /** Holds value of property icon. */
    private ImageIcon icon = null;
    
  /** Holds value of property showInTree. */
    private boolean showInTree = true;
    
  /** Creates new OVTObject */
    public OVTObject() {
    }
    
  /** Creates new OVTObject */
    public OVTObject(String name) {
        setName(name);
    }
    
  /** Getter for property name.
   * @return Value of property name.
   */
    public String getName() {
        return name;
    }
  /** Setter for property name.
   * @param name New value of property name.
   */
    public void setName(String newValue) {
        String oldValue = this.name;
        this.name = newValue;
        if (!oldValue.equals(newValue)) firePropertyChange("name", oldValue, newValue);
    }
    
  /** Getter for property parent.
   * @return Value of property parent.
   */
    public OVTObject getParent() {
        return parent;
    }
    
  /** Setter for property parent.
   * @param parent New value of property parent.
   */
    public void setParent(OVTObject parent) {
        this.parent = parent;
    }
    
    
  /** Add a PropertyChangeListener to the listener list.
   * @param l The listener to add.
   */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }
    
  /** Add a PropertyChangeListener to the listener list.
   * this is valid for JDK 1.1
   * @param l The listener to add.
   */
    public void addPropertyChangeListener(String property, java.beans.PropertyChangeListener l) {
    /* if (property == "visible") {
        System.out.println("------- OVTObject: " + getName() + " -> addPropertyChangeListener("+ property +",...) called -------");
        (new Exception("Debug")).printStackTrace();
    }*/
        propertyChangeSupport.addPropertyChangeListener (property, l);
    }
    
  /** Removes a PropertyChangeListener from the listener list.
   * @param l The listener to remove.
   */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
  /** Removes a PropertyChangeListener from the listener list.
   * @param l The listener to remove.
   */
    public void removePropertyChangeListener(String property, java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (property, l);
    }
    
  /** Add a VetoableChangeListener to the listener list.
   * @param l The listener to add.
   */
    public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vetoableChangeSupport.addVetoableChangeListener (l);
    }
  /** Removes a VetoableChangeListener from the listener list.
   * @param l The listener to remove.
   */
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
        vetoableChangeSupport.removeVetoableChangeListener (l);
    }
    
  /** Fires property change
   *
   */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
  /** Getter for property children.
   * @return Value of property children.
   */
    public Children getChildren() {
        return children;
    }
  /** Setter for property children.
   * @param children New value of property children.
   *
   * @throws PropertyVetoException
   
    public void setChildren(Children children) {
        Children oldChildren = this.children;
        this.children = children;
        propertyChangeSupport.firePropertyChange ("children", oldChildren, children);
    }*/
    
    
    
  /** Getter for property descriptors.
   * @return Value of property descriptors.
   */
    public Descriptors getDescriptors() {
        return descriptors;
    }
    
  /** Setter for property descriptors.
   * @param descriptors New value of property descriptors.
   */
    public void setDescriptors(Descriptors descriptors) {
        this.descriptors = descriptors;
    }
    
    public boolean isValid() {
        return valid;
    }
  /** Sets property valid to false.
   *
   */
    public void invalidate() {
        valid = false;
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
        firePropertyChange("enabled", new Boolean(oldValue), new Boolean(enabled));
    }
    
    
  /** Getter for property icon.
   * @return Value of property icon.
   */
    public ImageIcon getIcon() {
        return icon;
    }
  /** Setter for property icon.
   * @param icon New value of property icon.
   */
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
    
    
    public boolean isLeaf() {
        return (getChildren() == null);
    }
    
  /** Returns the number of levels above this node -- the distance from the
   * root to this node. If this node is the root, returns 0.
   * @return the number of levels above this node
   */
    public int getLevel() {
        OVTObject p = getParent();
        int i = 0;
        // do untill the node will not have a parent
        for (i=0; p != null; i++)
            p = p.getParent();
        return i;
    }
    
  /** Returns the path from the root, to get to this node. */
    public OVTObject[] getPath() {
        int level = getLevel();
        OVTObject[] path = new OVTObject[level + 1];
        path[level] = this;
        for (int i = level-1; i>=0; i--) {
            path[i] = path[i+1].getParent();
        }
        return path;
    }
    
  /** Returns the path from the root, to get to this node. */
    public String[] getPathStringArray() {
        int level = getLevel();
        OVTObject o = this;
        String[] path = new String[level + 1];
        for (int i = level; i>=0; i--) {
            path[i] = o.getName();
            o = o.getParent();
        }
        return path;
    }
    
  /** Returns the path from the root, to get to this node. */
    public String getPathString() {
        String[] path = getPathStringArray();
        String res = path[0];
        for (int i=1; i<path.length; i++)
            res += "." + path[i];
        return res;
    }
    
    public static String path2String(String[] path) {
        String res = path[0];
        for (int i=1; i<path.length; i++)
            res+= path[i];
        return res;
    }
    
    /** executes {@link ovt.datatype.Children#addChild(ovt.object.OVTObject) addChild()} method
     * of {@link #children this.children}.
     * <p>
     * Does not fire <CODE>children.fireChildRemoved(child)</CODE> !!!
     */
    public void addChild(OVTObject child) {
        //if (children == null) children = new Children();
        //child.setParent(this);
        children.addChild(child);
        //children.fireChildAdded(child);
    }
    
    /** removes child from children and does not fire <CODE>children.fireChildRemoved(child)</CODE> !!!
    public void removeChild(OVTObject child) throws IllegalArgumentException { // by oleg
        if (child.getParent() != this) throw new IllegalArgumentException(getName() + " is not a child.");
        children.remove(child);
        //children.fireChildRemoved(child);
    }*/
    
    /* It's not a good method
     everybody should implemment this by themselvs.
     
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
    } */
    
    /** 
     * Destructor. 
     * <li> disposes all WindowedPropertyEditors 
     * <li> removes all propertyListeners
     * <li> disposes all children (calls child.dispose() method)
     * <p>
     * One has to redefine this method if the object has something to dispose 
     * (like customizer) or unregister itself as a listener.
     * <p>
     * The purpose of this mehthod is to remove all pointers to and from this object
     * to outer environment. So the Gatbage Collector will be able to free this memory.
     * The second purpose is to close all windows/customizers of this object ;-)
     */
    public void dispose() {
        if (descriptors != null) {
            // dispose WindowedPropertyEditors
            Enumeration e = descriptors.elements();
            while (e.hasMoreElements()) {
                BasicPropertyDescriptor  pd = (BasicPropertyDescriptor)e.nextElement();
                OVTPropertyEditor editor = pd.getPropertyEditor();
                if (editor instanceof WindowedPropertyEditor) {
                    ((WindowedPropertyEditor)editor).dispose();
                }
            }
        }
        // remove all property listeners
        propertyChangeSupport.removeAllPropertyChangeListeners();
        // dispose children
        Enumeration e = children.elements();
        while (e.hasMoreElements()) ((OVTObject)e.nextElement()).dispose();
    }
    
    /*
    public OVTObject getChild(String name) throws IllegalArgumentException {
        if (children == null) return null;
        OVTObject obj = children.get(name);
        return obj;
    }*/
    
/*
  public OVTObject getObject(String[] path) throws IllegalArgumentException {
    OVTObject o = this;
    Children children;
    for (int i=0; i<path.length; i++, o = o.getChild(path[i])) {
        Log.log("Processing object: " + o.getName(), 5);
        if (!o.getName().equals(path[i])) {
            String msg = "Object '" + path2String(path) + "' not found.";
            Log.log(msg, 5);
            throw new IllegalArgumentException(msg);
        }
    }
    Log.log("Found object: " + o.getName(), 5);
    return o;
  }*/
    
    public OVTObject getObject(String path) throws IllegalArgumentException {
        Log.log("Call to getObject with path '" + path + "'", 7);
        StringTokenizer st = new StringTokenizer(path, ".");
        String myName = st.nextToken();
        int count = st.countTokens();
        Log.log("path has " + count + " tokens", 9);
        if (count == 0) {
            if (getName().equals(path)) {
                System.out.println(this + "::getObject(" + path + "): object found ");
                return this;
            }
            else throw new IllegalArgumentException("Object not found[1]: " + path);
        } else if (count > 0) {
            Log.log("count > 0 ", 9);
            OVTObject o = this; OVTObject nextO;            
            String child;
            while (st.hasMoreTokens()) {
                child = st.nextToken();
                Log.log("child = " + child, 7);
                nextO = o.getChildren().getChild(child);
                if (nextO == null) {
                    /*
                    BeansCollection bc = o.getBeanDesriptors();
                    BasicBeanDescriptor beanDesc = bc.getDescriptor(child);
                    if (beanDesc == null) throw new IllegalArgumentException("BeansSource: " + getName() +". Bean Descriptor not found: " + name);
                    try {
                        nextO = (OVTObject)beanDesc.getValue(o);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("Illegal Access: BeansSource: " + getName() +". Bean Descriptor: " + name);
                    }*/
                }
                if (nextO == null) throw new IllegalArgumentException("Object "+child+" not found in : " + path);
                o = nextO;
                Log.log("current obj = " + o.getName(), 7);
            }
            return o;
        } 
        else throw new IllegalArgumentException("Object not found[3]: " + path);
    }
    
    
        
    
/** returns the PropertyDescriptor */
    public BasicPropertyDescriptor getPD(String propPath) throws IllegalArgumentException {
        Log.log("Call to getPD with path '" + propPath + "'", 5);
        PropertyPath pp = new PropertyPath(propPath);
        OVTObject obj = getObject(pp.getObjectPath());
        Descriptors desc =  obj.getDescriptors();
        if (desc == null) throw new IllegalArgumentException("Object " + pp.getObjectPath() + " has no properies");
        BasicPropertyDescriptor pd = desc.getDescriptor(pp.getPropName());
        if (pd == null) throw new IllegalArgumentException("Object " + pp.getObjectPath() + " has no property " + pp.getPropName());
        return pd;
    }
    
/*  public OVTObject getObject(String path) throws IllegalArgumentException {
    Log.log("Call to getObject with path '" + path + "'", 5);
    StringTokenizer st = new StringTokenizer(path, ".");
    String myName = st.nextToken();
    int count = st.countTokens();
    Log.log("path has " + count + " tokens", 5);
    if (count == 0) {
        if (getName().equals(path)) return this;
        else throw new IllegalArgumentException("Object '" + path + "' not found.");
    } else if (count > 0) {
        String childPath = path.substring(path.indexOf(".")+1, path.length());
        String childName = st.nextToken();
        OVTObject o = getChild(childName);
        return o.getObject(childPath);
    } else throw new IllegalArgumentException("Object '" + path + "' not found.!!???");
 
  }*/
    
    
    
    
    
/** pathName should be MyName.ChildName.ChildsChildName....*/
  /** pathName should be MyName.ChildName.ChildsChildName....
   * private static Object getObject(String fullName, Object obj)
   * throws IllegalArgumentException {
   * int dot_index = fullName.indexOf(".");
   * // check if it is me (obj)
   * if (dot_index == -1) {
   * if (fullName.equals(getName())) return obj;
   * }
   *
   * String childName = fullName.substring(0, );
   * Log.log("getObject - childName = '"+childName+"'");
   * Children chldrn = null;
   * try {
   * chldrn = ((ChildrenSource)obj).getChildren();
   * } catch (ClassCastException e) {
   * throw new IllegalArgumentException("No such object : '" + fullName + "'");
   * }
   * OVTObject child = chldrn.get(childName);
   * if (child == null) throw new IllegalArgumentException("No such object : '" + fullName + "'");
   *
   * if (childName.indexOf(".") == -1) return child;
   * else {
   * // try to find child's child (vnuchok)
   * String childsChildName = childName.substring(childName.indexOf("."), childName.length());
   * return getObject(childsChildName, child);
   * }
   * }*/
    
/**
 * @return weather the object has to be shown in the tree while rendering it.
 */
    public boolean showInTree() {
        return showInTree;
    }
/** Will not be used in future. because all objects in Children should be visible in a TreePanel.
 * If true - the object will to be shown in the {@link ovt.gui.TreePanel TreePanel}. 
 * @param showInTree New value of property showInTree.
 * @see ovt.gui.TreePanel
 */
    public void showInTree(boolean showInTree) {
        this.showInTree = showInTree;
    }
    
    
/* *********************************
 *  Foloving metods needed for advansed show/hide (c) oleg
 */
    
    /**
     * @return 0 - has no visual child,
     * 1 - has visual child, but no one visible
     * 2 - has visible visual child
     */
    public int hasVisualChildEx() {
        
        Children children = getChildren();
        if (children == null) return 0;     // has no visual child
        
        int rc = 0;
        Enumeration e = children.elements();
        while(e.hasMoreElements() && rc < 2) {
            OVTObject obj = (OVTObject) e.nextElement();
            
            try {
                if (((VisualObject)obj).isVisible()) return 2;  // has visible child
                rc = 1; // no exception - has visual child
            } catch (ClassCastException e2) {}
            
            int rec_ret = obj.hasVisualChildEx();
            if (rc < rec_ret) rc = rec_ret;
        }
        
        //System.out.println("HasVisualChildEx returned " + rc);
        return rc;
    }
    
    public Vector getVisualLeafs() {
        Vector v = new Vector();
        addVisualLeafs(v);
        return v;
    }
    
    public Vector getVisibleLeafs() {
        Vector v = new Vector();
        addVisibleLeafs(v);
        return v;
    }
    
    
    public Vector getVisualChildren() {
        Vector v = new Vector();
        addVisualChildren(v);
        return v;
    }
    
    private boolean addVisualLeafs(Vector v) {
        Children children = getChildren();
        if (children == null) return false;
        boolean hasVisualChild = false;
        Enumeration e = children.elements();
        while(e.hasMoreElements()) {
            OVTObject obj = (OVTObject) e.nextElement();
            boolean hasVisualDescendant = obj.addVisualLeafs(v);
            if (!hasVisualDescendant && obj instanceof VisualObject) {
                v.addElement(obj);
                hasVisualChild = true;
            }
            hasVisualChild |= hasVisualDescendant;
        }
        return hasVisualChild;
    }
    
    private boolean addVisibleLeafs(Vector v) {
        Children children = getChildren();
        if (children == null) return false;
        boolean hasVisualChild = false;
        Enumeration e = children.elements();
        while(e.hasMoreElements()) {
            OVTObject obj = (OVTObject) e.nextElement();
            boolean hasVisualDescendant = obj.addVisualLeafs(v);
            if (!hasVisualDescendant && obj instanceof VisualObject) {
                if (((VisualObject)obj).isVisible())
                    v.addElement(obj);
                hasVisualChild = true;
            }
            hasVisualChild |= hasVisualDescendant;
        }
        return hasVisualChild;
    }
    
    
    private void addVisualChildren(Vector v) {
        Children children = getChildren();
        if (children == null) return;
        Enumeration e = children.elements();
        while(e.hasMoreElements()) {
            OVTObject obj = (OVTObject) e.nextElement();
            if (obj instanceof VisualObject) v.addElement(obj);
            obj.addVisualChildren(v);
        }
    }
    
}
