/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /ovt/datatype/Interval.java $
 Date:      $Date: 2006/02/20 16:06:39 $
 Version:   $Revision: 2.6 $


 Copyright (c) 2000-2015 OVT Team (Kristof Stasiewicz, Mykola Khotyaintsev,
 Yuri Khotyaintsev, Erik P. G. Johansson, Fredrik Johansson)
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
 Khotyaintsev, E. P. G. Johansson, F. Johansson
 
 =========================================================================*/
/*
 * Interval.java
 *
 * Created on March 20, 2001, 12:02 PM
 */
package ovt.datatype;

import java.util.*;

/**
 * Instances of the class represents TIME INTERVALS (not an absolute points in
 * time)
 *
 * NOTE: This class used to have a strange implementation using an internal
 * ovt.datatype.Time object meant having day>30, hour>23, or min>59 led to
 * trying to create an illegal date, which triggered an exception, which
 * prevents the caller from defining such time intervals. Therefore the class
 * has been reimplemented to handle arbitrarily large (positive) time
 * intervals.<BR>
 * /Erik P G Johansson 2015-07-10.
 *
 * @author ko, Erik P G Johansson
 * @version
 */
public class Interval {

    //private final Time time;
    private int days = 0;    // Values >=0
    private int hours = 0;   // Values 0-23
    private int minutes = 0;   // Values 0-59
    private double seconds = 0; // Values 0-59.9999...


    // Used 1 time.
    // NOTE: Possible to set impossible combinations.
    public Interval(int days, int hours, int minutes, double seconds)
            throws IllegalArgumentException {
        //time = new Time(1950, 01, day + 1, hour, min, 0);     // NOTE: days + 1.
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        normalize();
    }


    // Used 6 times
    public Interval(double intervalInDays) throws IllegalArgumentException {
        //if (days >= 32) {
        //    throw new IllegalArgumentException("The interval can not be greater or equal than 32 days.");
        //}
        //time = new Time(days);  // Time(double): Argument is interpreted as mjd.

        this.days = 0;
        this.hours = 0;
        this.minutes = 0;
        this.seconds = intervalInDays * Time.SECONDS_IN_DAY;
        normalize();
    }


    // Used 3 times.
    /*public Interval(String s) throws NumberFormatException {
     int day = 0;
     int hour = 0;
     int min = 0;
     double sec = 0;
     StringTokenizer st = new StringTokenizer(s, " ");
     String elem;
     while (st.hasMoreTokens()) {
     elem = st.nextToken().toLowerCase();
     if (elem.endsWith("d")) {
     day = new Integer(elem.substring(0, elem.length() - 1)).intValue();
     } else if (elem.endsWith("h")) {
     hour = new Integer(elem.substring(0, elem.length() - 1)).intValue();
     } else if (elem.endsWith("m")) {
     min = new Integer(elem.substring(0, elem.length() - 1)).intValue();
     } else if (elem.endsWith("s")) {
     sec = new Integer(elem.substring(0, elem.length() - 1)).intValue();
     } else {
     throw new NumberFormatException();
     }
     }
     time = new Time(1950, 01, day + 1, hour, min, sec);   // NOTE: Days plus one.
     }*/
    public Interval(String s) throws NumberFormatException {
        final StringTokenizer st = new StringTokenizer(s, " ");

        int tempDays = 0, tempHours = 0, tempMinutes = 0, tempSeconds = 0;
        while (st.hasMoreTokens()) {
            final String elem = st.nextToken().toLowerCase();
            if (elem.endsWith("d")) {
                tempDays += Integer.parseInt(elem.substring(0, elem.length() - 1));
            } else if (elem.endsWith("h")) {
                tempHours += Integer.parseInt(elem.substring(0, elem.length() - 1));
            } else if (elem.endsWith("m")) {
                tempMinutes += Integer.parseInt(elem.substring(0, elem.length() - 1));
            } else if (elem.endsWith("s")) {
                tempSeconds += Integer.parseInt(elem.substring(0, elem.length() - 1));   // NOTE: Only integer number of seconds.
            } else {
                throw new NumberFormatException();
            }
        }

        // NOTE: Do not want to assign the internal instance fields until we know there was no error.
        days = tempDays;
        hours = tempHours;
        minutes = tempMinutes;
        seconds = tempSeconds;
        normalize();
    }


    // Uncertain how many times this method is called. NetBeans can not reliably detect its use detects toString() for other classes to.
    // (Use of this method can probably be hidden from NetBeans too, due to special syntax for toString().)
    /*public String toString() {
     int day = time.getDays() - 1;   // NOTE: Minus one.
     int hour = time.getHours();
     int min = time.getMinutes();
     int sec = (int) time.getSeconds();
     String res = "";
     res += (day != 0 ? "" + day + "d " : "");
     res += (hour != 0 ? "" + hour + "h " : "");
     res += (min != 0 ? "" + min + "m " : "");
     res += (sec != 0 ? "" + sec + "s " : "");
     return res;
     }*/
    public String toString() {
        final String s = toString(false);
        return s;
    }


