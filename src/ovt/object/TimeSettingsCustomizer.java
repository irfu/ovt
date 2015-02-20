/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/TimeSettingsCustomizer.java,v $
Date:      $Date: 2003/09/28 17:52:52 $
Version:   $Revision: 1.2 $


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
 * TimeSettingsCustomizer.java
 *
 * Created on November 27, 2000, 7:29 PM
 */

package ovt.object;


import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.object.editor.*;

import vtk.*;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.beans.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 *
 * @author  ko
 * @version 
 */
public class TimeSettingsCustomizer extends CustomizerDialog 
    implements TimeChangeListener, WindowListener, PropertyChangeListener
{
  private TimeSettings timeSettings;
  private JButton applyButton, okButton;
  private TimeSet timeSet;
  private TimeSetCustomizer customizer;
  
  /** Creates new TimeSettingsCustomizer */
  public TimeSettingsCustomizer(TimeSettings ts) {
      super();
      setTitle("Time Settings");
      try {
            setIconImage (Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/Clock.gif")));
	} catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
      this.timeSettings = ts;
      timeSettings.addTimeChangeListener(this);
      timeSet = (TimeSet)timeSettings.getTimeSet().clone();
      
      // make interior
      
      Container cont = getContentPane();
      //cont.setBorder(BorderFactory.createEmptyBorder(5,10,0,10));
      cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
      
      customizer = new TimeSetCustomizer();
      customizer.setObject(timeSet);
      
      // listen if customier changes any of the values
      // to enable apply button 
      PropertyChangeListener applyButtonStateUpdater = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            applyButton.setEnabled(valuesChanged());
        }
      };
      
      customizer.addPropertyChangeListener("startMjd", applyButtonStateUpdater);
      customizer.addPropertyChangeListener("intervalMjd", applyButtonStateUpdater);
      customizer.addPropertyChangeListener("stepMjd", applyButtonStateUpdater);
      
      cont.add(customizer);
      // ------------------- close, reset buttons ----------------
      
      JPanel panel = new JPanel();
      //panel.setAlignmentX(LEFT_ALIGNMENT);
      panel.setLayout(new java.awt.GridLayout (1, 3, 10, 10));
      
      JButton button = new JButton("Cancel");
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              setVisible(false);
              revert();
          }
      });
      panel.add(button);
      
      okButton = new JButton("OK");
      okButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              try {
                applyAction();
                setVisible(false);
              } catch (IllegalArgumentException e2) {
                timeSettings.getCore().sendErrorMessage("Input error", e2.getMessage());
              } catch (SyncException e3) {
                timeSettings.getCore().sendErrorMessage("Input error", e3.getMessage());
                e3.getSource().requestFocus();
              }
          }
      });
      panel.add(okButton);
      
      applyButton = new JButton("Apply");
      applyButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              try {
                applyAction();
              } catch (IllegalArgumentException e2) {
                timeSettings.getCore().sendErrorMessage("Input error", e2.getMessage());
              } catch (SyncException e3) {
                timeSettings.getCore().sendErrorMessage("Input error", e3.getMessage());
                e3.getSource().requestFocus();
              }
          }
      });
      applyButton.setEnabled(false);
      panel.add(applyButton);
      
      cont.add(panel);
      
      pack();
      //setResizable(false);
      
      // senter the window
      Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension windowSize = getSize();
      setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
    }


private boolean valuesChanged() {
    return !(timeSet.equals(timeSettings.getTimeSet()));
}


public void windowClosed(java.awt.event.WindowEvent p1) {    
    //System.out.println("windowClosed");
}

public void windowDeiconified(java.awt.event.WindowEvent p1) {
    //System.out.println("windowDeiconified");
}

public void windowOpened(java.awt.event.WindowEvent p1) {
    //System.out.println("windowOpened");
}

public void windowIconified(java.awt.event.WindowEvent p1) {
    //System.out.println("windowIconified");
}

public void windowClosing(java.awt.event.WindowEvent p1) {
    //System.out.println("windowClosing");
}

public void windowActivated(WindowEvent evt) {
    //System.out.println("windowActivated");
}

public void windowDeactivated(WindowEvent evt) {
    //System.out.println("windowDeactivated -> Reverting....");
    //revert();
}

  /** Sets previous values */
private void revert() { 
    timeSet = (TimeSet)timeSettings.getTimeSet().clone();
    customizer.setObject(timeSet);
    applyButton.setEnabled(false);
}

private void applyAction() throws IllegalArgumentException, SyncException {
    
    customizer.sync();
    //Log.log("valuesChanged()="+valuesChanged());
    if (valuesChanged()) {
        timeSettings.setTimeSet(timeSet);
        //timeSettings.fireTimeChange();
        timeSettings.Render();
    }
}

public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged())  revert();
}

/** Listen to timeSet. if it changes - update applyButon state */
public void propertyChange(java.beans.PropertyChangeEvent evt) {
    applyButton.setEnabled(valuesChanged());
}

}
