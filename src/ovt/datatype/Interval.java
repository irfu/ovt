/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Interval.java,v $
  Date:      $Date: 2006/02/20 16:06:39 $
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
 * Interval.java
 *
 * Created on March 20, 2001, 12:02 PM
 */

package ovt.datatype;

import java.util.*;

/**
 *
 * @author  ko
 * @version 
 */
public class Interval {
    Time time;

    public Interval(int day, int hour, int min) throws IllegalArgumentException {
        time = new Time(1950, 01, day + 1, hour, min, 0);
    }
    
    public Interval(double days) throws IllegalArgumentException {
        if (days >=32) throw new IllegalArgumentException("The interval can not be greater or equal to  32 days");
        time = new Time(days);
    }
    
    public Interval(String s) throws NumberFormatException {
        int day = 0;
        int hour = 0;
        int min = 0;
        double sec = 0;
        StringTokenizer st = new StringTokenizer(s, " ");
        String elem;
        while (st.hasMoreTokens()) {
            elem = st.nextToken().toLowerCase();
            if (elem.endsWith("d")) day = new Integer(elem.substring(0, elem.length() - 1)).intValue(); 
            else if (elem.endsWith("h")) hour = new Integer(elem.substring(0, elem.length() - 1)).intValue(); 
            else if (elem.endsWith("m")) min = new Integer(elem.substring(0, elem.length() - 1)).intValue();
            else if (elem.endsWith("s")) sec = new Integer(elem.substring(0, elem.length() - 1)).intValue();
            else throw new NumberFormatException();
        }
        time = new Time(1950, 01, day + 1, hour, min, sec);
    }
    
    public String toString() {
        int day = time.getDay() - 1;
        int hour = time.getHour();
        int min = time.getMinutes();
        int sec = (int)time.getSeconds();
        String res = "";
        res += (day != 0 ? "" + day + "d " : "");
        res += (hour != 0 ? "" + hour + "h " : "");
        res += (min != 0 ? "" + min + "m " : "");
        res += (sec != 0 ? "" + sec + "s " : "");
        return res;
    }

    public double getMjd() {
        return time.getMjd();
    }
    
    public double getDay() {
        return time.getDay() - 1;
    }
    
    public double getHour() {
        return time.getHour();
    }
    
    public void setMinutes(int mins) {
        time.setMinutes(mins);
    }
    
    public double getMinutes() {
        return time.getMinutes();
    }
    
    public void setSeconds(int sec) {
        //time.setTime( new Time(1950, 01, time.getDay(), time.getHour(), time.getMinutes(), 0, time.getMsec()) );
        time.setSeconds(sec);
    }
    
    public double getSeconds() {
        return time.getSeconds();
    }
    
    
    public boolean equals(Interval interval) {
        return (interval.getDay() == getDay()  &&  interval.getHour() == getHour()
                &&  interval.getMinutes() == getMinutes()  &&  interval.getSeconds() == getSeconds());
    }
}
