/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/XYZToolBar.java,v $
  Date:      $Date: 2003/09/28 17:52:42 $
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

package ovt.gui;

import ovt.*;
import ovt.gui.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.object.editor.*;
import ovt.datatype.*;


import vtk.*;

import java.beans.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class XYZToolBar extends JToolBar implements ActionListener {

  private OVTCore core;
  private XYZWindow XYZwin;


  public XYZToolBar(OVTCore core, XYZWindow xyzwin) {
    super("Time and CS Toolbar");
    XYZwin = xyzwin;
    this.core = core;
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    setMargin(new Insets(1, 1, 1, 1));
  
    addCoordinateSystemComboBox();
    addPolarCoordinateSystemComboBox();
    addSeparator();
    addButtons();
    addSeparator();
    addVCRButtons();
    
    
    // insert spacer which will expand if needed??? let's try
    //add(new JComponent());
    	
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	
  }

public OVTCore getCore()
	{ return core; }

public XYZWindow getVW()
	{ return XYZwin; }

  protected void addButtons() {

	JButton button = null;

        String imgDir = OVTCore.getImagesDir();
        
	/*button = new JButton(new ImageIcon(imgDir+"Save.gif"));
	button.setName("Export");
	button.setToolTipText("Export image");
	button.addActionListener(this);
	button.setAlignmentY(0.5f);
        add(button);
        
        
	button = new JButton(new ImageIcon(imgDir+"Print.gif"));
	button.setName("Print");
	button.setToolTipText("Print");
	button.addActionListener(this);
        button.setAlignmentY(0.5f);
        if ( !XYZwin.windowResizable ){ 
          button.setEnabled(false);
        }
	add(button);
        
	addSeparator();*/
        try {
            button = new JButton(new ImageIcon(Utils.findResource("images/Clock.gif")));
        } catch (java.io.FileNotFoundException e2) { 
            e2.printStackTrace(System.err);
            button = new JButton();
        }
	button.setToolTipText("Set time");
	button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getCore().getTimeSettings().setCustomizerVisible(true);
            }
        });
        button.setAlignmentY(0.5f);
	add(button);
        
        
        /*button = new UpdateManetosphereButton(new ImageIcon(imgDir+"magnetopause.gif"), getCore().getMagnetosphere());
	button.setEnabled(false);
        button.setAlignmentY(0.5f);
	add(button);*/
  }

  
  protected void addCoordinateSystemComboBox() {
    BasicPropertyDescriptor  pd = getCore().getCoordinateSystem().getDescriptors().getDescriptor("coordinateSystem");
    JComboBox comp = (JComboBox)((ComponentPropertyEditor)pd.getPropertyEditor()).getComponent();
    comp.setMaximumSize(comp.getPreferredSize());
    add(comp);
  }

  protected void addPolarCoordinateSystemComboBox() {
    BasicPropertyDescriptor  pd = getCore().getCoordinateSystem().getDescriptors().getDescriptor("polarCoordinateSystem");
    JComboBox comp = (JComboBox)((ComponentPropertyEditor)pd.getPropertyEditor()).getComponent();
    comp.setMaximumSize(comp.getPreferredSize());
    add(comp);
  }

  
protected void addVCRButtons() {
  
  Component[] comps = new CurrentMjdToolbarComponents(core.getTimeSettings()).getComponents();
  for (int i=0; i<comps.length; i++) {
    add(comps[i]);
  }
}

  public void actionPerformed(ActionEvent e) {
	Component  comp = (Component)e.getSource();
	String compName = comp.getName();
	if (compName.equals("Export")) ImageOperations.exportImageDialog(getCore());
	
	if (compName.equals("Print"))  ImageOperations.print(getCore());
  }

}

class UpdateManetosphereButton extends JButton implements PropertyChangeListener {
    
    Magnetosphere mps;
    
    UpdateManetosphereButton(ImageIcon icon, Magnetosphere amps) {
        super(icon);
        this.mps = amps;
        setToolTipText("Update magnetospheric structure");
	addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mps.update();
            }
        });
        mps.addPropertyChangeListener("updated", this);
        mps.addPropertyChangeListener("visible", this);
    }
    
    /** Listen to magnetosphere changes. */
    public void propertyChange(PropertyChangeEvent evt) {
            boolean visible = mps.isVisible();
            boolean updated = mps.isUpdated();
            if (visible == true) {
                if (isEnabled() != !updated) setEnabled(!updated);
            } else if (isEnabled()) setEnabled(false);
    }
    
}
