/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/SatInfoWindow.java,v $
Date:      $Date: 2003/09/28 17:52:41 $
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
 * SatInfoWindow.java
 *
 * Created on June 30, 2002, 8:15 PM
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
public class SatInfoWindow extends JDialog implements Customizer {
    
    private Descriptors desc;
    private JLabel fileL = new JLabel("File : .........");
    private JLabel timeL = new JLabel("Orbit data is available for 2000-01-01 00:00:00 - 2000-01-02 00:00:00");
    private JLabel revolPeriodL = new JLabel("Approx. revolution period:  2.3 days");
    
    public SatInfoWindow(JFrame owner) {
        super(owner, true); 
        
        JPanel cont = new JPanel();
        // create layout : new java.awt.GridLayout (4, 1, 5, 5)
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        cont.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10));
        
        
        JButton okButton = new JButton("  OK  ");
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //setVisible(false);
                dispose();
            }
        });
        okButton.setAlignmentX(0.5f);
        okButton.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        
        fileL.setFont(Style.getLabelFont());
        timeL.setFont(Style.getLabelFont());
        revolPeriodL.setFont(Style.getLabelFont());
        
        cont.add(Box.createRigidArea( new Dimension(0, 20)));
        cont.add(fileL);
        cont.add(Box.createRigidArea( new Dimension(0, 20)));
        cont.add(timeL);
        cont.add(Box.createRigidArea( new Dimension(0, 20)));
        cont.add(revolPeriodL);
        cont.add(Box.createRigidArea( new Dimension(0, 20)));
        cont.add(okButton);
        
        getRootPane().setDefaultButton(okButton);
        
        getContentPane().add(cont);
        
        pack();
        
        // senter the window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width/2 - windowSize.width/2,
            scrnSize.height/2 - windowSize.height/2);
    }
    
    public void setObject(Object sat) {
        Sat satellite = (Sat)sat;
        setTitle("Satellite : " + satellite.getName());
        fileL.setText("File : "+satellite.getOrbitFile().getAbsolutePath());
        //recordsL.setText("Data has "+module.getData().length+ " records");
        timeL.setText("Orbit data available for "+new Time(satellite.getFirstDataMjd())+" - " +
                              new Time(satellite.getLastDataMjd()));
        
        String period = "not available";
        double perDays = satellite.getRevolutionPeriod();
        if (perDays > 0) {
             Interval interv = new Interval(perDays);
             interv.setSeconds(0);
             if (interv.getDay() > 0) interv.setMinutes(0); // if the period is in days - no need to show minutes .-))
             period = interv.toString();
        }
        revolPeriodL.setText("Approx. revolution period :  "+period);
    }

    
}
