/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/OVTNode.java,v $
  Date:      $Date: 2003/09/28 17:52:41 $
  Version:   $Revision: 2.2 $


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
 * OVTNode.java
 *
 * Created on March 15, 2000, 9:45 AM
 */
 
package ovt.gui;

import ovt.datatype.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.event.*;
import ovt.object.*;
import ovt.interfaces.*;

import java.beans.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

/** 
 *
 * @author  root
 * @version 
 */
public class OVTNode extends DefaultMutableTreeNode 
        implements ChildrenListener, PropertyChangeListener {
    
  protected TreePanel treePanel;
  
  /** Creates new OVTNode */
  public OVTNode(Object obj, TreePanel treePanel) {
    this.treePanel = treePanel;
    setUserObject(obj);
    
    //if (Object implements OVTTreeNode)
    
    try {
      OVTObject ovtObj = ((OVTObject)getUserObject());
      if (!ovtObj.showInTree()) return;
      ovtObj.addPropertyChangeListener("enabled", this);
      ovtObj.addPropertyChangeListener("visible", this);
      ovtObj.addPropertyChangeListener("name", this);
    } catch (ClassCastException e2) {
      System.out.println(e2.toString() + " -> OVTObject");
    }
    
    try {
      Children children = ((ChildrenSource)getUserObject()).getChildren();
      
      if (children != null) {
          children.addChildrenListener(this);
          addChildren(children);
      }
    } catch (ClassCastException e2) {  e2.printStackTrace();
    }

    
    
  }

  private void addChildren(Children children) {
     try {
      if (children != null) {
        Enumeration e = children.elements();
        Object o;
        while (e.hasMoreElements()) {
            o = e.nextElement();
            try {
                if (((OVTObject)o).showInTree()) {
                    //ovt.util.Log.log("adding Child "+o);
                    add(new OVTNode(o, treePanel));
                }
            } catch (ClassCastException ignore) {
                add(new OVTNode(o, treePanel));
            }
        }
      }
    } catch (ClassCastException e2) {  e2.printStackTrace();
    }
  }
  
  
  public boolean getAllowsChildren() {
    boolean result = false;
    try { 
      Children children = ((OVTTreeNode)getUserObject()).getChildren();  
      if (children != null) return false;
      if (children.size() == 0) return false;
    } catch (ClassCastException e2) {}
    return false;
  }
  
  public String toString() {
    try { 
      return ((OVTTreeNode)getUserObject()).getName();
    } catch (ClassCastException e2) {}
    // else
    return getUserObject().toString();
  }
  
  
  public void propertyChange(PropertyChangeEvent evt) {
    //System.out.println("OVTNode recieved event - " + evt.getPropertyName());

    if (evt.getPropertyName().equals("visible")) {
      treePanel.nodeChanged(this);
      return;
    }
    
    if (evt.getPropertyName().equals("enabled")) {
      treePanel.nodeChanged(this);
      return;
    }

    if (evt.getPropertyName().equals("name")) {
//DBG*/ System.out.println("OVTNode::propertyChange() -> updating node");
      treePanel.nodeChanged(this);
      return;
    }
    
    
  }
 
    /** sets user object of this node visible or not */
    public void userObjectSetVisible(boolean visible) {
        try {
           ((VisualObject) getUserObject()).setVisible(visible);
        } 
        catch(ClassCastException e) {
            System.err.println("Error: not a VisualObject. " + e);
        }
    }

    /** returns user object isVisible() */
    public boolean userObjectIsVisible() {
        try {
           return ((VisualObject) getUserObject()).isVisible();
        } 
        catch(ClassCastException e) {
            System.err.println("Error: not a VisualObject. " + e);
            return false;
        }
    }
    
    
    public void childAdded(ChildrenEvent evt) {
        //DBG*/ System.out.println("OVTNode::propertyChange() -> adding child");
                treePanel.getTreeModel().insertNodeInto(
                    new OVTNode(evt.getChild(), treePanel),  // Node to add
                    this,                                   // parent
                    this.getChildCount()                  // insert position
                );
    }
    
    public void childRemoved(ChildrenEvent evt) {
        //DBG* System.out.println("OVTNode::propertyChange() -> removing node");
                //if (getParent() != null) treePanel.getTreeModel().removeNodeFromParent(this);
                //Log.log("evt.getChild()="+evt.getChild());
                OVTNode node = findChildNode(evt.getChild());
                treePanel.getTreeModel().removeNodeFromParent(node);
    }
    
    
    public void childrenChanged(ChildrenEvent evt) {
        removeAllChildren();
        Children children = ((ChildrenSource)getUserObject()).getChildren();
        // children = (Children)evt.getSource() - the same
        /*Enumeration e = children.elements();
        Object o;
        while (e.hasMoreElements()) {
            o = e.nextElement();
            try {
                if (((OVTObject)o).showInTree()) {
                    ovt.util.Log.log("adding Child "+((NamedObject)o).getName());
                }
            } catch (ClassCastException ignore) {
            }
        } */
        addChildren(children); 
        treePanel.getTreeModel().nodeStructureChanged(this);
    }
    
    /** Searches for the child node with the user object obj */
    private OVTNode findChildNode(OVTObject obj) {
        Enumeration e = children();
        while (e.hasMoreElements()) {
            OVTNode node = (OVTNode)e.nextElement();
            //Log.log("-->childNode="+node);
            if (node.getUserObject().equals(obj)) return node;
        }
        return null;
    }
    
    
}
