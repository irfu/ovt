/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/CurrentMjdToolbarComponents.java,v $
Date:      $Date: 2003/09/28 17:52:40 $
Version:   $Revision: 2.3 $


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
 * CurrentMjdToolbarComponents.java
 *
 * Created on June 22, 2002, 8:26 PM
 */

package ovt.gui;

import ovt.*;
import ovt.util.*;
import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 *
 * @author  ko
 * @version 
 */
public class CurrentMjdToolbarComponents implements 
        TimeChangeListener, ActionListener {

    private TimeSettings timeSettings;
    private JButton rewind, back, play, forward, fastForward;
    private JComboBox timeBox;
    
/** Creates new CurrentMjdToolbarComponents */
public CurrentMjdToolbarComponents(TimeSettings ts) {
    this.timeSettings = ts;
    timeSettings.addTimeChangeListener(this);

    try {
        rewind = new JButton(new ImageIcon(Utils.findResource("images/VCRRewind.gif")));
    } catch (java.io.FileNotFoundException e2) { 
        e2.printStackTrace(System.err);
        rewind = new JButton();
    }
    rewind.setName("rewind");
    rewind.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            getTimeSet().setCurrentMjdIndex(0);
            timeSettings.fireCurrentMjdChange();
            timeSettings.Render();
        }
    });
    rewind.setAlignmentY(0.5f);
    rewind.setToolTipText("Start");
    
    try {
        back = new JButton(new ImageIcon(Utils.findResource("images/VCRBack.gif")));
    } catch (java.io.FileNotFoundException e2) { 
        e2.printStackTrace(System.err);
        back = new JButton();
    }
    back.setName("back");
    back.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            getTimeSet().setCurrentMjdIndex(getTimeSet().getCurrentMjdIndex() - 1);
            timeSettings.fireCurrentMjdChange();
            timeSettings.Render();
        }
    });
    back.setAlignmentY(0.5f);
    back.setToolTipText("Step back");
        
    timeBox = new JComboBox();
    //timeBox.setFont(Style.getMenuFont());
    timeBox.setToolTipText("Time");
    
    try {
        forward = new JButton(new ImageIcon(Utils.findResource("images/VCRForward.gif")));
    } catch (java.io.FileNotFoundException e2) { 
        e2.printStackTrace(System.err);
        forward = new JButton();
    }
    forward.setName("forward");
    forward.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            getTimeSet().setCurrentMjdIndex(getTimeSet().getCurrentMjdIndex() + 1);
            timeSettings.fireCurrentMjdChange();
            timeSettings.Render();
        }
    });
    forward.setAlignmentY(0.5f);
    forward.setToolTipText("Step forward");
    
    try {
        fastForward = new JButton(new ImageIcon(Utils.findResource("images/VCRFastForward.gif")));
    } catch (java.io.FileNotFoundException e2) { 
        e2.printStackTrace(System.err);
        fastForward = new JButton();
    }
    fastForward.setName("fastForward");
    fastForward.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            getTimeSet().setCurrentMjdIndex(getTimeSet().getMaxCurrentMjdIndex());
            timeSettings.fireCurrentMjdChange();
            timeSettings.Render();
        }
    });
    fastForward.setAlignmentY(0.5f);
    fastForward.setToolTipText("End");
        
    
    updateComboBoxDataModel();
    refresh();
  }
  
  private TimeSet getTimeSet() {
    return timeSettings.getTimeSet();
  }
  
  public Component[] getComponents() {
    return new Component[]{rewind, back, timeBox, forward, fastForward};
  }
  
private void updateComboBoxDataModel() {
    if (OVTCore.DEBUG > 3) System.out.print("CurrentMjdVCRComponents.updateCombobox ");
    timeBox.removeActionListener(this);
    
    double[] values = getTimeSet().getValues();
    Object[] tags = new Object[values.length];
    for (int i=0; i<tags.length; i++) {
      tags[i] = new Time(values[i]).toString();
      if (OVTCore.DEBUG > 3) System.out.print(" "+i);
    }
    
    timeBox.setModel(new DefaultComboBoxModel(tags));
    //timeBox.setremoveAllItems();
    
    timeBox.setMaximumSize(timeBox.getPreferredSize());
    timeBox.addActionListener(this);
    if (OVTCore.DEBUG > 3) System.out.println("done.");
}

private void refresh() {
    int currentMjdIndex = getTimeSet().getCurrentMjdIndex();
    
    // adjust timeBox
    timeBox.removeActionListener(this);
    //System.out.println("adjust textBox by '" + editor.getAsText() +"'");
    //timeBox.setSelectedItem(editor.getAsText());
    timeBox.setSelectedIndex(currentMjdIndex);
    timeBox.addActionListener(this);
    
    
    // adjust buttons
    if (currentMjdIndex == 0) { // isStart()
      //System.out.println("Start!!!!!!!!");
      rewind.setEnabled(false);
      back.setEnabled(false);
      forward.setEnabled(true);
      fastForward.setEnabled(true);
    } else if (currentMjdIndex == getTimeSet().getMaxCurrentMjdIndex()) { //isStop()
      //System.out.println("Stop!!!!!!!!");
      rewind.setEnabled(true);
      back.setEnabled(true);
      forward.setEnabled(false);
      fastForward.setEnabled(false);
    } else {
      //System.out.println("aaa!!!!!!!!");
      rewind.setEnabled(true);
      back.setEnabled(true);
      forward.setEnabled(true);
      fastForward.setEnabled(true);
    }
}
  
public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
        updateComboBoxDataModel();
        refresh();
    } else // curentMjdChanged
        refresh();
}

/** Listen to action event from timeBox */
public void actionPerformed(java.awt.event.ActionEvent evt) {
    // It's better to use getSelectedIndex here, but we use this for testing
    getTimeSet().setCurrentMjdIndex(timeBox.getSelectedIndex());
    timeSettings.fireCurrentMjdChange();
    timeSettings.Render();
}


}
