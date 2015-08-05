/*=========================================================================

 Program:   Orbit Visualization Tool
 Source:    $Source: /stor/devel/ovt2g/ovt/datatype/Time.java,v $
 Date:      $Date: 2006/02/20 16:06:39 $
 Version:   $Revision: 2.7 $


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
// Time: "1996-12-01 12:00"
//
//
package ovt.datatype;

import java.lang.*;
import java.text.*;
import java.util.*;

/**
 * One should remove the concept "1996-12-01 12:00:00" and move to the one like
 * in ISDAT "1996-12-01 12:00:00.0"
 */
/**
 * Class whose instances represent a point in time expressed with year, month,
 * day, hour, minute, second (as opposed to e.g. Julian Day or number of seconds
 * since a point in time "X"). Instances of Time are mutable.
 *
 * Modified Julian Day (mjd):<BR>
 * --------------------------<BR>
 * The class contains methods for converting between "Time" and "modified Julian
 * Day" (mjd) and therefore defines what "mjd" means. Modified Julian Day (mjd)
 * is the number of days since January 1, 1950, 00:00:00 (epoch). Must not
 * necessarily be an integer. OVT's mjd therefore uses another epoch than what
 * is conventionally meant when using the term "Modified Julian Day". <BR>
 *
 * NOTE 1: The conversion functions between mjd Time (given in years, months,
 * days, hours, minutes, seconds) assumes that there is a constant
 * 24*60*60=86400 s/day, i.e. there are no leap seconds (as in UTC), but mjd
 * still follows astronomical days (Earth rotation relative to the direction of
 * the Sun) on multi-year timescales. Since the speed of Earth's rotation varies
 * slightly, this can be interpreted as that the physical length of a second in
 * the "Time" class varies slightly.<BR>
 *
 * NOTE 2: Since the length of astronomical days (Earth rotation relative to the
 * Sun) varies on multi-year time scales, a specific difference between two Mjd
 * values (or Julian Day values) is technically not a proportional to a specific
 * amount of physical time, albeit it is very close.<BR>
 * /Erik P G Johansson 2015-06-16
 */
public class Time {

    // Constants that can be used to convert between OVT's mjd and Julian Day for other epochs.
    public static final double Y2000 = Time.getMjd(2000, 1, 1, 0, 0, 0);
    public static final double Y1970 = Time.getMjd(1970, 1, 1, 0, 0, 0);
    public static final double Y1960 = Time.getMjd(1960, 1, 1, 0, 0, 0);
    public static final double Y1950 = Time.getMjd(1950, 1, 1, 0, 0, 0);
    public static final double Y3799 = Time.getMjd(3799, 1, 1, 0, 0, 0);

    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;
    public static final int HOUR = 3;
    public static final int MINUTE = 4;
    public static final int SECOND = 5;

    /**
     * This value should be used in the case of error mjd This can happen if you
     * have to return some mjd, but something went wrong.
     */
    public static final double ERROR_MJD = Double.MIN_VALUE;
    public static final int MINUTES_IN_DAY = 24 * 60;
    public static final int SECONDS_IN_DAY = 24 * 60 * 60;
    public static final double DAYS_IN_SECOND = 1. / (double) SECONDS_IN_DAY;

    private int year = 0;
    private int month = 0;  // Values 1-12 (not 0-11).
    private int day = 0;    // Values 1-31 (depending on month)
    private int hour = 0;   // Values 0-23
    private int mins = 0;   // Values 0-59
    private double sec = 0; // Values 0-59.9999... , according to isValid(...).


    public Time(String time)
            throws NumberFormatException {
        setTime(time);
    }


    public Time(double mjd)
            throws IllegalArgumentException {
        setTime(mjd);
    }


    public Time(int year, int month, int day, int hour, int mins, double sec)
            throws IllegalArgumentException {
        setTime(year, month, day, hour, mins, sec);
    }


    public Time(Calendar date) {
        // Calendar.JANUARY == 0  -> +1
        setTime(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1, // NOTE: Plus one. Why?
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));
    }


    public void setTime(int year, int month, int day, int hour, int mins, double sec)
            throws IllegalArgumentException {

        if (!isValid(year, month, day, hour, mins, sec)) {
            throw new IllegalArgumentException();
        }
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.mins = mins;
        this.sec = sec;
    }


