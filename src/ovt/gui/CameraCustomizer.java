/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/CameraCustomizer.java,v $
Date:      $Date: 2003/09/28 17:52:40 $
Version:   $Revision: 2.12 $


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
 * CameraCustomizer.java
 *
 * Created on September 28, 2000, 5:51 PM
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


public class CameraCustomizer extends JFrame implements PropertyChangeListener {
    
    /* first element corresponds to Camera.PARALLEL_PROJECTION, 
     * second - PERSPECTIVE_PROJECTION. Is used to indicate
     * panel in CardLayout container.
     */
    private static final String[] SCALE_PANNEL = { "Parallel Scaler Pannel", "R Pannel"};
    /** The panel which holds R and ParallelScale panels in card layout*/
    private JPanel scalePanel;
    protected Camera cam;
    private Descriptors desc;
    protected JComboBox viewFromComboBox;
    protected JComboBox viewToComboBox;
    protected JComboBox projectionComboBox;
    
    
public CameraCustomizer(Camera acam, Window parent) {
    super();
    setTitle("View Control");
    try {
        setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.findResource("images/camera.gif")));
    } catch (FileNotFoundException e2) { e2.printStackTrace(System.err); }
    this.cam = acam;
    desc = cam.getDescriptors();
    
    viewFromComboBox = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("viewFrom").getPropertyEditor())).getComponent();
    viewToComboBox = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("viewTo").getPropertyEditor())).getComponent();
    projectionComboBox = (JComboBox)((ComponentPropertyEditor)(desc.getDescriptor("projection").getPropertyEditor())).getComponent();
    
    Dimension dim = viewFromComboBox.getPreferredSize();
    dim.width = 100;
    viewFromComboBox.setPreferredSize(dim);
    viewToComboBox.setPreferredSize(dim);
    
    Container cont = getContentPane();
    //create layout : new java.awt.GridLayout (4, 1, 5, 5)
    cont.setLayout(new BoxLayout(cont,BoxLayout.Y_AXIS));
    //cont.set
    //cont.setLayout(new BorderLayout(0,5));
    
    // Undo and redo buttons panel
        /* JPanel undoredoPanel = new JPanel();
        undoredoPanel.setLayout(new java.awt.GridLayout (1, 2, 10, 10));
         
        JButton undoButton = new JButton("Undo");
        undoButton.setEnabled(false);
        undoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.out.println("Not already implemented :-)");
            }
        });
        undoredoPanel.add(undoButton);
         
        JButton redoButton = new JButton("Redo");
        redoButton.setEnabled(false);
        redoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.out.println("Not already implemented ;-)");
            }
        });
        undoredoPanel.add(redoButton);
         
        cont.add(undoredoPanel); */
    
    // -------- views Panel -------
    
    JPanel viewsPanel = createViewsPanel();
    
    cont.add(viewsPanel); //, BorderLayout.NORTH
    
    // -------- positions Panel -------
    
    JPanel positionPanel = createPositionPanel();
    
    cont.add(positionPanel); //, BorderLayout.CENTER
    
    
    // -------- Projection Panel -------
    
    JPanel panel = new JPanel();
    panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS) );
    panel.add(new JLabel("Projection:"));
    panel.add(Box.createHorizontalGlue());
    panel.add(projectionComboBox);
    
    cont.add(panel);
    
    // -------- Camera light controls -------
    
    //Component lightControls = ((ComponentPropertyEditor)(desc.getDescriptor("lightIntensity").getPropertyEditor())).getComponent();
    //cont.add(lightControls, BorderLayout.EAST);
    
    // ------------------- close, reset buttons ----------------
    
    panel = new JPanel();
    panel.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
    
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            cam.setCustomizerVisible(false);
        }
    });
    panel.add(closeButton);
    
    addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            cam.setCustomizerVisible(false);
        }
    });
    
    JButton resetButton = new JButton("Reset");
    resetButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            cam.reset();
            cam.Render();
        }
    });
    panel.add(resetButton);
    
    cont.add(panel); //, BorderLayout.SOUTH
    
    
    
    pack();
    //setResizable(false);
}

private JPanel createViewsPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(new JLabel("From"));
    panel.add(Box.createHorizontalStrut(5));
    panel.add(viewFromComboBox);
    panel.add(Box.createHorizontalStrut(5));
    panel.add(new JLabel("To"));
    panel.add(Box.createHorizontalStrut(5));
    panel.add(viewToComboBox);
    return panel;
}

