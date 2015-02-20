/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/mag/MagActivityDataModel.java,v $
  Date:      $Date: 2003/09/28 17:52:43 $
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
 * MagActivityDataModel.java
 *
 * Created on den 25 mars 2000, 00:30
 */

package ovt.mag;

import ovt.beans.*;
import ovt.datatype.*;
import ovt.OVTCore;

import java.io.*;
import java.util.*;

/** Class for handling magnetic activity data stored in the disk files.
 *Files should exist in directory <I>mdata/</I> on the disk and have
 *header with column names. First column should be always <B>Time</B>.
 *All extra and unsuficcient data will be ignored and overwritten
 *after <CODE>save()</CODE>
 * @author Yuri Khotyaintsev
 * @version 1.0
 */
public class MagActivityDataModel extends javax.swing.table.AbstractTableModel {

  private String name = null;
  private Vector data = new Vector();
  private MagActivityDataRecord defaultValues;
  private double minValue;
  private double maxValue;
  private String[] columnNames = null;
  private int columnNumber = 0;
  private int rowNumber = 0;
  protected double lastMjd = -1;
  private MagActivityDataRecord lastValues = null;
  private File file = null;
  /** WHO NEEDS THIS PROPERTY?????? */
  private int index;


  /** Utility field used by bound properties. */
  private OVTPropertyChangeSupport propertyChangeSupport =  new OVTPropertyChangeSupport (this);
  
  
  public MagActivityDataModel(int index, double minValue, double maxValue, double defaultValue, String columnName) {
      init(index, minValue, maxValue, new double[]{ defaultValue }, new String[]{ columnName } );
  }
  
  /** Creates new MagActivityDataModel
   * @param param Name of activity parameter.
   * @throws Exception for parsing problems
   * @throws FileNotFoundException for file lookup
   * @throws IOException for IO problems
   */
  public MagActivityDataModel(int index, double minValue, double maxValue, double[] defaultValues, String[] columnNames) {
      init(index, minValue, maxValue, defaultValues, columnNames);
  }
  
  /** default values should not include time. Default time is Y200 */
 private void init(int index, double minValue, double maxValue, double[] defaultValues, String[] columnNames) {
    this.index = index;
    this.name = MagProps.getActivityName(index);
    this.minValue = minValue;
    this.maxValue = maxValue;
    // set column names
    this.columnNames = new String[columnNames.length + 1];
    this.columnNames[0] = "Time";
    for (int i=0; i<columnNames.length; i++) this.columnNames[i+1] = columnNames[i];
    this.columnNumber = this.columnNames.length; 
    // set default values
    this.defaultValues = new MagActivityDataRecord(Time.Y2000, defaultValues);
    this.file = new File(OVTCore.getGlobalSetting(name+".File", OVTCore.getUserdataDir()+name+".dat"));
    
    try {
        //file = new File(ovt.OVTCore.getUserdataDir(), param + magDataExt);
        load();
    } catch (IOException e2) {  }
    if (data.size() == 0) data.addElement(getDefaultValues().clone());
    //System.out.println(getName() + " size = " + data.size() );
    rowNumber = data.size();
  }

  protected void load() throws IOException {
    Vector newData = new Vector();
    RandomAccessFile fileIn = new RandomAccessFile(file,"r");
    long length = fileIn.length();
    
    //if ( length <= 0 ) throw new Exception("MagActivityDataModel: activity file is empty");
    
    /* Read data
    first entry is time in format 1994-04-04 12:00:00
    to be understood by ovt.datatype.Time.
    Incomplete lines or extra data will be disregarded */
    
    int rowCount = 0;
    int fileCount = 0;
    String s;
    while(fileIn.getFilePointer() < length) {
      boolean parsed = true;
      fileCount++;
      s = fileIn.readLine();
      
      if (s.startsWith("#")) continue; // skip comments
      
      StringTokenizer tok = new StringTokenizer(s, "\t");
      try {
          if (tok.countTokens() < getColumnCount()) throw new NumberFormatException();
          Time time = new ovt.datatype.Time(tok.nextToken());
          double[] dataRead = new double[getColumnCount() - 1];
          for (int i=0; i<dataRead.length; i++) {
              dataRead[i] = new Double(tok.nextToken()).doubleValue();
              if (!isValid(dataRead[i])) throw new NumberFormatException();
          }
          newData.addElement(new MagActivityDataRecord(time, dataRead));
       } catch (NumberFormatException e2) {
              System.out.println("parse error in line #" + fileCount + " file "
              + file.getAbsolutePath());
       }
    }
    fileIn.close();
    if (newData.size() == 0) throw new IOException("File is empty");
    data = newData;
    rowNumber = data.size();
    
    sortData();
    fireTableDataChanged();
    //fireTableChanged(new TableModelEvent(
  }
  
