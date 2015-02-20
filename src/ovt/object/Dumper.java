/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/object/Dumper.java,v $
  Date:      $Date: 2006/02/20 16:06:39 $
  Version:   $Revision: 2.9 $


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

package ovt.object;

import ovt.*;
import ovt.gui.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
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

/*
 * Dumper.java
 *
 * Created on March 13, 2001, 6:27 PM
 */

public class Dumper {
    OrbitMonitorModule om;
    
    /** Holds types of data selected for dumping */
    int[] columns = {DumpRecord.TIME, DumpRecord.POS_X, DumpRecord.POS_Y, DumpRecord.POS_Z};
    /*public static final int[] items = { DumpRecord.TIME, DumpRecord.POS_X, DumpRecord.POS_Y, DumpRecord.POS_Z, 
                        DumpRecord.R, DumpRecord.VEL, 
                        DumpRecord.B, DumpRecord.B_X, DumpRecord.B_Y, DumpRecord.B_Z, 
                        DumpRecord.FP1_LAT, DumpRecord.FP1_LON, DumpRecord.FP2_LAT, DumpRecord.FP2_LON, DumpRecord.DIST_TO_MP, 
                        DumpRecord.SPIN_B,  DumpRecord.SPIN_V, DumpRecord.SPIN_S, DumpRecord.DIP_TILT}; */
    /** number of colums */
    public static final int COLS = DumpRecord.numOfFields;
    //JComboBox[] colsCB = new JComboBox[COLS];
    
    //String[] tags = new String[items.length + 1];
    
    DumperPanel panel;
    
    /** Holds value of property file. */
    private File file = new File(OVTCore.getUserdataDir()+"untitled.dat");
    
    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport =  new java.beans.PropertyChangeSupport (this);
    
