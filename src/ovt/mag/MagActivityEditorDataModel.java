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
 * MagActivityEditorDataModel.java
 *
 * Created on den 25 mars 2000, 00:30
 */
package ovt.mag;

import ovt.beans.*;
import ovt.datatype.*;
import ovt.OVTCore;

import java.io.*;
import java.util.*;
import ovt.mag.MagProps.MagActivityDataSource;
import ovt.util.Log;

/**
 * Class modelling the "magnetic" activity data in a JTable (GUI) and read
 * from/saved to files.
 *
 * Files should exist in directory <I>mdata/</I> on the disk and have header
 * with column names. First column should be always <B>Time</B>. All extra and
 * insufficient data will be ignored and overwritten after <CODE>save()</CODE>.
 *
 * @author Yuri Khotyaintsev
 * @version 1.0
 */
public class MagActivityEditorDataModel extends javax.swing.table.AbstractTableModel implements MagActivityDataSource {

    private static final int DEBUG = 20;  // Log message log level.

    private String name = null;
    private Vector data = new Vector();
    private MagActivityDataRecord defaultValues;
    private double minValue;
    private double maxValue;
    private String[] columnNames = null;
    private int columnNumber = 0;
    private int rowCount = 0;   // Number of rows of data = length of the "data" vector.

//    private double cachedMjd = -1;   // The time for which "cachedValues" is valid.
//    private MagActivityDataRecord cachedValues = null;  // Cached reference for last call to getValues(..)
    private File file = null;
    private int index;

    /**
     * Utility field used by bound properties.
     */
    private OVTPropertyChangeSupport propertyChangeSupport = new OVTPropertyChangeSupport(this);


    /**
     * Constructor when using one "index" (data) column.
     */
    public MagActivityEditorDataModel(int index, double minValue, double maxValue, double defaultValue, String columnName) {
        init(index, minValue, maxValue, new double[]{defaultValue}, new String[]{columnName});
    }


    /**
     * Constructor when using an arbitrary number of "index" (data) columns.
     *
     * @param param Name of activity parameter.
     * @throws Exception for parsing problems
     * @throws FileNotFoundException for file lookup
     * @throws IOException for IO problems
     */
    public MagActivityEditorDataModel(int index, double minValue, double maxValue, double[] defaultValues, String[] columnNames) {
        init(index, minValue, maxValue, defaultValues, columnNames);
    }


    /**
     * Default values should not include time. Default time is Y2000.
     */
    private void init(int index, double minValue, double maxValue, double[] defaultValues, String[] columnNames) {
        this.index = index;
        this.name = MagProps.getActivityName(index);
        this.minValue = minValue;
        this.maxValue = maxValue;
        // set column names
        this.columnNames = new String[columnNames.length + 1];
        this.columnNames[0] = "Time";
        for (int i = 0; i < columnNames.length; i++) {
            this.columnNames[i + 1] = columnNames[i];
        }
        this.columnNumber = this.columnNames.length;
        // set default values
        this.defaultValues = new MagActivityDataRecord(Time.Y2000, defaultValues);
        this.file = new File(OVTCore.getGlobalSetting(name + ".File", OVTCore.getUserdataSubdir() + name + ".dat"));

        try {
            //file = new File(ovt.OVTCore.getUserdataSubdir(), param + magDataExt);
            load();
        } catch (IOException e2) {
        }
        if (data.size() == 0) {
            data.addElement(getDefaultValues().clone());
        }
        //System.out.println(getName() + " size = " + data.size() );
        rowCount = data.size();
    }


    protected void load() throws IOException {
        final Vector newData = new Vector();
        final RandomAccessFile fileIn = new RandomAccessFile(file, "r");
        final long length = fileIn.length();

    //if ( length <= 0 ) throw new Exception("MagActivityEditorDataModel: activity file is empty");
        /* Read data
         first entry is time in format 1994-04-04 12:00:00
         to be understood by ovt.datatype.Time.
         Incomplete lines or extra data will be disregarded */
        //int rowCount = 0;
        int fileCount = 0;
        String s;
        while (fileIn.getFilePointer() < length) {
            boolean parsed = true;
            fileCount++;
            s = fileIn.readLine();

            if (s.startsWith("#")) {
                continue; // skip comments
            }
            final StringTokenizer tok = new StringTokenizer(s, "\t");
            try {
                if (tok.countTokens() < getColumnCount()) {
                    throw new NumberFormatException();
                }
                final Time time = new ovt.datatype.Time(tok.nextToken());
                final double[] dataRead = new double[getColumnCount() - 1];
                for (int i = 0; i < dataRead.length; i++) {
                    dataRead[i] = Double.parseDouble(tok.nextToken());
                    if (!isValid(dataRead[i])) {
                        throw new NumberFormatException("Loaded value is outside of (reasonable) range.");
                    }
                }
                newData.addElement(new MagActivityDataRecord(time, dataRead));
            } catch (NumberFormatException e2) {
                System.out.println("parse error in line #" + fileCount + " file "
                        + file.getAbsolutePath());
            }
        }
        fileIn.close();
        if (newData.size() == 0) {
            throw new IOException("File is empty");
        }
        data = newData;
        rowCount = data.size();

        sortData();
        fireTableDataChanged();
        //fireTableChanged(new TableModelEvent(
    }


