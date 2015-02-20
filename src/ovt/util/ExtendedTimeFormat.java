/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/ExtendedTimeFormat.java,v $
  Date:      $Date: 2003/09/28 17:52:55 $
  Version:   $Revision: 1.4 $


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
 * ExtendedTimeFormat.java
 *
 * Created on July 17, 2001, 12:43 AM
 */

package ovt.util;

import ovt.*;
import ovt.gui.*;
import ovt.mag.*;
import ovt.beans.*;
import ovt.util.*;
import ovt.event.*;
import ovt.object.*;
import ovt.datatype.*;
import ovt.interfaces.*;

import java.beans.*;
import java.util.*;

/**
 *
 * @author  root
 * @version 
 */
public class ExtendedTimeFormat extends OVTObject {
    
    private static final int DAY    = 0;
    private static final int HOUR   = 1;
    private static final int MINUTE = 2;
    private static final int SECOND = 3;
    /** Holds value of property unit. */
    private int unit = DAY;
    private int unitMultiplyFactor = 1;

    /** Holds value of property offsetMjd. */
    private double offsetMjd = Time.Y2000;
    
    
    public ExtendedTimeFormat() {
    }
    
    public DoubleAndInteger parseMjd(String str) throws NumberFormatException {
        return parseMjd(str, 0);
    }
    
    public DoubleAndInteger parseMjd(String str, int bedinindex) throws NumberFormatException {
        int startIndex = StringUtils.doubleStartsAt(str, bedinindex);
        if (startIndex == -1) throw new NumberFormatException("Double could not be found in string ("+str+")");
        int endIndex = StringUtils.doubleEndsAt(str, startIndex);
        //Log.log("Trying to parse double from '"+str.substring(startIndex, endIndex+1)+"'");
        double v = new Double(str.substring(startIndex, endIndex+1)).doubleValue();
        
        double mjd = this.offsetMjd + v / this.unitMultiplyFactor;
        return new DoubleAndInteger(mjd, endIndex);
    }

    /** Getter for property unit.
     * @return Value of property unit.
 */
    public int getUnit() {
        return unit;
    }    

    /** Setter for property unit.
     * @param unit New value of property unit.
 */
    public void setUnit(int unit) throws IllegalArgumentException {
        if (unit < DAY  ||  unit>SECOND)
            throw new IllegalArgumentException("Wrong unit: ("+unit+")");
        int oldUnit = this.unit;
        this.unit = unit;
        if (unit == DAY) unitMultiplyFactor = 1;
        else unitMultiplyFactor = 24*(int)Math.pow(60., unit-1); 
        propertyChangeSupport.firePropertyChange ("unit", new Integer (oldUnit), new Integer (unit));
    }    

    
/** Getter for property offsetMjd.
 * @return Value of property offsetMjd.
 */
    public double getOffsetMjd() {
        return offsetMjd;
    }
    
    /** Setter for property offsetMjd.
     * @param offsetMjd New value of property offsetMjd.
 */
    public void setOffsetMjd(double offsetMjd) {
        double oldOffsetMjd = this.offsetMjd;
        this.offsetMjd = offsetMjd;
        propertyChangeSupport.firePropertyChange ("offsetMjd", new Double (oldOffsetMjd), new Double (offsetMjd));
    }
    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = new Descriptors();
                
                // date format
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("offsetMjd", this);
                pd.setDisplayName("Offset time:");
                pd.setMenuAccessible(false);
                GUIPropertyEditor editor = new MjdEditor(pd);
                pd.setPropertyEditor(editor);
                addPropertyChangeListener("offsetMjd", editor);
                descriptors.put(pd);
                
                // hours format
                pd = new BasicPropertyDescriptor("unit", this);
                pd.setDisplayName("Time unit");
                pd.setMenuAccessible(false);
                editor = new ComboBoxPropertyEditor(pd, new int[]{DAY, HOUR, MINUTE, SECOND}, new String[]{"day", "hour", "minute", "second"});
                pd.setPropertyEditor(editor);
                addPropertyChangeListener("unit", editor);
                descriptors.put(pd);
                
            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }    
}