    /**
     * Prints out length of interval using days-hours-minutes-seconds. Ignores
     * fields that are zero. Optionally rounds number of seconds to an integer.
     */
    public String toString(boolean roundSeconds) {

        final Interval i = new Interval(days, hours, minutes, seconds);
        if (roundSeconds) {
            /**
             * NOTE: Important to round properly, not only round down or up.
             * Rounding up/down can otherwise make e.g. time settings change by
             * themselves when the user presses "Apply".
             */
            i.setSeconds((int) Math.round(seconds));
        }
        i.normalize();

        String str = "";
        str += (i.days != 0 ? "" + i.days + "d " : "");
        str += (i.hours != 0 ? "" + i.hours + "h " : "");
        str += (i.minutes != 0 ? "" + i.minutes + "m " : "");
        str += (i.seconds != 0 ? "" + i.seconds + "s " : "");
        return str.trim();
        //return res;        
    }


    // Used 3 times.
    /**
     * Get length of entire interval in seconds.
     */
    public double getIntervalInDays() {
        //return time.getIntervalInDays();
        return getIntervalInSeconds() * Time.DAYS_IN_SECOND;
    }


    // Used 3 times.
    public double getDays() {
        //return time.getDays() - 1;   // NOTE: getDays() minus one.
        return days;
    }


    // Used 2 times.
    public double getHours() {
        //return time.getHours();
        return hours;
    }


    // Used 1 time.
    // NOTE: One of two functions that changes the internal state.
    public void setMinutes(int mins) {
        //time.setMinutes(mins);    // Set number of minutes in the field "minutes", minutes in an hour, 0-59.
        this.minutes = mins;
        normalize();
    }


    // Used 2 times.
    public double getMinutes() {
        //return time.getMinutes();
        return minutes;
    }


    // Used 1 time.
    // NOTE: One of two functions that changes the internal state, and the it is used to "round away" the seconds.
    // NOTE: Only integer seconds.
    public void setSeconds(int sec) {
        // //time.setTime( new Time(1950, 01, time.getDays(), time.getHours(), time.getMinutes(), 0, time.getMsec()) );
        //time.setSeconds(sec);   // Set number of seconds in the fields "seconds", seconds in a minute, 0-59.
        this.seconds = sec;
        normalize();
    }


    // Used 2 times.
    public double getSeconds() {
        //return time.getSeconds();   // Get number of seconds in the fields "seconds", seconds in a minute, 0-59.
        return seconds;
    }


    // Used 1 time.
    public boolean equals(Interval interval) {
        return (interval.getDays() == getDays()
                && interval.getHours() == getHours()
                && interval.getMinutes() == getMinutes()
                && interval.getSeconds() == getSeconds());
    }


    /**
     * Get length of entire interval in seconds
     */
    private double getIntervalInSeconds() {
        //return this.days + this.hours/24.0 + this.minutes/Time.MINUTES_IN_DAY + this.seconds/Time.SECONDS_IN_DAY;
        return this.days * Time.SECONDS_IN_DAY + this.hours * (60 * 60) + this.minutes * 60 + this.seconds;

    }


    /**
     * Redistribute the values of the internal fields so that no field
     * represents more time than the one unit of a "greater" field, .e.g. second
     * = 0 to 59, hours = 0 to 59 and so on.
     */
    private void normalize() {
        double remSeconds = getIntervalInSeconds();         // rem = remaining

        this.days = (int) Math.floor(((long) remSeconds) / Time.SECONDS_IN_DAY);
        remSeconds = remSeconds - this.days * Time.SECONDS_IN_DAY;

        this.hours = (int) Math.floor(((long) remSeconds) / 3600);
        remSeconds = remSeconds - this.hours * 3600;

        this.minutes = (int) Math.floor(((long) remSeconds) / 60);
        remSeconds = remSeconds - this.minutes * 60;

        this.seconds = remSeconds;
    }


    /**
     * Informal test code.
     */
    public static void main(String[] args) {
        /*class Test {
         int d,h,m;
         double s;
         Test(int d, int h, int m, double s) {
         this.d=d;
         this.h=h;
         this.m=m;
         this.s=s;
         }
         }*/

        final String[] inputs = {"1d 2m 4h", "1d 62m 4h", "1d 2m 50h", "150m 50h 1d", "4d 60h 150m 301s"};
        final String[] outputs = {"1d 4h 2m", "1d 5h 2m", "3d 2h 2m", "3d 4h 30m", "6d 14h 35m 1.0s"};
        for (int i = 0; i < inputs.length; i++) {
            final Interval newI = new Interval(inputs[i]);
            final String actualOutput = newI.toString();

            System.out.println(inputs[i] + " ==> " + actualOutput);
            if (!actualOutput.equals(outputs[i])) {
                System.out.println("ERROR");
                System.exit(1);
            }
        }
    }
}