// this method should be moved away to the TimeFormat class
    public void setTime(String time) throws NumberFormatException {
        try {
            year = Integer.valueOf(time.substring(0, 4)).intValue();
            month = Integer.valueOf(time.substring(5, 7)).intValue();
            day = Integer.valueOf(time.substring(8, 10)).intValue();
            hour = Integer.valueOf(time.substring(11, 13)).intValue();
            mins = Integer.valueOf(time.substring(14, 16)).intValue();
            if (time.length() >= 19) {
                sec = Integer.valueOf(time.substring(17, 19)).intValue();
            } else {
                sec = 0;
            }

            if (!isValid(year, month, day, hour, mins, sec)) {
                throw new IllegalArgumentException();
            }
            setTime(year, month, day, hour, mins, sec);
        } catch (IndexOutOfBoundsException e2) {
            throw new NumberFormatException(e2.getMessage());
        } catch (IllegalArgumentException e3) {
            throw new NumberFormatException(e3.getMessage());
        }
    }


    public void setTime(Time time) {
        this.year = time.year;
        this.month = time.month;
        this.day = time.day;
        this.hour = time.hour;
        this.mins = time.mins;
        this.sec = time.sec;
    }


    /**
     * @return Return time value in mjd.
     */
    public static double getMjd(String s) throws NumberFormatException {
        Time t = new Time(s);
        return t.getMjd();
    }


    /**
     * Convert from mjd to "Time". Uncertain what the actual time limits (min &
     * max) are for this function to work. Conversion double-to-int is one
     * limit. It is known empirically that something goes wrong with
     * date-to-mjd-to-date for times before 1950-01-01, 00:00.00 (i.e. negative
     * mjd values).
     *
     * @see Comments for Time#getMjd(int year, int month, int day, int hour, int
     * mins, double sec)
     */
    public void setTime(double mjd) {

        if ((mjd < Y1950) || (Y3799 < mjd)) {
            throw new IllegalArgumentException("mjd=" + mjd + " is out of range for conversion formula.");
        }

        int jday;
        double temp;
        int l, m, n, jj;

        // was before 2006-02-20 temp = mjd + 5.7870370370370369e-9;
        //
        // we add 1e-12 in order to avoid cases, when 0.99999999999
        // will be counted as 0 when casting from double to int
        temp = mjd + 2e-12;
        jday = (int) temp;
        l = (jday + 18204) * 4000 / 1461001;
        n = jday - (l * 1461) / 4 + 18234;
        m = (n * 80) / 2447;

        // Set instance variables: day, month etc..
        day = n - (m * 2447) / 80;
        jj = m / 11;
        month = m + 2 - jj * 12;
        year = l + 1900 + jj;
        temp = (temp - jday) * 24.;
        hour = (int) temp;
        temp = (temp - hour) * 60.;
        mins = (int) temp;
        sec = (temp - mins) * 60. - 2.0954757928848267E-7;// - 1.2665987e-7; 
        if (sec < 0) {
            sec = 0;
        }
	// 2.0954757928848267E-7 is subtracted because of before added 2e-12 to mjd
        // this is very rough, needs to be considered in detail later

        //this.sec = (int) temp;
        //temp = (temp - sec) * 1000.;
        //this.msec = (int)(temp + .5); 
    }


    /**
     * Convert from "Time" to "Modified Julian day" (Mjd).<BR>
     *
     * NOTE: Not known where formula comes from but empirically it seems to work
     * for the itme intervals OVT uses. The Mjd conversion jumps at year 1900
     * (and every 1900 years) which the real Julian day conversion function does
     * not.<BR>
     * /Erik P G Johansson 2015-06-25.
     *
     * NOTE: There is at least one OVT function that converts in the opposite
     * direction: public void setTime(double mjd)<BR>
     * (non-static) which should be modified if this function is modified. As
     * long as both functions use the same offset the technicalites are probably
     * not that important. <BR>
     */
    public static double getMjd(int year, int month, int day, int hour, int mins, double sec)
            throws NumberFormatException {

        if (year < 1900) {
            throw new IllegalArgumentException("Can not use year = " + year + " < 1900.");
        } else if (year >= (2 * 1900)) {
            throw new IllegalArgumentException("Can not use year = " + year + " > 2*1900.");
        }
        int jj, l;
        double temp_mjd;

        jj = (14 - month) / 12;
        l = year % 1900 - jj;
        temp_mjd = day - 18234 + (l * 1461) / 4 + ((month - 2 + jj * 12) * 367) / 12;
        temp_mjd += (hour * 3600 + mins * 60 + sec) / 86400.;

        return temp_mjd;
    }


    public static int getYear(double mjd) {
        return new Time(mjd).year;
    }


    public int get(int what) throws NumberFormatException {
        switch (what) {
            case YEAR:
                return year;
            case MONTH:
                return month;
            case DAY:
                return day;
            case HOUR:
                return hour;
            case MINUTE:
                return mins;
            case SECOND:
                return (int) sec;
            default:
                throw new IllegalArgumentException("Invalid type : " + what);
        }
    }


    public String getAsText(int what) throws NumberFormatException {
        int n = get(what);
        String res = "" + n;
        if (res.length() == 1) {
            res = "0" + res;
        }
        return res;
    }


    public int getYear() {
        return year;
    }


    public void setYear(int year) throws IllegalArgumentException {
        if (year < 1950 || year > 2100) {
            throw new IllegalArgumentException("Wrong year " + year);
        }
        this.year = year;
    }


    public int getMonth() {
        return month;
    }


    public void setMonth(int month) throws IllegalArgumentException {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Wrong month " + month);
        }
        this.month = month;
    }


    public int getDay() {
        return day;
    }


    public int getHour() {
        return hour;
    }


    public int getMinutes() {
        return mins;
    }


    public void setMinutes(int mins) {
        this.mins = mins;
    }


    public double getSeconds() {
        return sec;
    }


    public void setSeconds(double sec) {
        this.sec = sec;
    }


    /**
     * This is a quick hack. There could be a better solution
     */
    public int getDayOfTheYear() {
        return (int) (getMjd() - new Time(year, 1, 1, 0, 0, 0).getMjd());
    }


    public double getMjd() {
        return getMjd(year, month, day, hour, mins, sec);
    }


    @Override
    /**
     * Return string representation of the contents of the object. Should not
     * really be used for anything but debugging?
     */
    public String toString() {
        String yeart, montht, dayt, hourt, minst, sect;
        yeart = String.valueOf(year);
        montht = String.valueOf(month);
        dayt = String.valueOf(day);
        hourt = String.valueOf(hour);
        minst = String.valueOf(mins);

        sect = String.valueOf((int) sec);
        if (sect.length() == 1) {
            sect = "0" + sect;
        }

        // this is correct, but MjdEditPanel (MjdEditorPanel?) should be revised to function properly
        //sect = String.valueOf(((int)sec*1e6)/1e6);
        //if (sect.indexOf('.') == 1) { sect = "0" + sect; }
        if (montht.length() == 1) {
            montht = "0" + montht;
        }
        if (dayt.length() == 1) {
            dayt = "0" + dayt;
        }
        if (hourt.length() == 1) {
            hourt = "0" + hourt;
        }
        if (minst.length() == 1) {
            minst = "0" + minst;
        }

        return yeart + "-" + montht + "-" + dayt + " " + hourt + ":" + minst + ":" + sect;//*/

        //=============================================================================
        // Copied from the API documentation for java.util.Formatter:
        // 'd' = (integer argument) "The result is formatted as a decimal integer"
        // 'e' = (floating point argument) "The result is formatted as a decimal number in computerized scientific notation"
        // 'f' = (floating point argument) "The result is formatted as a decimal number"
        // 'g' = (floating point argument) "The result is formatted using computerized
        //       scientific notation or decimal format, depending on the precision and the value after rounding."
        // 's' = (general) "If the argument arg is null, then the result is "null".
        //       If arg implements Formattable, then arg.formatTo is invoked. Otherwise,
        //       the result is obtained by invoking arg.toString()."
        // -----
        // Not sure how to safely format seconds to both display decimals and
        // zero-padding (two digits left of decimal). Therefore done manually.
        /*String secStr = String.valueOf(sec);
         if (sec < 10) {
         secStr = "0" + secStr;
         }*/
        // Special printout of "sec" since it might use scientific notation.
        // Printouts like "2004-02-11 00:00:01.0477378964424133E-7" can be misleading if one misses the ending "E-7".
        //return String.format("%02d-%02d-%02d %02d:%02d:%02d (sec=%g)", year, month, day, hour, mins, (int) sec, sec);
    }


    public static String toString(double mjd) {
        Time t = new Time(mjd);
        return t.toString();
    }


    public static boolean isValid(String time) {
        // Time: "1996-12-01 12:00:00"
        int yeart, montht, dayt, hourt, minst;
        double sect = 0;

        if (time.length() < 16) {
            return false;
        }
        if ((time.charAt(4) != '-') || (time.charAt(7) != '-') || (time.charAt(10) != ' ') || (time.charAt(13) != ':')) {
            return false;
        }
        //if (time.length() != 16) { return false;}
        try {
            yeart = Integer.valueOf(time.substring(0, 4)).intValue();
            montht = Integer.valueOf(time.substring(5, 7)).intValue();
            dayt = Integer.valueOf(time.substring(8, 10)).intValue();
            hourt = Integer.valueOf(time.substring(11, 13)).intValue();
            minst = Integer.valueOf(time.substring(14, 16)).intValue();
            if (time.length() >= 19) {
                sect = Integer.valueOf(time.substring(17, 19)).intValue();
            }
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (!isValid(yeart, montht, dayt, hourt, minst, sect)) {
            return false;
        }
        return true;
    }


    /**
     * NOTE: Technically does not correctly detect leap years (years divisible
     * by 400 but not by 100 are not leap years). This should not be a problem
     * since the problem does not appear for years 1901-2099.
     */
    public static boolean isValid(int yeart, int montht, int dayt, int hourt,
            int minst, double sect) {

        if ((hourt > 23) || (hourt < 0) || (minst > 59) || (minst < 0) || (sect
                >= 60) || (sect < 0)) {
            return false;
        }
        if ((yeart < 1900) || (yeart > 4000) || (montht < 1) || (montht > 12) || (dayt < 1)) {
            return false;
        }

        if ((montht == 1) && (dayt > 31)) {
            return false;
        }

        if ((montht == 2) && (yeart % 4 == 0) && (dayt > 29)) {
            return false;
        }
        if ((montht == 2) && (dayt > 30)) {
            return false;
        }

        if ((montht == 3) && (dayt > 31)) {
            return false;
        }
        if ((montht == 4) && (dayt > 30)) {
            return false;
        }
        if ((montht == 5) && (dayt > 31)) {
            return false;
        }
        if ((montht == 6) && (dayt > 30)) {
            return false;
        }
        if ((montht == 7) && (dayt > 31)) {
            return false;
        }
        if ((montht == 8) && (dayt > 31)) {
            return false;
        }
        if ((montht == 9) && (dayt > 30)) {
            return false;
        }
        if ((montht == 10) && (dayt > 31)) {
            return false;
        }
        if ((montht == 11) && (dayt > 30)) {
            return false;
        }
        if ((montht == 12) && (dayt > 31)) {
            return false;
        }

        return true;
    }


    public Object clone() {
        return new Time(year, month, day, hour, mins, sec);
    }


    public static double getGSMTime(double mjd) {
        return new Julian(mjd).getGSMTime();
    }


    /**
     * converts mjd to julian
     */
    public static Julian getJulian(double mjd) {
        return new Julian(mjd);
    }


    /**
     * Informal test code.
     */
    public static void main(String[] args) {
        test_mjdConversions();
    }


    /**
     * Informal test code.
     */
    private static void test_mjdConversions() {
        //final ovt.util.TimeFormat tf = new ovt.util.TimeFormat();

        System.out.println("mjd=0   : " + new Time(0.0));   // Determine epoch (back conversion not checked here).

        {
            System.out.println("Test whether conversions mjd-->Time-->mjd are consistent, or if they drift (much or little).");
            // Starts with mjd value.
            final int N_iterations = 2;
            final double[] mjdTestValues = new double[]{0, 1000, new Time(2004, 02, 11, 00, 00, 00).getMjd()};
            for (double mjdTestValue : mjdTestValues) {
                double mjd1 = mjdTestValue;

                for (int i = 0; i < N_iterations; i++) {
                    final Time time = new Time(mjd1);
                    final double mjd2 = time.getMjd();
                    //System.out.println("mjd1=" + mjd1 + ";  time=" + time + ";  mjd2=" + mjd2);
                    System.out.println(String.format("mjd1=%12f;  time=%-39s;  mjd2=%12f", mjd1, time, mjd2));
                    mjd1 = mjd2;
                }
            }
        }

        {
            System.out.println("Test whether conversions Time-->mjd-->Time are consistent, or if they drift (much or little).");
            // Starts with Time (year-month-...) value.
            final int N_iterations = 1;
            final Time[] timeTestValues = new Time[]{
                new Time(2004, 02, 11, 00, 00, 00),
                new Time(2015, 4, 9, 00, 00, 00),
                new Time(1900, 1, 1, 00, 00, 00),
                new Time(1901, 1, 1, 00, 00, 00),
                new Time(1902, 1, 1, 00, 00, 00),
                new Time(1910, 1, 1, 00, 00, 00),
                new Time(1920, 1, 1, 00, 00, 00),
                new Time(1930, 1, 1, 00, 00, 00),
                new Time(1940, 1, 1, 00, 00, 00),
                new Time(1949, 12, 31, 23, 59, 59),
                new Time(1950, 1, 1, 00, 00, 00),
                new Time(1960, 1, 1, 00, 00, 00),
                new Time(1970, 1, 1, 00, 00, 00),
                new Time(1980, 1, 1, 00, 00, 00),
                new Time(1990, 1, 1, 00, 00, 00)
            };
            for (Time timeTestValue : timeTestValues) {
                Time time1 = timeTestValue;

                for (int i = 0; i < N_iterations; i++) {
                    final double mjd = time1.getMjd();
                    Time time2 = new Time(mjd);
                    System.out.println(String.format("time1=%-39s;  mjd=%12f;  time2=%-39s", time1, mjd, time2));
                    time1 = time2;
                }
                System.out.println("--");
            }
        }

    }
}
