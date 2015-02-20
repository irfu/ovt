/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/gui/LoadDataWizard.java,v $
  Date:      $Date: 2006/02/20 16:06:39 $
  Version:   $Revision: 1.9 $


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
 * LoadDataWizard.java
 *
 * Created on July 12, 2001, 5:46 AM
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
 * @author  root
 * @version 
 */
public class LoadDataWizard extends JDialog {

    public static final String LAST_DATA_FILE = "LastDataFile";
    
    private JPanel buttonsPanel = null;
    private JButton backButton, forwardButton, cancelButton;
    private IntHashtable pages = new IntHashtable();
    private WizardPage currentPage = null;
    private Sat sat;
    private boolean finished = false;
    private DataModule data = null;
    private FilenamePage filenamePage ;
    private TimeFormatPage timeFormatPage;
    private LastPage lastPage;
    private JLabel label;
    private JPanel pageContainer;
    
    
    /** Creates new LoadDataWizard */
    public LoadDataWizard(Sat sat, Frame owner) {
        super(owner, "Load Data Wizard", true);
        this.sat = sat;
        data = new DataModule(sat);
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        
        filenamePage = new FilenamePage();
        timeFormatPage = new TimeFormatPage(filenamePage);
        lastPage = new LastPage();
        
        // set Layout
        getContentPane().setLayout(new BorderLayout(0,0));
        
        label = new JLabel();
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            label.setAlignmentX(CENTER_ALIGNMENT);
            
        JLabel imageLabel ;
        try {
            imageLabel = new JLabel(new ImageIcon(Utils.findResource("images/data_wizard.gif")));
            
        } catch (FileNotFoundException e2) { 
            e2.printStackTrace(System.err); 
            imageLabel = new JLabel();
        }
        
        pageContainer = new JPanel();
            pageContainer.setMinimumSize( new Dimension(400, imageLabel.getHeight()));
            pageContainer.setPreferredSize( new Dimension(400, imageLabel.getHeight()));
            //imageLabel.setBorder(BorderFactory.createRaisedBevelBorder());
        getContentPane().add(label, BorderLayout.NORTH);
        getContentPane().add(imageLabel, BorderLayout.WEST);
        getContentPane().add(pageContainer, BorderLayout.CENTER);
        getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
        
        
        setPage(filenamePage);
        pack();
        
        // center the window
        Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = getSize();
        setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
    }

    public DataModule start() {
        setVisible(true);
        return data;
    }
    
    private void setPage(WizardPage page) {
        if (currentPage != null) pageContainer.remove(currentPage);
        currentPage = page;
        
        if (page.isFirstPage()) backButton.setEnabled(false);
        else backButton.setEnabled(true);
        if (page.isLastPage()) {
            forwardButton.setText(" Finish ");
            buttonsPanel.remove(backButton);
            backButton.setEnabled(false);
        } //else forwardButton.setText(" Next > ");
        label.setText(page.getTitle());
        
        pageContainer.add(page, BorderLayout.CENTER);
        
        //page.invalidate();
        getContentPane().repaint();
        pack();
    }
    
    private JPanel getButtonsPanel() {
        if (buttonsPanel == null) {
            buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
            buttonsPanel.setBorder( BorderFactory.createEmptyBorder(0, 10, 10, 10));
            
            backButton = new JButton(" < Back ");
            backButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    WizardPage prevPage = currentPage.getPreviousPage();
                    setPage(prevPage);
                }
            });
            
            forwardButton = new JButton(" Next > ");
            getRootPane().setDefaultButton(forwardButton);
            forwardButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    WizardPage nextPage = currentPage.nextButtonPressed();
                    if (currentPage.isLastPage()) setVisible(false);
                    if (nextPage != null) setPage(nextPage);
                }
            });
            
            cancelButton = new JButton(" Cancel ");
            cancelButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    data = null;
                    setVisible(false);
                }
            });
            
            buttonsPanel.add(Box.createHorizontalGlue());
            buttonsPanel.add(backButton);
            buttonsPanel.add(Box.createRigidArea( new Dimension(10, 0)));
            buttonsPanel.add(forwardButton);
            buttonsPanel.add(Box.createRigidArea( new Dimension(10, 0)));
            buttonsPanel.add(cancelButton);
            
        }
        return buttonsPanel;
    }
    