    /** Indicates weather the header was writen to a file
     * if not - the file will be opened ">" for writing
     * and the header will be writen to it.
     */
    boolean headerWritten = false;
    
    
    public Dumper(OrbitMonitorModule oMonitor) {
      this.om = oMonitor;
      // init tags
      /*tags[0] = "None";
      for (int i=0; i<items.length; i++)
          tags[i+1] = DumpRecord.getName(items[i]);*/
      
      // if user changes distanceUnits in OrbitMonitor - data should be writen to
      // a new file
      om.addPropertyChangeListener("distanceUnit", new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) { headerWritten = false; }
      });
      om.addPropertyChangeListener("dateFormat", new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) { headerWritten = false; }
      });
      om.addPropertyChangeListener("hoursFormat", new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) { headerWritten = false; }
      });
      om.getCore().getCoordinateSystem().addCoordinateSystemChangeListener(new CoordinateSystemChangeListener() {
          public void coordinateSystemChanged(CoordinateSystemEvent evt) {
            headerWritten = false;
          }
      });
    }

    public String[] getTags() {
        return DumpRecord.tags;
    }
    
    public int[] getColumns() {
        return columns;
    }
    public void setColumns(int[] columns) {
        this.columns = columns;
        headerWritten = false;
    }
    
    /** Returns a string without commas */
    private static String removeCommas(String str) {
        // do not understand at all why this was here !?!?! 2006-02-10
	// Mykola	
        //StringTokenizer st = new StringTokenizer(str, ",");
        //String res = "";
        //while (st.hasMoreTokens()) res += st.nextToken();
        //return res;
	return str.replace(',','.');
    }

    private void writeHeader(PrintWriter writer) throws IOException {
        
        writer.write("# Data file generated by OVT for "+ om.getSat().getName() +"\n");
        writer.write("# XYZ coordinate system : "+CoordinateSystem.getCoordSystem(om.getCS())+"\n");
        writer.write("# Polar coordinate system : "+CoordinateSystem.getCoordSystem(om.getPolarCS())+"\n");
        writer.write("# Columns : \n");
        int[] columnDataTypes = getColumns();
        for (int i=0; i<columnDataTypes.length; i++) {
            String s="# "+(i+1)+". "+ DumpRecord.getName(columnDataTypes[i]);
            switch (columnDataTypes[i]) {
                case DumpRecord.TIME : s += " [ " + TimeFormat.getFormat(om.getDateFormat()) + " ]";
                                                    break;
                case DumpRecord.POS_X:
                case DumpRecord.POS_Y:
                case DumpRecord.POS_Z:
                case DumpRecord.R:
                case DumpRecord.DIST_TO_MP:
                    s+= " [ " + om.getDistanceUnit() +" ]";
                    break;
                case DumpRecord.B:
                case DumpRecord.B_X:
                case DumpRecord.B_Y:
                case DumpRecord.B_Z:
                    s+= " [ nT ]";
                    break;
                case DumpRecord.DIP_TILT:
                    s+= " [ RAD ]";
                    break;    
            }
            writer.write(s+" \n");
        }
        
    }
    
    /** log one row */
    private void log(PrintWriter writer, double mjd)  throws IOException {
        int[] records = getColumns();
        double[] values = om.getValues(records, mjd);
        String line = "", s="";
        for (int j=0; j<values.length; j++) {
		if (records[j] == DumpRecord.TIME) s = om.getTimeFormat().format(values[j]);
		else s = ""+values[j];
                if (j == values.length-1) line = line + s;
                else line = line + s + "\t"; 
        }
        writer.write(line + "\n");
        headerWritten = true;
    }
    
    /** Logs entry into a file */
    public void logEntry() throws IOException {
        PrintWriter writer = null;
        if (!headerWritten) {
            if (file.exists()) {
                int res = JOptionPane.showConfirmDialog(om.getCore().getXYZWin(),
                                "File alredy exists. Overwrite?",
                                "Overwrite ?",
                                JOptionPane.YES_NO_OPTION);
                if ( res == JOptionPane.NO_OPTION) return;
            }
            // rewriting file
            writer = new PrintWriter(new FileWriter(file.getAbsolutePath(), false));
            writeHeader(writer);
            
        } else 
            writer = new PrintWriter(new FileWriter(file.getAbsolutePath(), true));
            
        log(writer, om.getMjd());
        writer.close();
    }

    
    /** Logs all entries into a file */
    private void logAll() throws IOException {
        // if the file exist prompt if one can overwrite it.
        if (file.exists()) {
                int res = JOptionPane.showConfirmDialog(om.getCore().getXYZWin(),
                                "File alredy exists. Overwrite?",
                                "Overwrite ?",
                                JOptionPane.YES_NO_OPTION);
                if ( res == JOptionPane.NO_OPTION) return;
        }
        PrintWriter writer = new PrintWriter(new FileWriter(file.getAbsolutePath(), false));
        logAll(writer);    
        writer.close();
    }

    /** Logs all entries into a stream "out" */
    public void logAll(PrintWriter out) throws IOException {
        writeHeader(out);
        double[] timeMap = om.getTimeSet().getValues();
        for (int i=0; i<timeMap.length; i++) log(out, timeMap[i]);
    }
    
    
    public JPanel getPanel() {
        if (panel == null) panel = new DumperPanel();
        return panel;
    }

    /** Add a PropertyChangeListener to the listener list.
     * @param l The listener to add.
 */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
 */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }
    
    /** Getter for property file.
     * @return Value of property file.
 */
    public File getFile() {
        return file;
    }
    
    /** Setter for property file.
     * @param file New value of property file.
 */
    public void setFile(File file) {
        File oldFile = this.file;
        this.file = file;
        headerWritten = false;
        propertyChangeSupport.firePropertyChange ("file", oldFile, file);
    }
    