  /** Removes all elements from data and adds Defaultvalues record. */
  public void reset() {
    data.removeAllElements();
    data.addElement(getDefaultValues().clone());
    rowNumber = data.size();
    lastValues = null;
    fireTableDataChanged();
  }
  
  /**
   * @return number of rows (used by XML & JTable)
   */
  public int getRowCount() {
    return rowNumber;
  }

  /**
   * set number of rows (used by XML)
   */
  public void setRowCount(int numberOfRows) {
    rowNumber = numberOfRows;
    data.setSize(numberOfRows);
  }

    /** Is used by XML to get data record */
  public MagActivityDataRecord getRecordAt(int row) {
        return (MagActivityDataRecord)data.elementAt(row);
  }
  
  /** Is used by XML to set data record */
  public void setRecordAt( int row, MagActivityDataRecord MagActivityDataRecord) {
        data.setElementAt(MagActivityDataRecord, row);
  }

  
  public int getColumnCount(){
    return columnNumber;
  }

  public Object getValueAt(int row, int col){
    if( row<0 || row>=rowNumber || col<0 || col>=columnNumber)
            throw new IllegalArgumentException("Index out of bounds");
    else 
            return getRecordAt(row).get(col);
  }

  
  public MagActivityDataRecord getDefaultValues() {
    return defaultValues;
  }
  
  protected int getRow(double mjd) {
    int rowCount = getRowCount();
    if ( rowCount == 0 ) throw new IllegalArgumentException();
    else if ( rowCount == 1 ) return 0; // only one data line present
    else {
        if (mjd <= getMjd(0)) return 0; // request before first data - return first data
        if (mjd >= getMjd(rowCount - 1)) return rowCount - 1; //request after last data
        int i=0;
        for (i=0; i<rowCount - 1; i++) 
            if (getMjd(i+1) > mjd) return i;
        return rowCount -1;
    }
  }
  
  public Object getValueAt(double mjd, int element) {
      if (mjd == lastMjd && lastValues != null) return lastValues.get(element);
      try {
          lastValues  = getRecordAt(getRow(mjd));
      } catch (IllegalArgumentException e2) {
          lastValues = getDefaultValues();
      }
      lastMjd = mjd;
      return lastValues.get(element);
  }
  
  public double[] getValues(double mjd) {
      if (mjd == lastMjd && lastValues != null) return lastValues.values;
      try {
          lastValues  = getRecordAt(getRow(mjd));
      } catch (IllegalArgumentException e2) {
          lastValues = getDefaultValues();
      }
      lastMjd = mjd;
      return lastValues.values;
  }

  public double getMjd(int row) {
    return getRecordAt(row).time.getMjd();
  }

  
  
  public double getLastMjd() {
    return lastMjd;
  }

  public boolean isCellEditable(int row, int col){
    return true;
  }

