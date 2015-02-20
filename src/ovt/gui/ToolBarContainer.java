/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/ToolBarContainer.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.3 $
 
 
Copyright (c) 2000-2003 OVT Team
(Kristof Stasiewicz, Mykola Khotyaintsev, Yuri Khotyaintsev)
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

package ovt.gui;

import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;


/**
 *
 * @author  ko
 */
public class ToolBarContainer extends JPanel {
  
  private XYZWindow XYZwin;
  private XYZToolBar toolBar;
  private CameraToolBar cameraToolBar;
    
  /** Creates a new instance of ToolBarContainer */
  public ToolBarContainer(OVTCore core, XYZWindow XYZwin) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    //this.core = core;
    this.XYZwin = XYZwin;
    setBorder(new EmptyBorder(0, 0, 0, 0));
    //setBackground(Color.blue);
    
    // create main ToolBar
    toolBar = new XYZToolBar(core, XYZwin);
    
    // create Camera ToolBar
    cameraToolBar = new CameraToolBar(core, XYZwin);
    
    // place toolbars in the container one above each other
    
    add(toolBar);
    add(cameraToolBar);
    
    addComponentListener(new ComponentListener() {
        public void componentResized(ComponentEvent evt) {
            updatePreferredSize();
        }
        public void componentHidden(ComponentEvent evt) {}
        public void componentShown(ComponentEvent evt) {}
        public void componentMoved(ComponentEvent evt) {}
        
    });
    
    addContainerListener( new ContainerListener() {
        public void componentAdded(ContainerEvent evt) {
            updatePreferredSize();
        }
        
        public void componentRemoved(ContainerEvent evt) {
            updatePreferredSize();
        }
    });
    
    
  }

  public void updatePreferredSize() {
      int cpWidth = XYZwin.getContentPane().getWidth();
      setPreferredWidth(cpWidth);
  }
  
  /** Sets preferred width and computes and sets preferred height for this width*/
  public void setPreferredWidth(int width) {
      int newHeight = getPreferredHeightForFlowLayoutContainer(this,width);

      if (getPreferredSize().height != newHeight) {
          setPreferredSize( new Dimension(width, newHeight));
          invalidate();
          XYZwin.getContentPane().validate();
      }
      //System.out.println("CP.width="+XYZwin.getContentPane().getWidth());
      //System.out.println("toolBarsContainer.minsize="+getMinimumSize());
      //System.out.println("toolBarsContainer.preffsize="+getPreferredSize());
      //System.out.println("toolBarsContainer.maxsize="+getMaximumSize());
  }
  
  public static int getPreferredHeightForFlowLayoutContainer(Container cont, int width) {
    int minWidth = cont.getMinimumSize().width;
    int minHeight = cont.getMinimumSize().height;
    
    //System.out.println("toolBarsContainer.getComponentCount="+cont.getComponentCount());
    
    if (width < minWidth) {
        if (cont.getComponentCount() == 0) 
            return 5; // leave some space for the user to put the toolbar back           
        else
            return minHeight*cont.getComponentCount();
    } else {
        if (cont.getComponentCount() == 0)
            return 5; // leave some space for the user to put the toolbar back        
        else 
            return minHeight;
    }
  }
    
}
