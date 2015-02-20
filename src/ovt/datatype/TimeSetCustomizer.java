/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/datatype/TimeSetCustomizer.java,v $
Date:      $Date: 2003/09/28 17:52:38 $
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
 * TimeSetCustomizer.java
 *
 * Created on June 21, 2002
 */

package ovt.datatype;

import ovt.beans.*;
import ovt.beans.editor.*;
import ovt.object.*;

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
public class TimeSetCustomizer extends JPanel 
    implements Customizer, Syncer
{
  private TimeSet timeSet;
  private static final int START = 0; 
  private static final int INTERVAL=1; 
  private static final int STEP=2;
  private static final String prop[] = { "startMjd", "intervalMjd", "stepMjd" };
  
  private PropertyEditor[] editor = new PropertyEditor[3];
  private MjdEditorPanel startTextField;
  private IntervalEditorPanel intervalTextField, stepTextField;
  
  /** Creates new TimeSettingsCustomizer */
public TimeSetCustomizer() {
    // create editors
    
    // start editor
    editor[START] = new ovt.beans.editor.MjdEditor();
    editor[START].addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            double mjd = ((Double)editor[START].getValue()).doubleValue();
            double oldMjd = timeSet.getStartMjd();
            timeSet.setStartMjd(mjd);
            firePropertyChange(prop[START], oldMjd, mjd);
        }
    });
    // interval editor
    editor[INTERVAL] = new ovt.beans.editor.IntervalEditor(); 
    editor[INTERVAL].addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            double mjd = ((Double)editor[INTERVAL].getValue()).doubleValue();
            double oldMjd = timeSet.getIntervalMjd();
            timeSet.setIntervalMjd(mjd);
            firePropertyChange(prop[INTERVAL], oldMjd, mjd);
        }
    });
    // step editor
    editor[STEP] = new ovt.beans.editor.IntervalEditor(); 
    editor[STEP].addPropertyChangeListener( new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            double mjd = ((Double)editor[STEP].getValue()).doubleValue();
            double oldMjd = timeSet.getStepMjd();
            timeSet.setStepMjd(mjd);
            firePropertyChange(prop[STEP], oldMjd, mjd);
        }
    });
    
    // make interior
    
    JPanel mainPanel = createMainPanel();
    
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout());
    
    JButton button = new JButton("<<");
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            timeSet.setStartMjd(timeSet.getStartMjd() - timeSet.getIntervalMjd());
            setObject(timeSet);
            firePropertyChange(prop[START], null, null);
        }
    });
    button.setPreferredSize(new Dimension(button.getPreferredSize().width, mainPanel.getPreferredSize().height));
    panel.add(button);
    
    
    panel.add(mainPanel);
    
    button = new JButton(">>");
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            timeSet.setStartMjd(timeSet.getStartMjd() + timeSet.getIntervalMjd());
            setObject(timeSet);
            firePropertyChange(prop[START], null, null);
        }
    });
    button.setPreferredSize(new Dimension(button.getPreferredSize().width, mainPanel.getPreferredSize().height));
    panel.add(button);
    
    add(panel);
    
    
}

    
private JPanel createMainPanel() {
    JPanel comp = new JPanel(false);
    comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
    
    // ------------------- close, reset buttons ----------------
    
    //PropertyDescriptors desc = new PropertyDescriptors(timeSet.getClass());
    
    /*for (int i=0; i<tf.length; i++) {
        //PropertyDescriptor pd = desc.get(props[i]);
        //PropertyEditor pe = Settings.findEditor(pd);
        //tf[i] = (JTextField)pe.getCustomEditor();
        tf[i] = new JTextField();
    }*/
    
    //start.setEditCompleteOnKey(true);
    
    startTextField = (MjdEditorPanel)editor[START].getCustomEditor();
    startTextField.requestDefaultFocus();
    intervalTextField = (IntervalEditorPanel)editor[INTERVAL].getCustomEditor();
    stepTextField = (IntervalEditorPanel)editor[STEP].getCustomEditor();
    
    JLabel label = new JLabel("Start ");
    label.setAlignmentX(CENTER_ALIGNMENT);
    comp.add(label);
    comp.add(startTextField);
    
    label = new JLabel("Interval ");
    label.setAlignmentX(CENTER_ALIGNMENT);
    comp.add(label);
    comp.add(intervalTextField);
    
    label = new JLabel("Tracing Step");
    label.setAlignmentX(CENTER_ALIGNMENT);
    comp.add(label);
    comp.add(stepTextField);
    
    return comp;
}
    

public void setObject(final java.lang.Object obj) {
    this.timeSet = (TimeSet)obj;
    editor[START].setValue(new Double(timeSet.getStartMjd()));
    editor[INTERVAL].setValue(new Double(timeSet.getIntervalMjd()));
    editor[STEP].setValue(new Double(timeSet.getStepMjd()));
}

public void actionPerformed(final java.awt.event.ActionEvent p1) {
}

public void sync() throws SyncException {
    startTextField.sync();
    intervalTextField.sync();
    stepTextField.sync();
}

}
