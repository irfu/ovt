/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/object/DataModule.java,v $
Date:      $Date: 2009/10/27 12:14:36 $
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

import vtk.*;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author  ko
 * @version 
 */
public class DataModule extends AbstractSatModule implements MenuItemsSource {
    private static final int TIME  = 0;
    private static final int VALUE = 1;
    
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    double[][] data = null;
    private int[] firstAndLastIndexes = { 0, 0 };
    double max = Double.NEGATIVE_INFINITY;
    double min = Double.POSITIVE_INFINITY;
    
    /** Holds value of property file. */
    private File file = null;
    private int numberOfColumns = 0;
    private TimeFormat timeFormat = new TimeFormat();
    private ovt.util.ExtendedTimeFormat extendedTimeFormat = new ExtendedTimeFormat();
    
    /** Timeset of Data */
    TimePeriod dataTimePeriod = new TimePeriod();
    DataModuleCustomizer customizer;
    
    private vtkLookupTable lookupTable;
    private DataScalarBar dataScalarBar;
    private OrbitDataModule orbitDataModule;
    private FieldlineDataModule fieldlineDataModule;
    
    public static final int TIME_FORMAT_NORMAL   = 0;
    public static final int TIME_FORMAT_EXTENDED = 1;
    
    /** Holds value of property timeFormatType. */
    private int timeFormatType = TIME_FORMAT_NORMAL;
    