// ------------------ FilenamePage --------------
    
    class FilenamePage extends WizardPage {
        JTextField fileTF;
                
        public FilenamePage() {
            super("Set file name");
            isFirstPage(true);
            setLayout(new FlowLayout ());
            setBorder(new TitledBorder("File"));
            
            fileTF = new JTextField("", 15);
            String startFile = OVTCore.getGlobalSetting(LAST_DATA_FILE);
            fileTF.setText(startFile);
            add(fileTF);
            
            JButton button = new JButton("Browse...");
            button.setMaximumSize(button.getMinimumSize());
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent evt) {
                    chooseFile();
                }
            });
            add(button);
            
        }

        private File getFile() {
            return new File(fileTF.getText());
        }
        
        private void setFile(File file) {
            fileTF.setText(file.getAbsolutePath());
        }
        
        private void chooseFile() {
            
            //File file = getFile();
            String startDir = OVTCore.getGlobalSetting(LAST_DATA_FILE);
            JFileChooser chooser = new JFileChooser(startDir);
            
            int returnVal = chooser.showDialog(this, "OK");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File tmpFile = chooser.getSelectedFile();
                if ( tmpFile.exists() ) {
                    if ( tmpFile.isDirectory() ) {
                        JOptionPane.showMessageDialog(this,
                        "File is a directory.",
                        "File error",
                        JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if ( !tmpFile.canRead() ) {
                        JOptionPane.showMessageDialog(this,
                        "File is not readable.",
                        "File error",
                        JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                setFile(tmpFile);
                fileTF.setMaximumSize(fileTF.getSize());
                //System.out.println("Set size: " + fileTF.getSize());
                fileTF.setPreferredSize(fileTF.getSize());
                // fileTF.setText(file.toString()); - in setFIle
            }
        }
        
        
        public WizardPage getNextPage() {
            return timeFormatPage;
        }
        
        public WizardPage nextButtonPressed() {
            try {
                data.setFile(getFile());
                OVTCore.setGlobalSetting(LAST_DATA_FILE, getFile().getAbsolutePath());
                data.getTimeFormat().setDateFormat(data.guessDateFormat());
                data.setTimeFormatType(data.TIME_FORMAT_NORMAL);
                return getNextPage();
            } catch (IOException e2) {
                sat.getCore().sendErrorMessage("Error", e2);
                return null;
            } catch (NumberFormatException e2) {
                sat.getCore().sendWarningMessage("Time format is not identified", e2);
                data.setTimeFormatType(data.TIME_FORMAT_EXTENDED);
                return getNextPage();
            }
        }
    
    }

// ------------------ TimeFormatPage --------------
    
    class TimeFormatPage extends WizardPage {
        JRadioButton useNormalRB = new JRadioButton("Normal");
        JRadioButton useExtendedRB = new JRadioButton("Extended");
        JComboBox dateCB, hoursCB, timeUnitsCB;
        JTextField startTimeTF;
                
        public TimeFormatPage(WizardPage prevPage) {
            super("Time format", prevPage);
            setLayout(new GridLayout (4, 1, 5, 5));
            setBorder(new TitledBorder("Time format"));
            
            Descriptors descriptors = data.getDescriptors();
            dateCB = (JComboBox)((ComponentPropertyEditor)descriptors.getDescriptor("dateFormat").getPropertyEditor()).getComponent();
            hoursCB = (JComboBox)((ComponentPropertyEditor)data.getTimeFormat().getDescriptors().getDescriptor("hoursFormat").getPropertyEditor()).getComponent();
            
            timeUnitsCB = (JComboBox)((ComponentPropertyEditor)descriptors.getDescriptor("unit").getPropertyEditor()).getComponent();
            startTimeTF = (JTextField)((ComponentPropertyEditor)descriptors.getDescriptor("offsetMjd").getPropertyEditor()).getComponent();
            
            JRadioButton[] rbtns = ((RadioButtonPropertyEditor)descriptors.getDescriptor("timeFormatType").getPropertyEditor()).getButtons();
            useNormalRB = rbtns[0];
            useExtendedRB = rbtns[1];
            
            data.addPropertyChangeListener( "timeFormatType",  new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (data.getTimeFormatType() == DataModule.TIME_FORMAT_NORMAL) {
                        dateCB.setEnabled(true);
                        hoursCB.setEnabled(true);
                        timeUnitsCB.setEnabled(false);
                        startTimeTF.setEnabled(false);
                    } else if (data.getTimeFormatType() == DataModule.TIME_FORMAT_EXTENDED) {
                        dateCB.setEnabled(false);
                        hoursCB.setEnabled(false);
                        timeUnitsCB.setEnabled(true);
                        startTimeTF.setEnabled(true);
                    }
                }
            });
            
            //YOU GONNA DIG THIS!!!!!!!!!
            
            JPanel normalPanel = new JPanel();
            normalPanel.setLayout(new FlowLayout());
            normalPanel.add(dateCB);
            normalPanel.add(hoursCB);
            
            JPanel extendedPanel = new JPanel();
            extendedPanel.setLayout(new FlowLayout());
            extendedPanel.add(new JLabel("Offset:"));
            extendedPanel.add(startTimeTF);
            extendedPanel.add(new JLabel("Unit:"));
            extendedPanel.add(timeUnitsCB);
            
            add(useNormalRB);
            add(normalPanel);
            add(useExtendedRB);
            add(extendedPanel);
        }
        
        public WizardPage getNextPage() {
            return lastPage;
        }
        
        public WizardPage nextButtonPressed() {
            try {
                data.loadData();
                lastPage.refresh();
                return getNextPage();
            } catch (IOException e2) {
                sat.getCore().sendErrorMessage("Error", e2);
                return null;
            } 
            
        }
    }

        
// ------------------ LastPage --------------
    
    class LastPage extends WizardPage {
        private JLabel loaded = new JLabel("Loaded 0 records");
        private JLabel interval = new JLabel("start -- stop");
        private JCheckBox changeGlobalTime = new JCheckBox("Set global time period to data time period");
                
        public LastPage() {
            super("Done!");
            isLastPage(true);
            setLayout(new BoxLayout (LastPage.this, BoxLayout.Y_AXIS));
            setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            add(Box.createRigidArea(new Dimension(0,15)));
            add(loaded);
            add(Box.createRigidArea(new Dimension(0,15)));
            add(interval);
            add(Box.createRigidArea(new Dimension(0,15)));
            add(changeGlobalTime);
            
        }
        
        public void refresh() {
            loaded.setText("Loaded "+data.getData().length+" records");
            interval.setText(""+new Time(data.getDataTimePeriod().getStartMjd())+" - " +
                              new Time(data.getDataTimePeriod().getStopMjd()));
        }
        
        public WizardPage nextButtonPressed() {
            setVisible(false);
            if (changeGlobalTime.isSelected()) {
                TimeSettings timeSettings = sat.getCore().getTimeSettings();
                double startMjd = data.getDataTimePeriod().getStartMjd();
                double intervalMjd = data.getDataTimePeriod().getStopMjd() - startMjd;
                TimeSet timeSet = new TimeSet(startMjd, intervalMjd, intervalMjd/30.);
                timeSet.setCurrentMjd(timeSet.getClosestFor(sat.getMjd()));
                timeSettings.setTimeSet(timeSet);
                //timeSettings.fireTimeSetChange();
            }
            data.setEnabled(sat.getTimeSet().intersectsWith(data.getDataTimePeriod()));
            return null;
        }
    
    }

    
}