//------------------------- D U M P E R -  P A N E L ---------------------------------
    
    class DumperPanel extends JPanel {
        JTextField fileTF;
      /** Creates new DumperPanel */
      public DumperPanel() {
        super(new BorderLayout(5,5));
        JSeparator separator = new JSeparator();
        //separator.setBackground(Color.gray);
        
        add(separator, BorderLayout.NORTH);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout (1, 3, 5, 5));
        
        JButton button = new JButton("Log");
        button.setSelected(true);
        button.setToolTipText("Dump one data line to file");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                try {
                    logEntry();
                } catch (IOException e2) { om.getCore().sendErrorMessage(e2);}
            }
        });
        panel.add(button);
        
        button = new JButton("Log All");
        button.setToolTipText("Dump data for the whole orbit");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                try {
                    logAll();
                } catch (IOException e2) { om.getCore().sendErrorMessage(e2);}
            }
        });
        panel.add(button);
        
        button = new JButton("Settings");
        button.setToolTipText("Dumper setings");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                new DumperSettingsWindow(om.getCore().getXYZWin()).setVisible(true);
            }
        });
        panel.add(button);
        add(panel, BorderLayout.CENTER);
        
        panel = new JPanel();
        panel.setLayout(new GridLayout (1, 2, 5, 5));
        panel.setBorder(new TitledBorder("File"));
        
        fileTF = new JTextField(file.getName());
          fileTF.setEditable(false);
          panel.add(fileTF);
        
        button = new JButton("Browse...");
          button.setMaximumSize(button.getMinimumSize());
          button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent evt) {
                chooseFile();
            }
          });
          panel.add(button);
          
        add(panel, BorderLayout.SOUTH);
        //fileTF.setMaximumSize(fileTF.getSize());
    }
    
  private void chooseFile() {
        JFileChooser chooser = null;
    if ( file == null ) 
      chooser = new JFileChooser(OVTCore.getUserdataDir());
    else 
      chooser = new JFileChooser(file);
    
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
        if ( !tmpFile.canWrite() ) {
          JOptionPane.showMessageDialog(this,
                               "File is not writable.",
                               "File error",
                               JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
      setFile(tmpFile);
      fileTF.setMaximumSize(fileTF.getSize());
      //System.out.println("Set size: " + fileTF.getSize());
      fileTF.setPreferredSize(fileTF.getSize());
      fileTF.setText(file.toString());
    }
   }

 //----------------------   DumperSettingsWindow    ---------------------------
   
    class DumperSettingsWindow extends JDialog {
        
        JComboBox[] colsCB = new JComboBox[COLS];
        
        
        DumperSettingsWindow(JFrame owner) {
            super(owner, "Dumper Settings", true);
            // set the list of items in combobox
            
            

            Container cont = getContentPane();
            //create layout : new java.awt.GridLayout (4, 1, 5, 5)
            cont.setLayout(new BorderLayout());

            // columns panel
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout (COLS, 2, 5, 5));
            JLabel label;
            for (int i=0; i<COLS; i++) {
                label = new JLabel("Column "+(i+1)+" ");
                  label.setFont(Style.getTextFont());
                colsCB[i] = new JComboBox(getTags());
                  colsCB[i].setFont(Style.getTextFont());
                panel.add(label);
                panel.add(colsCB[i]);
            }
       
            cont.add(panel, BorderLayout.CENTER);
        
            panel = new JPanel();
        
            JButton button = new JButton("Close");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        dispose();
                    }
                });
            panel.add(button);
            
            button = new JButton("OK");
                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        int[] co = getColumns();
                        if (co != null) {
                            setColumns(co);
                            dispose();
                        } else { // no fields where selected
                            om.getCore().sendWarningMessage("Oops...", "No data selected");
                            refresh();
                        }
                    }
                });
            panel.add(button);
            
            cont.add(panel, BorderLayout.SOUTH);
        
            refresh();
            
            pack();
            setResizable(false);
        
            // senter the window
            Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension windowSize = getSize();
            setLocation(scrnSize.width/2 - windowSize.width/2,
                 scrnSize.height/2 - windowSize.height/2);
        }
        
        private void refresh() {
            for (int i=0; i<columns.length; i++)
                colsCB[i].setSelectedItem(DumpRecord.getName(columns[i]));
            for (int i=columns.length; i<colsCB.length; i++)
                colsCB[i].setSelectedIndex(0); // None
        }
    
    
        /** create int[] with types from combo boxes */
        private int[] getColumns() {
            int[] res = null;
            String selected;
            Vector items = new Vector();
            for (int i=0; i<COLS; i++) {
                selected = (String)(colsCB[i].getSelectedItem());
                if (selected.equals("None")) continue;
                items.addElement(new Integer(DumpRecord.getType(selected)));
            }
            if (items.size() == 0) return null;
            // make int[] from vector
            Enumeration e = items.elements();
            res = new int[items.size()];
            for (int i=0; i<res.length; i++) res[i] = ((Integer)e.nextElement()).intValue();
            return res;
        }
    

    }
    
    DumperSettingsWindow settingsWindow;
    

}    
    
}
