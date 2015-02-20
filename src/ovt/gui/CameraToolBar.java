/*=========================================================================
 
  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/CameraToolBar.java,v $
  Date:      $Date: 2009/10/27 12:14:36 $
  Version:   $Revision: 2.4 $
 
 
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
import ovt.beans.*;
import ovt.object.*;
import ovt.object.editor.*;
import ovt.datatype.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 *
 * @author  ko
 */
public class CameraToolBar extends JToolBar {
  private OVTCore core;
  private XYZWindow XYZwin;

  /** Creates a new instance of CameraToolBar */
  public CameraToolBar(OVTCore core, XYZWindow XYZwin) {
    super("Camera Toolbar");
    this.XYZwin = XYZwin;
    this.core = core;
    setMargin(new Insets(1, 1, 1, 1));
    
    // View Control
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/camera.gif")));
        button.setToolTipText("View Control");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setCustomizerVisible(true);
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); }     
    
    addSeparator();
    
    // Look at northern hemisphere    
        
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/north_hem.gif")));
    	button.setToolTipText("Look at northern hemisphere");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().lookAtNothHemisphere();
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
        
    // Look at southern hemisphere
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/south_hem.gif")));
        button.setToolTipText("Look at southern hemisphere");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().lookAtSouthHemisphere();
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); }     
    
    addSeparator();
    
    // view from x
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/x.gif")));
        button.setToolTipText("View from X");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_X);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
    // view from -x

    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/minusx.gif")));
        button.setToolTipText("View from -X");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_MINUS_X);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
    
    // view from y
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/y.gif")));
        button.setToolTipText("View from Y");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_Y);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
    // view from -x
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/minusy.gif")));
        button.setToolTipText("View from -Y");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_MINUS_Y);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
    
    // view from z
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/z.gif")));
        button.setToolTipText("View from Z");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_Z);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
    // view from -Z
    try {
        JButton button = new JButton(new ImageIcon(Utils.findResource("images/minusz.gif")));
        button.setToolTipText("View from -Z");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getCamera().setViewFrom(Camera.VIEW_FROM_MINUS_Z);
                getCore().Render();
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
    } catch (java.io.FileNotFoundException e2) { e2.printStackTrace(System.err); } 
    
  }

  private OVTCore getCore() {
    return core;
  }
}

