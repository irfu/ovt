/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Characteristics.java,v $
  Date:      $Date: 2003/09/28 17:52:36 $
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
 * Characteristics.java
 *
 * Created on March 26, 2001, 3:29 PM
 */

package ovt.datatype;

import java.io.*;
import java.util.*;

/**
 *
 * @author  ko
 * @version 
 * We store all data in double[].
 */

public class Characteristics extends IntHashtable {
    
    /** Holds value of property mjd. */
    private double mjd = -1;
    
    /** Creates new Characteristics.
     * We store all data in double[].
     */
    public Characteristics() {
    }
    
    public Characteristics(double mjd) {
        this.mjd = mjd;
    }
    
    public void put (int index, double value) {
        put( index, new double[]{value});
    }
    
    public boolean equals(Characteristics obj) {
        // check the size
        if (obj.size() != size()) return false;
        Enumeration e = keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            if (!equal(get(key), obj.get(key))) return false;
        }
        return true;
    }

    private static boolean equal(Object obj1, Object obj2) {
        if ((obj1 instanceof double[] ) && (obj2 instanceof double[] )) 
            return doubleArraysEqual((double[])obj1, (double[])obj2);
        else return obj1.equals(obj2); 
    }
    
    private static boolean doubleArraysEqual(double[] a, double[] b) {
        if (a.length != b.length) return false;
        for (int i=0; i<a.length; i++)
            if (a[i] != b[i]) return false;
        return true;
    }
    
    public static void main(String[] args) {
        double[] a = {1, 2, 3, 4};
        double[] b = {1, 2, 2, 4};
        System.out.println(""+Characteristics.equal(a, b));
    }
    
    /** Getter for property mjd.
     * @return Value of property mjd.
 */
    public double getMjd() {
        return mjd;
    }
    
    /** Setter for property mjd.
     * @param mjd New value of property mjd.
 */
    public void setMjd(double mjd) {
        this.mjd = mjd;
    }
    
    public void list() {
        list(System.out);
    }
    
    public void list(PrintStream out) {
        out.println("Time : " + new Time(mjd));
        Enumeration e = elements();
        while (e.hasMoreElements()) {
            double[] values = (double[])e.nextElement();
            for (int i=0; i<values.length; i++) 
                out.print(values[i] + "\t");
            out.println();
        }
    }
    
    public Characteristics getInstance() {
        Characteristics res = new Characteristics(mjd);
        Enumeration e = keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            double[] values = (double[])get(key);
            //res.put(key, values.clone());
            res.put(key, ((double[])values).clone());
        }
        return res;
    }
    
}
