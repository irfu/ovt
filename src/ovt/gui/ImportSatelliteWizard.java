/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/gui/ImportSatelliteWizard.java,v $
Date:      $Date: 2003/09/28 17:52:41 $
Version:   $Revision: 2.5 $


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
 * ImportSatelliteWizard.java
 *
 * Created on June 29, 2002, 6:17 PM
 */

package ovt.gui;

import ovt.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;
import ovt.model.magnetopause.*;

import java.beans.*;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 *
 * @author  ko
 * @version 
 */
public class ImportSatelliteWizard extends JDialog {
    
    private final static String RECENT_ORBIT_FILE = "ImportSatelliteWizard.RecentOrbitFile";
    private final static int TLE      = 1;
    private final static int LTOF   = 2;
    
    private Sats sats;
    private Sat sat = null;
    
    FilePanel filePanel = new FilePanel("Select Orbit File", true);
    JRadioButton importButton, addButton;
    JButton okButton = new JButton();
    JButton cancelButton = new JButton("Cancel");
    JTextField satNameTF = new JTextField("", 10);
    

    /** Creates new ImportSatelliteWizard */
    public ImportSatelliteWizard(Sats saats, JFrame owner) {
        super(owner, "Import Satellite", true);
        this.sats = saats;
        
        okButton.setText("Import");
        getRootPane().setDefaultButton(okButton);
        filePanel.setFile(new File(OVTCore.getGlobalSetting(RECENT_ORBIT_FILE, OVTCore.getUserDir())));
        filePanel.setAcceptAllFileFilterUsed(false);
        OvtExtensionFileFilter filter = new OvtExtensionFileFilter("Long Term Orbit Files (*.ltof)");
        filter.addExtension(".ltof");
        filePanel.addExtensionFilter(filter);
        filter = new OvtExtensionFileFilter("Two Line Element Files (*.tle)");
        filter.addExtension(".tle");
        filePanel.addExtensionFilter(filter);
        
        //sat = new Sat(sats.getCore());
        
        // build GUI
        JPanel cont = new JPanel();
        setContentPane(cont);
        cont.setBorder( BorderFactory.createEmptyBorder(25, 15, 20, 15) );
        cont.setLayout ( new BoxLayout(cont, BoxLayout.Y_AXIS) );
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        filePanel.addPropertyChangeListener("file", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                //if ("".equals(satNameTF.getText())) {
                    // guess the name form file..
                    String fname = filePanel.getFile().getName();
                    int index = fname.lastIndexOf('.');
                    if (index != -1) satNameTF.setText(Utils.replaceUnderlines(fname.substring(0,index)));
                //}
                OVTCore.setGlobalSetting(RECENT_ORBIT_FILE, filePanel.getFile().getAbsolutePath());
            }
        
        });
        
        
        JLabel fileLabel = new JLabel("Orbit file:");
        fileLabel.setFont(Style.getLabelFont());
        
        JLabel satNameLabel = new JLabel("Satellte name:");
        satNameLabel.setFont(Style.getLabelFont());
        // adjust fileLabel size
        fileLabel.setMaximumSize(satNameLabel.getPreferredSize());
        
        
        panel.add(fileLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(filePanel);
        
        cont.add(panel);
        cont.add(Box.createVerticalStrut(10));
        
        // Sat Name Panel
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        satNameTF.setPreferredSize(filePanel.getTextField().getPreferredSize());
        // guess the name form file..
        String fname = filePanel.getFile().getName();
        int index = fname.lastIndexOf('.');
        if (index != -1) satNameTF.setText(fname.substring(0,index));
        
        panel.add(satNameLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(satNameTF);
        panel.add(Box.createHorizontalGlue());
        
        cont.add(panel);
        cont.add(Box.createVerticalStrut(20));
        
        
        // Cancel / OK Buttons Panel
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        panel.add( Box.createHorizontalGlue() );    
        
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                // set
                try {
                    String satName = satNameTF.getText();
                    File file = filePanel.getFile();
                    OVTCore.setGlobalSetting(RECENT_ORBIT_FILE, file.getAbsolutePath());
                    
                    // check the sat name
                    if ("".equals(satName)) 
                        throw new Exception("Please specify satellite name");
                    
                    
                    int dataType = getOrbitDataType(file.getName());
                    
                    String ext="";
                    if (dataType == TLE) ext = ".tle";
                    else if (dataType ==  LTOF) ext = ".ltof";
                    else throw new Exception("Orbit file should end with .tle or .ltof");
                    
                    
                    File outfile = new File(OVTCore.getOrbitDataDir()+Utils.replaceSpaces(satName)+ext);
                    if (outfile.exists()) {
                        int res = JOptionPane.showConfirmDialog(sats.getCore().getXYZWin(),
                                "Satellite alredy exists. Replace?",
                                "Replace?",
                                JOptionPane.YES_NO_OPTION);
                        if ( res == JOptionPane.NO_OPTION) return;
                    }
                    
                    if (dataType == TLE)   {                  
                        TLESorter.sort(file, outfile); // check the tle file and copy it to $USER_HOME/odata/
                        sat = new TLESat(sats.getCore());
                        
                    } else { // dataType == LTOF
                        Utils.copyFile(file, outfile, true, true);
                        sat = new LTOFSat(sats.getCore());
                    }
                    sat.setName(satName);
                    sat.setOrbitFile(outfile); // also checks if the file is valid
                    
                    
                    sats.getCore().sendMessage("Success!", satName+" was successfuly validated and imported to \n"+
                                            OVTCore.getOrbitDataDir());
                    
                } catch (IOException e2) {
                    sats.getCore().sendErrorMessage("Error", e2.getMessage());
                    return;
                } catch (Exception e3) {
                    sats.getCore().sendErrorMessage("Error", e3.getMessage());
                    return;
                }
                
                setVisible(false);
            }
        });
            
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                sat = null;
                setVisible(false);
            }
        });
        
        /*Dimension size = okButton.getPreferredSize();
        //cancelButton.setPreferredSize(size);
        okButton.setText("Add");
        okButton.setPreferredSize(size);*/
        
        panel.add(okButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(cancelButton);
        
        cont.add(panel);
        
        pack();
        
        // center the window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
    }
    
    public Sat start() {
        setVisible(true);
        return sat;
    }
    
    /** Derives the file type on the basis of the filename (*.tle, *.ltof)
     * @returns  TLE or LTOF or -1
     */
    private static int getOrbitDataType(String filename) {
        if (filename.toLowerCase().endsWith(".tle")) return TLE;
        else if (filename.toLowerCase().endsWith(".ltof")) return LTOF;
        else return -1; // could not determine satellite type
    }
    
    
    
}