    /** Creates new DataModule */
    public DataModule(Sat sat) {
        super(sat,  "Data", "images/data.gif");
        setParent(sat);
        // listen to property change from sat
        // when this object is removed - one has to remove
        // itself from the list of listeners !! NO NOT FORGET!
        sat.addPropertyChangeListener("enabled", this);
        
        sat.getCore().getTimeSettings().addTimeChangeListener(this);
        sat.getCore().getCoordinateSystem().addCoordinateSystemChangeListener(this);
        
        timeFormat.setParent(this); // let children know their roots ;-)
        extendedTimeFormat.setParent(this); 
        
        lookupTable = new vtkLookupTable();
            lookupTable.SetHueRange(0.6667, 0);
        
        dataScalarBar  = new DataScalarBar(this);
        addChild(dataScalarBar);
        
        try {
            descriptors = new Descriptors();
            
            BasicPropertyDescriptor pd = new BasicPropertyDescriptor("file", this);
            pd.setMenuAccessible(false);
            GUIPropertyEditor editor = new FileEditor(pd);
            addPropertyChangeListener("file", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);
            
            pd = new BasicPropertyDescriptor("timeFormatType", this);
            pd.setMenuAccessible(false);
            editor = new RadioButtonPropertyEditor(pd, 
                new int[]{ TIME_FORMAT_NORMAL, TIME_FORMAT_EXTENDED}, 
                new String[] { "Normal", "Extended" });
            addPropertyChangeListener("timeFormatType", editor);
            pd.setPropertyEditor(editor);
            descriptors.put(pd);
            
            descriptors.put(timeFormat.getDescriptors().getDescriptor("dateFormat"));
            
            descriptors.put(extendedTimeFormat.getDescriptors().getDescriptor("offsetMjd"));
            descriptors.put(extendedTimeFormat.getDescriptors().getDescriptor("unit"));
            
            setDescriptors(descriptors);
            
            if (!OVTCore.isServer()) {
                customizer = new DataModuleCustomizer(this);
                // can be (visible, customizer). think about it, MAN ;)
                pd = new BasicPropertyDescriptor("customizerVisible", this);
                pd.setMenuAccessible(false);
                
                editor = new VisibilityEditor(pd);
                //editor.setTags(new String[]{"Properties ...", "Properties ..."});
                //editor.setValues(new Object[]{new Boolean(true), new Boolean(true)});
                addPropertyChangeListener("customizerVisible", editor);
                pd.setPropertyEditor(editor);
                descriptors.put(pd);
            }
            
        } catch (IntrospectionException e2) {
            e2.printStackTrace(); System.exit(0);
        }
        
  orbitDataModule  = new OrbitDataModule(this);
  addChild(orbitDataModule);
   
  fieldlineDataModule  = new FieldlineDataModule(this);
  addChild(fieldlineDataModule);
        
}
    
public String getName() {  
    String f = "";
    if (file != null) f = file.getName();
    return "Data ("+f+")";
}

public double getMax() {
    return max;
}

public double getMin() {
    return min;
}

public TimePeriod getDataTimePeriod() {
    return dataTimePeriod;
}

public int getFirstIndex() {
    return firstAndLastIndexes[0];
}

public int getLastIndex() {
    return firstAndLastIndexes[1];
}

public int[] getFirstAndLastIndexes() {
    return firstAndLastIndexes;
}

private void updateFirstAndLastIndexes() {
    int[] ind = findFirstAndLastIndexes();
    firstAndLastIndexes[0] = ind[0];
    firstAndLastIndexes[1] = ind[1];
}

/** Returns the index of first data, whos mjd lays inside global timeSet */
private int[] findFirstAndLastIndexes() {
    int first = -1;
    double startMjd = getTimeSet().getStartMjd();
    double stopMjd = getTimeSet().getStopMjd();
    if (data == null || !getTimeSet().intersectsWith(dataTimePeriod)) {
        return new int[]{-1, -1};
    } else {
        int i;
        for (i=0; i<data.length; i++) { //bad idea. binomial poshuk - kruche
            double mjd = data[i][TIME];
            // set first index
            if (mjd >= startMjd  &&  first == -1) first = i;
            // return result when the last index is found
            if (mjd > stopMjd) { 
                //Log.log("mjd="+new Time(mjd)+" i="+i);
                return new int[]{ first, i-1 };
            }
        }
        //Log.log("Data length = "+data.length+" i="+i);
        return new int[]{ first, i-1}; // or i-1?
    }
}

/** This method should be implemmented. ovt.util.Settings should support multydimentional arrays */
public void setData(double[][] data) {
    this.data = data;
}

public double[][] getData() {
    try {
        if (data == null) loadData();
    } catch (IOException e2) {e2.printStackTrace(System.err); }
    return data;
}

public void loadData() throws IOException {
    if (file == null) throw new IOException("File is not specified");
    
    max = Double.NEGATIVE_INFINITY;
    min = Double.POSITIVE_INFINITY;
    
    BufferedReader inData = new BufferedReader(new FileReader(this.file));
    StringTokenizer st;
    String line;
    double val, mjd=0;
    int offset=0;
    DoubleAndInteger di;
    int lineNumber = 0;
    Timetable tt = new Timetable();
    try {
        while((line = inData.readLine()) != null) {
            lineNumber++;
            //System.out.println("["+lineNumber+"] "+line);
            if (line.startsWith("#")) continue; // skip comments
            
            try {
                if (timeFormatType == TIME_FORMAT_NORMAL) 
                        di = timeFormat.parseMjd(line);
                else  // timeFormatType == TIME_FORMAT_EXTENDED
                    di = extendedTimeFormat.parseMjd(line);
            } catch (NumberFormatException e2) {
                throw new IOException("Wrong time at line "+lineNumber + " ("+line+")\n"+file);
            }
            
            mjd = di.d;
            offset = di.i+1;
            
            int startIndex = StringUtils.doubleStartsAt(line, offset);
            int endIndex = StringUtils.doubleEndsAt(line, startIndex);
            //Log.log("->"+offset+" "+startIndex+" "+endIndex+" = '"+line.substring(startIndex, endIndex+1)+"'------");
            val = new Double(line.substring(startIndex, endIndex+1)).doubleValue();
            //Log.log("val = " + val + " max = " + max + " min = " + min);
            //Log.log(""+new Time(mjd)+" "+val); 
            tt.put(mjd, new Double(val));
            // set minimum and maximum values
            if (val > max) max = val;
            if (val < min) min = val;
        }
    } catch (NumberFormatException e2) {
        System.err.println("Invalid entrty in " + file + ", line #"+lineNumber+" : " + e2);
        e2.printStackTrace();
    }
    inData.close();
    System.out.println("min=" + min + " max="+ max +" done.");
    data = new double[tt.size()][2];
    Enumeration e = tt.keys();
    int i=0;
    while (e.hasMoreElements())
        data[i++][TIME] = ((Double)e.nextElement()).doubleValue();
    e = tt.elements();
    i=0;
    while (e.hasMoreElements())
        data[i++][VALUE] = ((Double)e.nextElement()).doubleValue();
    
    dataTimePeriod.setStartMjd(data[0][TIME]);
    dataTimePeriod.setStopMjd(data[data.length - 1][TIME]);
    
    //System.out.println("Data Time Set: " + dataTimeSet);
    
    boolean intersects = getTimeSet().intersectsWith(dataTimePeriod);
    if (isEnabled() != intersects) setEnabled(intersects);
    
    // update lookup table
    lookupTable.SetTableRange(min, max);
    lookupTable.Build();
    // update indexes
    updateFirstAndLastIndexes();
}

/** Getter for property file.
 * @return Value of property file.
 */
public File getFile() {
  return file;
}

/** Sets the DataFile and Loads the Data
 * @param file New value of property file.
 */
public void setFile(File file) throws IOException {
  File oldFile = this.file;
  if (!file.exists()) 
      throw new IOException("File '"+ file +"' does not exist");
  if (file.isDirectory()) 
      throw new IOException("File '"+ file +"' is a directory");
  if (!file.canRead())
      throw new IOException("File '"+ file +"' is not readable");
  
  int n = getNumberOfColumns(file);
  
  if (n < 2) throw new IOException("File '"+ file +"' has "+n+" column(s)");
  
  numberOfColumns = n;
  
  this.file = file;
  data = null;
  System.gc(); // run garbage collector
  propertyChangeSupport.firePropertyChange ("file", oldFile, file);
}

public TimeFormat getTimeFormat() {
    return timeFormat;
}

public int getNumberOfColumns() {
  return numberOfColumns;  
}

private static int getNumberOfColumns(File file) throws IOException {
    StringTokenizer st = new StringTokenizer(getFirstDataLine(file));
    return st.countTokens();
}

private static String getFirstDataLine(File file) throws IOException {
    int res = 0;
    BufferedReader inData = new BufferedReader(new FileReader(file));
    String line;
    boolean ok = false;
    // look for a first data line
    while((line = inData.readLine()) != null && !ok) {
        //System.out.println(line);
        if (line.startsWith("#")) continue; // skip coments
        // wrong and buggy place !
        // time could be 'yyyy/mm/nn hh:mm:ss' ....
        inData.close();
        return line;
    }
    throw new IOException("Line #1 was not found");
}


public int guessDateFormat() throws NumberFormatException, IOException {
    String line = getFirstDataLine(file);
    return TimeFormat.guessDateFormat(line);
}


private void showFileOpenDialog() {
    if (OVTCore.isGuiPresent()) {
        FileEditor editor = (FileEditor)getDescriptors().getDescriptor("file").getPropertyEditor();
        editor.setOpenDialogVisible(true);
    }
}

/** Getter for property customizerVisible.
 * @return Value of property customizerVisible.
 */
public boolean isCustomizerVisible() {
  if (!OVTCore.isServer()) return customizer.isVisible();
  else return false; 
}

/** Setter for property customizerVisible.
 * @param customizerVisible New value of property customizerVisible.
 */
public void setCustomizerVisible(boolean customizerVisible) {
  if (!OVTCore.isServer()) { 
        boolean oldCustomizerVisible = isCustomizerVisible();
        if (oldCustomizerVisible == customizerVisible) return; // nothing is changed
        customizer.setVisible(customizerVisible);
        firePropertyChange ("customizerVisible", new Boolean (oldCustomizerVisible), new Boolean (customizerVisible));
  }
}

/** Getter for property timeFormatType.
 * @return Value of property timeFormatType.
 */
public int getTimeFormatType() {
    return timeFormatType;
}

/** Setter for property timeFormatType.
 * @param timeFormatType New value of property timeFormatType.
 */
public void setTimeFormatType(int timeFormatType) {
    int oldTimeFormatType = this.timeFormatType;
    this.timeFormatType = timeFormatType;
    firePropertyChange ("timeFormatType", new Integer (oldTimeFormatType), new Integer (timeFormatType));
}

/** Tell children about time change */
public void timeChanged(TimeEvent evt) {
    if (evt.timeSetChanged()) {
        updateFirstAndLastIndexes();
        boolean enabled =  getTimeSet().intersectsWith(dataTimePeriod);
        if (isEnabled() != enabled) setEnabled(enabled);
    }
}

public JMenuItem[] getMenuItems() {
        final int ITEM_COUNT = 3;
        JMenuItem[] item = new JMenuItem[ITEM_COUNT];
        int i=-1;
        
        item[++i] = new JMenuItem("Remove");
        item[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {       
                sat.removeDataModule(DataModule.this);
                sat.getChildren().fireChildRemoved(DataModule.this);
            }
        });
        
        item[++i] = null; // Separator
        
        item[++i] = new JMenuItem("Properties...");
        item[i].addActionListener(new ActionListener() { 
            public void actionPerformed(ActionEvent e) { 
                setCustomizerVisible(true);
            } 
        });
        
        for (i=0; i<ITEM_COUNT; i++) {
            if (item[i] != null) item[i].setFont(Style.getMenuFont());
        }
        return item;
}

/** Method overriden to unregister this as a propertyChange listener and to dispose customizer */
public void dispose() {
    sat.removePropertyChangeListener("enabled",this);
    sat.getCore().getTimeSettings().removeTimeChangeListener(this);
    sat.getCore().getCoordinateSystem().removeCoordinateSystemChangeListener(this);
    setVisible(false);
    if (customizer != null) customizer.dispose();
    super.dispose();
}


public vtkLookupTable getLookupTable() { return lookupTable; }
/** for XML */
public DataScalarBar getDataScalarBar() { return dataScalarBar; }
/** for XML */
public OrbitDataModule getOrbitDataModule() { return orbitDataModule; }
/** for XML */
public FieldlineDataModule getFieldlineDataModule() { return fieldlineDataModule; }

}

