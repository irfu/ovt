/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/TimeSettingsCustomizer.java,v $
Date:      $Date: 2003/09/28 17:52:42 $
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
 * TimeSettingsCustomizer.java
 *
 * Created on November 27, 2000, 7:29 PM
 */

package ovt.gui;


import ovt.*;
import ovt.mag.*;
import ovt.util.*;
import ovt.event.*;
import ovt.beans.*;
import ovt.object.*;
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
  private TimeSet timeSet, oldTimeSet;
  
  /** Creates new TimeSettingsCustomizer */
  public TimeSettingsCustomizer(TimeSettings ts) {
      super();
      setTitle("Time Settings");
      try {
            setIconImage (Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/Clock.gif")));
	} catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
      this.timeSettings = ts;
      timeSettings.addTimeChangeListener(this);
      timeSet = new TimeSet(this.timeSettings);
      timeSet.addPropertyChangeListener(this);
      
      // make interior
      
      Container cont = getContentPane();
      //cont.setBorder(BorderFactory.createEmptyBorder(5,10,0,10));
      cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
      
      JPanel mainPanel = mainPanel();
      
      JPanel panel = new JPanel();
      panel.setLayout(new FlowLayout());
      
      JButton button = new JButton("<<");
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              timeSet.setStartMjd(timeSet.getStartMjd() - timeSet.getIntervalMjd());
          }
      });
      button.setPreferredSize(new Dimension(button.getPreferredSize().width, mainPanel.getPreferredSize().height));
      panel.add(button);
      
      
      panel.add(mainPanel);
      
      button = new JButton(">>");
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
              timeSet.setStartMjd(timeSet.getStartMjd() + timeSet.getIntervalMjd());
          }
      });
      button.setPreferredSize(new Dimension(button.getPreferredSize().width, mainPanel.getPreferredSize().height));
      panel.add(button);
      
      
      cont.add(panel);
      // ------------------- close, reset buttons ----------------
      
      panel = new JPanel();
      //panel.setAlignmentX(LEFT_ALIGNMENT);
      panel.setLayout(new java.awt.GridLayout (1, 3, 10, 10));
      
      button = new JButton("Cancel");
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

    
private JPanel mainPanel() {
        JPanel comp = new JPanel(false);
        comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
        
        // ------------------- close, reset buttons ----------------
        
        Descriptors desc = timeSet.getDescriptors();
        
        TextFieldEditorPanel start = (TextFieldEditorPanel)((ComponentPropertyEditor)(desc.getDescriptor("startMjd").getPropertyEditor())).getComponent();
        start.setEditCompleteOnKey(true);
        TextFieldEditorPanel interval = (TextFieldEditorPanel)((ComponentPropertyEditor)(desc.getDescriptor("intervalMjd").getPropertyEditor())).getComponent();
        interval.setEditCompleteOnKey(true);
        TextFieldEditorPanel step = (TextFieldEditorPanel)((ComponentPropertyEditor)(desc.getDescriptor("stepMjd").getPropertyEditor())).getComponent();
        //step.showMessageOnEror(false);
        step.setEditCompleteOnKey(true);
        
        JLabel label = new JLabel("Start ");
        label.setAlignmentX(CENTER_ALIGNMENT);
        comp.add(label);
        comp.add(start);

        label = new JLabel("Interval ");
        label.setAlignmentX(CENTER_ALIGNMENT);
        comp.add(label);
        comp.add(interval);
        
        label = new JLabel("Tracing Step");
        label.setAlignmentX(CENTER_ALIGNMENT);
        comp.add(label);
        comp.add(step);    
        return comp;
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
    timeSet.setStartMjd(timeSettings.getStartMjd());
    timeSet.setIntervalMjd(timeSettings.getIntervalMjd());
    timeSet.setStepMjd(timeSettings.getStepMjd());
}


private void applyAction() throws IllegalArgumentException {
    if (valuesChanged()) {
        timeSet.setStartMjd(timeSet.getStartMjd());
        timeSet.setIntervalMjd(timeSet.getIntervalMjd());
        timeSet.setStepMjd(timeSet.getStepMjd());
        timeSettings.fireTimeSetChange();
        timeSettings.Render();
    }
}

public void timeChanged(TimeEvent evt) {
    //String name = evt.getPropertyName();
    String name = "";
    if (name.equals("time")) {
        applyButton.setEnabled(false);
        revert();
    }
}

/** Listen to timeSet. if it changes - update applyButon state */
public void propertyChange(java.beans.PropertyChangeEvent evt) {
    applyButton.setEnabled(valuesChanged());
}

}