    /**
     * Removes all elements from data and then adds defaultValues record.
     */
    public void reset() {
        data.removeAllElements();
        data.addElement(getDefaultValues().clone());
        rowCount = data.size();
//        cachedMjd = -1;
//        cachedValues = null;
        fireTableDataChanged();
    }


    /**
     * @return number of rows (used by XML & JTable)
     */
    @Override
    public int getRowCount() {
        return rowCount;
    }


    /**
     * set number of rows (used by XML)
     */
    public void setRowCount(int numberOfRows) {
        rowCount = numberOfRows;
        data.setSize(numberOfRows);
    }


    /**
     * Is used by XML to get data record
     */
    public MagActivityDataRecord getRecordAt(int row) {
        return (MagActivityDataRecord) data.elementAt(row);
    }


    /**
     * Is used by XML to set data record
     */
    public void setRecordAt(int row, MagActivityDataRecord MagActivityDataRecord) {
        data.setElementAt(MagActivityDataRecord, row);
    }


    @Override
    public int getColumnCount() {
        return columnNumber;
    }


    @Override
    /**
     * Get value to put in specific cell in the GUI table.
     */
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= rowCount || col < 0 || col >= columnNumber) {
            throw new IllegalArgumentException("Index out of bounds");
        } else {
            return getRecordAt(row).get(col);
        }
    }


    public MagActivityDataRecord getDefaultValues() {
        return defaultValues;
    }


    /**
     * Return the corresponding row for a given time. If the mjd is between two
     * rows, then choose the row preceeding in time. Otherwise choose the row
     * nearest in time.
     */
    protected int getRow(double mjd) {
        final int rowCount = getRowCount();

        if (rowCount == 0) {
            throw new IllegalArgumentException();
        } else if (rowCount == 1) {
            return 0; // only one data line present
        } else {

            if (mjd <= getMjd(0)) {
                // CASE: mjd less than value on first row.
                return 0; // request before first data - return first data
            }
            if (mjd >= getMjd(rowCount - 1)) {
                // CASE: mjd greater than value on last row.
                return rowCount - 1; //request after last data
            }

            for (int i = 0; i < rowCount - 1; i++) {
                if (getMjd(i + 1) > mjd) {
                    return i;
                }
            }
            return rowCount - 1;
        }
    }

    /*public Object getValueAt(double mjd, int element) {
     if (mjd == lastMjd && lastValues != null) return lastValues.get(element);
     try {
     lastValues  = getRecordAt(getRow(mjd));
     } catch (IllegalArgumentException e2) {
     lastValues = getDefaultValues();
     }
     lastMjd = mjd;
     return lastValues.get(element);
     }*/

    /**
     * Derives the relevant value(s) for an arbitrary point in time.
     *
     * IMPLEMENTATION NOTE: Used to have an internal cache to speed up calls.
     * The cache is not as useful as it used to be before implementing the cache
     * in MagProps#getActivity but it still did a little bit of work work.
     * Remove the cache entirely?
     * /Erik P G Johansson 2015-10-23 (who did not write that code)
     *
     * IMPLEMENTATION NOTE: It appears that #getValues can return references to
     * an array which is later modified when the corresponding table entry is
     * modified. Can most likely be modifed to return a deep copy but I have
     * refrained from doing so, since it might have to do with how data
     * propagates throughout OVT when the "Apply"/"Update Visualization" button
     * is pressed (which triggers code which does not update information the way
     * one would expect from the name "Apply"). I know this is a long shot but I
     * have done so to be on the safe side and not introduce bugs. <BR>
     * /Erik P G Johansson 2015-10-23 (who did not write that code)
     */
    public double[] getValues(double mjd) {
//        if (mjd == cachedMjd && cachedValues != null) {
//            // CASE: Asks for the same mjd as the last time. ==> Cached value is invalid.
//            Log.log(this.getClass().getSimpleName()
//                    + "#getValues(" + mjd + /*"<=>" + new Time(mjd) +*/ ") = "
//                    + Arrays.toString(cachedValues.values)
//                    + "   // Cached value", DEBUG);
//            return cachedValues.values;
//        }
        MagActivityDataRecord returnValues;
        try {
            //cachedValues = getRecordAt(getRow(mjd));
            returnValues = getRecordAt(getRow(mjd));
        } catch (IllegalArgumentException e2) {
            //cachedValues = getDefaultValues();
            returnValues = getDefaultValues();
        }
        //cachedMjd = mjd;
        /*Log.log(this.getClass().getSimpleName()
         + "#getValues(" + mjd + ") = "
         + Arrays.toString(returnValues.values) //+ "   // (Non-cached value)"
         , DEBUG);*/
        //return cachedValues.values;
        return returnValues.values;
        //return returnValues.values.clone();  // Better return deep copy?
    }


    /**
     * Get mjd value for specific row (not arbitrary time).
     */
    public double getMjd(int row) {
        return getRecordAt(row).time.getMjd();
    }


    /*public double getLastMjd() {
     return lastMjd;
     }*/
    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }


    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            if (ovt.datatype.Time.isValid((String) value)) {
                final MagActivityDataRecord rec = getRecordAt(row);
                rec.time = new Time((String) value);
                final double mjd = rec.time.getMjd();

                // Move the current row so that the whole table is ordered by time.
                if (row < rowCount - 1) {
                    if (mjd > getMjd(row + 1)) {
                        int i = row + 1;
                        while (mjd > getMjd(i)) {
                            flipRows(i, i - 1);
                            fireTableRowsUpdated(i - 1, i);
                            i++;
                            if (i == rowCount) {
                                break;
                            }
                        }
                    } else if (row > 0) {
                        if (mjd < getMjd(row - 1)) {
                            int i = row - 1;
                            while (mjd < getMjd(i) && i >= 0) {
                                flipRows(i, i + 1);
                                fireTableRowsUpdated(i, i + 1);
                                i--;
                                if (i < 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            final MagActivityDataRecord rec = getRecordAt(row);
            try {
                final double dat = Double.parseDouble((String) value);
                if (isValid(dat)) {
                    rec.values[col - 1] = dat;
                } else {
                    System.out.println("Specified value is out of range");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format");
            }
        }
//        cachedMjd = -1;
//        cachedValues = null;
    }


    public String getName() {
        return name;
    }


    @Override
    public String getColumnName(int col) {
        if (col < 0 || col >= columnNumber) {
            return null;
        } else {
            return columnNames[col];
        }
    }


    /**
     * Saves <CODE>data</CODE> to file. Old file should be moved to .bak
     *
     * @throws FileNotFoundException {@link java.io.FileNotFoundException}
     * @throws IOException {@link java.io.IOException}
     */
    public void save() throws IOException {
        //System.out.println("Saving ...");
        PrintWriter fileOut = new PrintWriter(new FileOutputStream(file.getAbsolutePath(), false));
        // create header
        String line = "# Time";
        for (int i = 1; i < columnNumber; i++) {
            line = line + "\t" + columnNames[i];
        }
        fileOut.println(line);
        // write data
        for (int i = 0; i < rowCount; i++) {
            line = "";
            for (int j = 0; j < columnNumber; j++) {
                line += getValueAt(i, j) + "\t";
            }
            fileOut.println(line);
        }
        fileOut.close();
    }


    public void insertRows(int row) {
        MagActivityDataRecord rec = (MagActivityDataRecord) getRecordAt(row).clone();
        data.insertElementAt(rec, row + 1);
        rowCount = data.size();
        fireTableRowsInserted(row, row + 1);
    }


    public void deleteRows(int firstRow, int lastRow) {
        for (int i = lastRow; i >= firstRow; i--) {
            data.removeElementAt(i);
        }
        rowCount = data.size();
        fireTableRowsDeleted(firstRow, lastRow);
    }


    protected void flipRows(int a, int b) {
        MagActivityDataRecord reca = getRecordAt(a);
        MagActivityDataRecord recb = getRecordAt(b);
        data.setElementAt(reca, b);
        data.setElementAt(recb, a);
    }


    /**
     * Used to sort the data by time. NOTE: Not to be confused with sorting
     * columns in the GUI (clicking on column title), which the Java GUI
     * routines (Swing/AWT) handle themselves.
     */
    protected void sortData() {
        int lo = 0;
        int up = rowCount - 1;
        int i, j;
        while (up > lo) {
            j = lo;
            for (i = lo; i < up; i++) {
                if (getMjd(i) > getMjd(i + 1)) {
                    flipRows(i, i + 1);
                    j = i;
                }
            }
            up = j;
            for (i = up; i > lo; i--) {
                if (getMjd(i) < getMjd(i - 1)) {
                    flipRows(i, i - 1);
                    j = i;
                }
            }
            lo = j;
        }
    }


    private boolean isValid(double value) {
        return value >= minValue && value <= maxValue;
    }


    // Seems unused.
  /*public boolean isValid(Double value) {
     double val = value.doubleValue();
     return val >= minValue && val <= maxValue;
     }*/
    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }


    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, l);
    }


    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }


    /**
     * Getter for property file.
     *
     * @return Value of property file.
     */
    public File getFile() {
        return file;
    }


    /**
     * Setter for property file.
     *
     * @param file New value of property file.
     */
    public void setFile(File file) {
        File oldFile = this.file;
        this.file = file;
        OVTCore.setGlobalSetting(name + ".File", file.getAbsolutePath());
        propertyChangeSupport.firePropertyChange("file", oldFile, file);
    }


    /**
     * Getter for property index.
     *
     * @return Value of property index.
     */
    public int getIndex() {
        return index;
    }


    /**
     * Setter for property index.
     *
     * @param index New value of property index.
     */
    // Seems unused.
    public void setIndex(int index) {
        this.index = index;
    }


    public void fireTableDataChanged() {
        ovt.util.Log.log("fireTableDataChanged executed!", DEBUG);
        super.fireTableDataChanged();
    }

}
