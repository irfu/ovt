/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/test.java,v $
 Date:      $Date: 2009/10/27 11:56:36 $
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
 $Id: test.java,v 2.4 2009/10/27 11:56:36 yuri Exp $
 $Source: /stor/devel/ovt2g/ovt/test.java,v $  
 */
package ovt;

import vtk.vtkActor;
import vtk.vtkBoxRepresentation;
import vtk.vtkBoxWidget2;
import vtk.vtkCell;
import vtk.vtkCellPicker;
import vtk.vtkConeSource;
import vtk.vtkLookupTable;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;
import vtk.vtkScalarBarRepresentation;
import vtk.vtkScalarBarWidget;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.rendering.vtkAbstractEventInterceptor;
import vtk.rendering.vtkEventInterceptor;
import vtk.rendering.jogl.vtkAbstractJoglComponent;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

public class test {

    static {
        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }
        }
        vtkNativeLibrary.DisableOutputWindow(null);
    }

    public static void main(String[] args) {
        final boolean usePanel = false;
        final boolean windowResizable = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        // build VTK Pipeline
                
                String file="images/ovt.gif";
                java.net.URL url = OVTCore.class.getClassLoader().getSystemResource(file);
        if (url == null) System.out.println("File not found ("+file+")");
        
                // create sphere geometry
                vtkSphereSource sphere = new vtkSphereSource();
                sphere.SetRadius(1.0);
                sphere.SetThetaResolution(18);
                sphere.SetPhiResolution(18);

                // map to graphics library
                vtkPolyDataMapper map = new vtkPolyDataMapper();
                map.SetInputConnection(sphere.GetOutputPort());
                //map.SetInputData(sphere.GetOutput());

                // actor coordinates geometry, properties, transformation
                vtkActor aSphere = new vtkActor();
                aSphere.SetMapper(map);
                aSphere.GetProperty().SetColor(0, 0, 1); // sphere color blue

                // VTK rendering part
                final vtkAbstractJoglComponent<?> joglWidget = usePanel ? new vtkJoglPanelComponent() : new vtkJoglCanvasComponent();
                System.out.println("We are using " + joglWidget.getComponent().getClass().getName() + " for the rendering.");

                joglWidget.getRenderer().AddActor(aSphere);
                // Add orientation axes
                vtkAbstractJoglComponent.attachOrientationAxes(joglWidget);

                // UI part
                JFrame frame = new JFrame("SimpleVTK");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().setLayout(new BorderLayout());

                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, windowResizable);
                splitPane.setLeftComponent(new JScrollPane());
                splitPane.setRightComponent(joglWidget.getComponent());
                splitPane.setOneTouchExpandable(windowResizable);
                splitPane.setDividerSize(6);
                frame.getContentPane().add(splitPane);
                //frame.getContentPane().add(joglWidget.getComponent(),
                //        BorderLayout.CENTER);
                frame.setSize(400, 400);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                joglWidget.resetCamera();
                joglWidget.getComponent().requestFocus();

                // Add r:ResetCamera and q:Quit key binding
                joglWidget.getComponent().addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (e.getKeyChar() == 'r') {
                            joglWidget.resetCamera();
                        } else if (e.getKeyChar() == 'q') {
                            System.exit(0);
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                    }
                });
            }
        });
    }
}