private JPanel createPositionPanel() {
    JPanel posPanel = new JPanel();
    // create layout
    posPanel.setLayout( new BoxLayout(posPanel, BoxLayout.Y_AXIS) );
    //String[] list = {"r", "parallelScale", "delta", "phi"};
    
    JSlider slider[] = new JSlider[5]; // we have five sliders
    
    // ---- r ----
    BasicPropertyDescriptor pd = desc.getDescriptor("r");
    slider[0] = (JSlider)((ComponentPropertyEditor)(pd.getPropertyEditor())).getComponent();
    Component label = new JLabel(pd.getDisplayName());
    JTextField tf = createDoubleEditorComponent(pd);
    tf.setMaximumSize(new Dimension( 100, tf.getPreferredSize().height ));
    //label.setAlignmentX(CENTER_ALIGNMENT);
    
    JPanel rPanel = new JPanel();
        rPanel.setLayout( new BoxLayout(rPanel, BoxLayout.X_AXIS) );
        rPanel.add(label);
        rPanel.add(Box.createHorizontalGlue());
        rPanel.add(tf);
        rPanel.add(slider[0]);
    
    
    
    // ---- parallelScale ----
    pd = desc.getDescriptor("parallelScale");
    slider[1] = (JSlider)((ComponentPropertyEditor)(pd.getPropertyEditor())).getComponent();
    label = new JLabel(pd.getDisplayName());
    tf = createDoubleEditorComponent(pd);
    tf.setMaximumSize(new Dimension( 100, tf.getPreferredSize().height ));
    
    JPanel pscPanel = new JPanel();
        pscPanel.setLayout( new BoxLayout(pscPanel, BoxLayout.X_AXIS) );
        pscPanel.add(label);
        pscPanel.add(Box.createHorizontalGlue());
        pscPanel.add(tf);
        pscPanel.add(slider[1]);
    
    // create container for r and parallelScale panels
    // with Card Layout
    scalePanel = new JPanel( new CardLayout());
    scalePanel.add(pscPanel, SCALE_PANNEL[Camera.PARALLEL_PROJECTION]);
    scalePanel.add(rPanel, SCALE_PANNEL[Camera.PERSPECTIVE_PROJECTION]);    
    
    posPanel.add(scalePanel);
    
    // ---- delta ----
    pd = desc.getDescriptor("delta");
    slider[2] = (JSlider)((ComponentPropertyEditor)(pd.getPropertyEditor())).getComponent();
    label = new JLabel(pd.getDisplayName());
    tf = createDoubleEditorComponent(pd);
    tf.setMaximumSize(new Dimension( 100, tf.getPreferredSize().height ));
    
    JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS) );
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.add(tf);
        panel.add(slider[2]);    
    posPanel.add(panel);
    
    // ---- phi ----
    pd = desc.getDescriptor("phi");
    slider[3] = (JSlider)((ComponentPropertyEditor)(pd.getPropertyEditor())).getComponent();
    label = new JLabel(pd.getDisplayName());
    tf = createDoubleEditorComponent(pd);
    tf.setMaximumSize(new Dimension( 100, tf.getPreferredSize().height ));
    
    panel = new JPanel();
        panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS) );
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.add(tf);
        panel.add(slider[3]);    
    posPanel.add(panel);

    // ---- viewUpAngle ----
    pd = desc.getDescriptor("viewUpAngle");
    slider[4] = (JSlider)((ComponentPropertyEditor)(pd.getPropertyEditor())).getComponent();
    label = new JLabel(pd.getDisplayName());
    tf = createDoubleEditorComponent(pd);
    tf.setMaximumSize(new Dimension( 100, tf.getPreferredSize().height ));
    
    panel = new JPanel();
        panel.setLayout( new BoxLayout(panel, BoxLayout.X_AXIS) );
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.add(tf);
        panel.add(slider[4]);    
    posPanel.add(panel);

    // set each slider's preffered size to the biggest slider size
    int maxWidth = 0;
    for (int i=0; i<slider.length; i++) {
        int sliderWidth = slider[i].getPreferredSize().width;
        if (sliderWidth > maxWidth) maxWidth = sliderWidth;
    }
    for (int i=0; i<slider.length; i++) {
        slider[i].setMaximumSize(new Dimension(maxWidth, slider[i].getPreferredSize().height));
    }
    
    
    // ----- update scalePanel when Projectiom method is changed
    cam.addPropertyChangeListener("projection", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            updateScalePanel();
        }
    });
    
    updateScalePanel();
    
    return posPanel;
}

private void updateScalePanel() {
    CardLayout layout = (CardLayout)(scalePanel.getLayout());
    layout.show(scalePanel, SCALE_PANNEL[cam.getProjection()]);
}

private JTextField createDoubleEditorComponent(BasicPropertyDescriptor pd) {
    OVTObject obj = (OVTObject)pd.getBean();
    DoubleEditor editor = new DoubleEditor(pd, 2);
    editor.setEditCompleteOnKey(true);
    // Render each time user changes time by means of gui
    editor.addGUIPropertyEditorListener(new GUIPropertyEditorListener() {
        public void editingFinished(GUIPropertyEditorEvent evt) {
            cam.Render();
        }
    });
    obj.addPropertyChangeListener(pd.getName(), editor);
    obj.addPropertyChangeListener("position", editor);
    return (JTextField)editor.getComponent();
}

public void propertyChange(PropertyChangeEvent evt) {
    // listens to visibility change of a camera
    String propertyName = evt.getPropertyName();
    if (propertyName.equals("customizerVisible")) {
        
        boolean value = ((Boolean)evt.getNewValue()).booleanValue();
        setVisible(value);
        
    }
}

//public Point getLocation() { return super.getLocation(); }
//public void setLocation(Point p) { super.setLocation(p); }

}