  public void setValueAt(Object value, int row, int col) {
    if (col == 0) {
      if (ovt.datatype.Time.isValid((String)value)){
        MagActivityDataRecord rec = getRecordAt(row);
        rec.time = new Time((String)value);
        double mjd = rec.time.getMjd();
      
      if ( row < rowNumber-1 )
      if ( mjd > getMjd(row+1) ){
        int i = row+1;
        while ( mjd > getMjd(i) ) {
          flipRows(i,i-1);
          fireTableRowsUpdated(i-1,i);
          i++;
          if (i == rowNumber) break;
        }
      } else if ( row > 0 )
      if ( mjd < getMjd(row-1) ){
        int i = row-1;
        while ( mjd < getMjd(i) && i >= 0) {
          flipRows(i,i+1);
          fireTableRowsUpdated(i,i+1);
          i--;
          if ( i < 0 ) break;
        }
      }
    }
    } else {
        MagActivityDataRecord rec = getRecordAt(row);
        try {
            double dat = new Double((String)value).doubleValue();
        if ( isValid(dat) ) rec.values[col-1] = dat;
        else System.out.println("Specified value is out of range");
      } catch (NumberFormatException e) {
        System.out.println("Invalid number format");
      }
    }
    lastValues = null; //do we need lastmjd = -1 ?
  }

  public String getName(){
    return name;
  }

  public String getColumnName(int col) {
    if (col<0 || col>=columnNumber) return null;
    else return columnNames[col];
  }

  /** Saves <CODE>data</CODE> to file. Old file should be moved to .bak
   * @throws FileNotFoundException {@link java.io.FileNotFoundException}
   * @throws IOException {@link java.io.IOException}
   */
  public void save() throws IOException {
    //System.out.println("Saving ...");
    PrintWriter fileOut = new PrintWriter( new FileOutputStream(file.getAbsolutePath(), false));
    // create header
    String line = "# Time";
    for (int i=1; i<columnNumber; i++) {
      line = line + "\t"+ columnNames[i];
    }
    fileOut.println(line);
    // write data
    for (int i=0; i<rowNumber; i++) {
      line = "";
      for (int j=0; j<columnNumber; j++) {
        line += getValueAt(i,j) + "\t";
      }
      fileOut.println(line);
    }
    fileOut.close();
  }

  public void insertRows(int row){
    MagActivityDataRecord rec = (MagActivityDataRecord)getRecordAt(row).clone();
    data.insertElementAt(rec, row+1);
    rowNumber = data.size();
    fireTableRowsInserted(row, row + 1);
  }

  public void deleteRows(int firstRow, int lastRow){
    for(int i=lastRow;i>=firstRow;i--){
      data.removeElementAt(i);
    }
    rowNumber = data.size();
    fireTableRowsDeleted(firstRow, lastRow);
  }


  protected void flipRows(int a, int b){
    MagActivityDataRecord reca = getRecordAt(a);
    MagActivityDataRecord recb = getRecordAt(b);
    data.setElementAt(reca,b);
    data.setElementAt(recb,a);
  }

  protected void sortData() {
    int lo = 0;
    int up = rowNumber-1;
    int i,j;
    while ( up > lo ) {
      j = lo;
      for ( i = lo; i < up; i++ ){
        if( getMjd(i) > getMjd(i+1) ) {
          flipRows(i,i+1);
          j = i;
        }
      }
      up = j;
      for ( i = up; i > lo; i-- ){
        if( getMjd(i) < getMjd(i-1) ) {
          flipRows(i,i-1);
          j = i;
        }
      }
      lo = j;
    }

  }

  public boolean isValid(double value) {
    if ( value >= minValue && value <= maxValue ) return true;
    else return false;
  }

  public boolean isValid(Double value) {
    double val = value.doubleValue();
    if ( val >= minValue && val <= maxValue ) return true;
    else return false;
  }
  
/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
  public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
      propertyChangeSupport.addPropertyChangeListener (l);
  }

/** Add a PropertyChangeListener to the listener list.
 * @param l The listener to add.
 */
  public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener l) {
      propertyChangeSupport.addPropertyChangeListener (propertyName, l);
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
      OVTCore.setGlobalSetting(name+".File", file.getAbsolutePath());
      propertyChangeSupport.firePropertyChange ("file", oldFile, file);
  }
  
  
  /** Getter for property index.
   * @return Value of property index.
 */
  public int getIndex() {
      return index;
  }
  
  /** Setter for property index.
   * @param index New value of property index.
 */
  public void setIndex(int index) {
      this.index = index;
  }
  
/*  public void fireTableDataChanged() {
    ovt.util.Log.log("fireTableDataChanged executed!");
    super.fireTableDataChanged();
  } */

}

