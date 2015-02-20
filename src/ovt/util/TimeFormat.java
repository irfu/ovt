/*=========================================================================

Program:   Orbit Visualization Tool
Source:    $Source: /stor/devel/ovt2g/ovt/util/TimeFormat.java,v $
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


package ovt.util;

import ovt.datatype.*;
import ovt.beans.*;
import ovt.object.*;
import ovt.interfaces.*;

import java.beans.*;
import java.text.*;
import java.lang.*;
import java.util.*;
import java.util.regex.*;

/**
 * Transforms ovt.datatype.Time into String and vice versa.
 *
 * @author  ko
 * @version 
 */
public class TimeFormat extends OVTObject {
    /** ISO time string format <CODE>yyyy-mm-ddTHH:MM:ss.wwwwwwZ</CODE>
     * as described in the CEF data file syntax
     * http://www.space-plasma.qmul.ac.uk/csds/welcome.html
     */
    public final static int ISO		= 1; 
    /** date format : <CODE>yyyy mm dd HH MM ss.wwwwww</CODE> 
     * During parsing characters in between values are ignored
     *
     * Will parse, e.g. 2002-02-11 03:03:59 and 2002/02$11 03:03:59
     * Will also parse 2002a02(11_03D03*59.4354545645646
     * and 2002 2 1 3 3 0
     */
    public final static int ISO_NONSTRICT = 2;
    /** Modified Julian Day format - the number of days since 1-Jan-1950 */
    public final static int DAYS_SINCE_1950_01_01 = 3;
    /** ISDAT epoch format - the number of seconds since 1-Jan-1970 */
    public final static int SECS_SINCE_1970_01_01 = 4;

    
    /** Holds value of dateFormat. By default {@link ISO_NONSTRICT}. */
    private int dateFormat = ISO_NONSTRICT;
    
    private DecimalFormat oo_format = new DecimalFormat("00");    
    private DecimalFormat ss_wwwwww_format = new DecimalFormat("00.000000");
    private DecimalFormat secs_format = new DecimalFormat();
    private DecimalFormat days_format = new DecimalFormat();
    /** Creates new TimeFormat */
    public TimeFormat() {
    	DecimalFormatSymbols symb = new DecimalFormatSymbols();
  	symb.setDecimalSeparator('.');
    	ss_wwwwww_format.setDecimalFormatSymbols(symb);
	secs_format.setMaximumFractionDigits(6); // to account for 1e6 sec
	secs_format.setGroupingUsed(false);
	secs_format.setDecimalFormatSymbols(symb);
	days_format.setMaximumFractionDigits(11); // to account for 1e6 sec
	days_format.setGroupingUsed(false);
	days_format.setDecimalFormatSymbols(symb);
    }
    
    public String format(double mjd) {
    	return format(new ovt.datatype.Time(mjd));
    }
    
    public String format(ovt.datatype.Time time) {
        String date = "", hours = "";
        switch (dateFormat) {
            case ISO :
	    	date = ""+time.getYear()+"-"+
			oo_format.format(time.getMonth())+"-"+
			oo_format.format(time.getDay())+"T"+
			oo_format.format(time.getHour())+":"+
			oo_format.format(time.getMinutes())+":"+
			ss_wwwwww_format.format(time.getSeconds())+"Z";
		break; 
	    case ISO_NONSTRICT:
	    	date = ""+time.getYear()+" "+
			oo_format.format(time.getMonth())+" "+
			oo_format.format(time.getDay())+" "+
			oo_format.format(time.getHour())+" "+
			oo_format.format(time.getMinutes())+" "+
			ss_wwwwww_format.format(time.getSeconds());
		break;
	    case DAYS_SINCE_1950_01_01:
	    	date = days_format.format(time.getMjd());
		break;
	    case SECS_SINCE_1970_01_01:	    
	    	date = ""+(time.getMjd()-Time.Y1970)*3600.*24.;//secs_format.format(time.getMjd()*3600.*24.);
		break;
            
        }
        return date;
    }
    
    public StringBuffer format(Object obj, StringBuffer buf, FieldPosition pos) {
        return new StringBuffer("a");
    }
    
    /** Getter for property dateFormat.
     * @return Value of property dateFormat.
 */
    public int getDateFormat() {
        return dateFormat;
    }
    
    public String getDateFormatString() {
        return getFormat(dateFormat);
    }
    /** Setter for property dateFormat.
     * @param dateFormat New value of property dateFormat.
 */
    public void setDateFormat(int dateFormat) throws IllegalArgumentException {
        if (dateFormat != ISO  &&  
		dateFormat != ISO_NONSTRICT  && 
		dateFormat != DAYS_SINCE_1950_01_01 && 
		dateFormat != SECS_SINCE_1970_01_01)
            throw new IllegalArgumentException("Invalid format type '"+ dateFormat +"'");
        int oldDateFormat = this.dateFormat;
        this.dateFormat = dateFormat;
        firePropertyChange ("dateFormat", new Integer (oldDateFormat), new Integer (dateFormat));
    }
    
    
    public static String getFormat(int type) {
        switch (type) {
            case ISO			: return "yyyy-mm-ddTHH:MM:ss.wwwwwwZ";
            case ISO_NONSTRICT		: return "yyyy mm dd HH MM ss.wwwwww";
            case DAYS_SINCE_1950_01_01	: return "Days since 1950-01-01";
            case SECS_SINCE_1970_01_01	: return "Seconds since 1970-01-01";
        }
        throw new IllegalArgumentException("Invalid format type '"+ type +"'");
    }
    
    public static int[] getDateFormats() {
        return new int[]{ ISO, ISO_NONSTRICT, DAYS_SINCE_1950_01_01, SECS_SINCE_1970_01_01 };
    }
    
    public static String[] getDateFormatNames() {
        int[] formats = getDateFormats();
        String[] res = new String[formats.length];
        for (int i=0; i<formats.length; i++)
            res[i] = getFormat(formats[i]);
        return res;
    }
    
    public DoubleAndInteger parseMjd(String str) throws NumberFormatException {
        TimeAndStringLength a = parse(str);
        return new DoubleAndInteger(a.time.getMjd(), a.length);
    }
    
    public TimeAndStringLength parse(String str) throws NumberFormatException {
        int year=0, month=0, day=0, hour=0, mins=0;
	double secs=0;
        int offset = 11;
        int length = -1;
        
        switch (dateFormat) {
            case ISO:
                year = Integer.valueOf(str.substring(0,4)).intValue();
                month = Integer.valueOf(str.substring(5,7)).intValue();
                day = Integer.valueOf(str.substring(8,10)).intValue();
		if (str.charAt(10) != 'T') throw new
			NumberFormatException("Invalid ISO format yyyy-mm-ddTHH:MM:ss.wwwwwwZ");
		hour = Integer.valueOf(str.substring(11,13)).intValue();
		mins = Integer.valueOf(str.substring(14,16)).intValue();
		secs = Double.valueOf(str.substring(17,26)).doubleValue();
		if (str.charAt(26) != 'Z') throw new
			NumberFormatException("Invalid ISO format yyyy-mm-ddTHH:MM:ss.wwwwwwZ");
		length = 26;
                break;
            case ISO_NONSTRICT:
	    	// select groups of digits in the form ddddd or dddd.ddd
		// first are needed for year, month, day, hour, minute
		// the second is needed for seconds
	        Pattern pattern = Pattern.compile("(\\d+\\.\\d+)|(\\d+)");	
		Matcher matcher = pattern.matcher(str);
		matcher.find();
		year = Integer.valueOf(matcher.group()).intValue();
		matcher.find();
		month = Integer.valueOf(matcher.group()).intValue();
		matcher.find();
		day = Integer.valueOf(matcher.group()).intValue();
		matcher.find();
		hour = Integer.valueOf(matcher.group()).intValue();
		matcher.find();
		mins = Integer.valueOf(matcher.group()).intValue();
		matcher.find();
		secs = Double.valueOf(matcher.group()).doubleValue();
		length = matcher.end();
                break;
        }

        try {
            return new TimeAndStringLength(new Time(year, month, day, hour, mins, secs), length);
        } catch (IllegalArgumentException ill_a_e) {
            throw new NumberFormatException(ill_a_e.getMessage());
        }
    }
    
    
    /*public Time parse(StringBuffer str) throws NumberFormatException {
        int year=0, month=0, day=0, hour=0, mins=0, sec=0;
        try {
        switch (dateFormat) {
            case YYYY_MM_DD:
            case YYYY_MM_DD_SLASH:
                year = Integer.valueOf(str.substring(0,4)).intValue();
                month = Integer.valueOf(str.substring(5,7)).intValue();
                day = Integer.valueOf(str.substring(8,10)).intValue();
                str.delete(0, 11);
                break;
            case YYYY_DDD:
                year = Integer.valueOf(str.substring(0,4)).intValue();
                month = 0; // needs a check
                day  = Integer.valueOf(str.substring(5,8)).intValue();
                str.delete(0, 9);
                break;
        }
        switch (hoursFormat) {
            case HH_MM_SS:
                StringTokenizer st = new StringTokenizer(str.substring(6, str.length()));
                //int endIndex = StringUtils.doubleEndsAt(str, offset+6);
                String s = st.nextToken();
                sec = Integer.valueOf(s).intValue();
                str.delete(5, s.length());
            case HH_MM:
                hour = Integer.valueOf(str.substring(0, 2)).intValue();
                mins = Integer.valueOf(str.substring(3, 5)).intValue();
                str.delete(
                break;
            case HH_HHHH:
                StringTokenizer st2 = new StringTokenizer(str.substring(offset, str.length()));
                hour = Integer.valueOf(st2.nextToken()).intValue();
                break;
            case SSSSS_SS:
                StringTokenizer st3 = new StringTokenizer(str.substring(offset, str.length()));
                sec = Integer.valueOf(st3.nextToken()).intValue();
                break;    
        }
        } catch ( NoSuchElementException e2) {
            throw new NumberFormatException(e2.getMessage());
        }
        return new Time(year, month, day, hour, mins, sec, 0);
    }*/
    
    public static int guessDateFormat(String str) {
        
        if (str.indexOf('-') == 4 && str.indexOf('-', str.indexOf('-')+1) == 7) {
            if (isInteger(str, 0, 4) && isInteger(str, 5, 7) && isInteger(str, 8, 10))
                return ISO;
            else 
                throw new NumberFormatException("Wrong ISO format 'yyyy-mm-ddTHH:MM:ss.wwwwwwZ' ("+str+")");
        }
        if (str.indexOf(' ') == 4 && str.indexOf(' ', str.indexOf(' ')+1) == 7) {
            if (isInteger(str, 0, 4) && isInteger(str, 5, 7) && isInteger(str, 8, 10))
                return ISO_NONSTRICT;
            else 
                throw new NumberFormatException("Wrong clean ISO format 'yyyy mm dd HH MM ss.wwwwww' ("+str+")");
        }
        throw new NumberFormatException("Wrong date format ("+str+")");
    }
    
    /** Can distinguish hh:mm and hh:mm:ss formats
    public static int guessHoursFormat(String str) throws NumberFormatException {
        int semicIndex1 = str.indexOf(':');
        int semicIndex2 = str.indexOf(':', semicIndex1+1);
        
        if (semicIndex1 == 2) { // could be hh:mm or hh:mm:ss
            if (!isInteger(str, 0, 2)) 
                throw new NumberFormatException("Wrong hh format ("+str+")");
            if (!isInteger(str, 3, 5))
                throw new NumberFormatException("Wrong mm format ("+str+")");
            if (semicIndex2 == 5) {
                if (isInteger(str, 6, 8))
                    return HH_MM_SS;
                else 
                    throw new NumberFormatException("Wrong ss format ("+str+")");
            } else return HH_MM;
            
        }
        // left patterns: hh.hhhh and ssssss.ss
        // I don't know how to distinguish them..
        throw new NumberFormatException("Wrong time format ("+str+")");
        
    }*/
    
    
    private static boolean isInteger(String str, int beginIndex, int endIndex) {
        try {
            new Integer(str.substring(beginIndex, endIndex));
        } catch (NumberFormatException e2) {
            return false;
        } catch (IndexOutOfBoundsException e3) {
            return false;
        }
        
        return true;
    }
    
    private static boolean isDouble(String str, int beginIndex, int endIndex) {
        try {
            new Double(str.substring(beginIndex, endIndex));
        } catch (NumberFormatException e2) {
            return false;
        } catch (IndexOutOfBoundsException e3) {
            return false;
        }
        return true;
    }
    
    public Descriptors getDescriptors() {
        if (descriptors == null) {
            try {
                descriptors = new Descriptors();
                
                // date format
                BasicPropertyDescriptor pd = new BasicPropertyDescriptor("dateFormat", this);
                pd.setDisplayName("Date format");
                pd.setMenuAccessible(false);
                GUIPropertyEditor editor = new ComboBoxPropertyEditor(pd, TimeFormat.getDateFormats(), TimeFormat.getDateFormatNames());
                pd.setPropertyEditor(editor);
                addPropertyChangeListener("dateFormat", editor);
                descriptors.put(pd);
                
                
            } catch (IntrospectionException e2) {
                System.out.println(getClass().getName() + " -> " + e2.toString());
                System.exit(0);
            }
        }
        return descriptors;
    }
    
    public static void main(String[] args) {
	//}
		
        /*Time time = new Time("2001-02-03 10:00:36");
        TimeFormat tf = new TimeFormat();
        tf.setHoursFormat(tf.HH_HHHH);
        
        int[] f = TimeFormat.guessDateAndHoursFormat(args[0]);
        System.out.println(TimeFormat.getFormat(f[0])+" "+TimeFormat.getFormat(f[1]));
        String str = "123456";
        System.out.println(str.substring(0, 2));*/
	
    	Pattern pattern = Pattern.compile("(\\d+\\.\\d+)|(\\d+)");
	
	Matcher matcher = pattern.matcher("2004 02-03 13:00:55.66666 3.45");
	
	while(matcher.find()) {
		System.out.println("I found the text \"" + matcher.group() +
			"\" starting at index " + matcher.start() +
			" and ending at index " + matcher.end() + ".");
	}
	
    }
}

class TimeAndStringLength {

    public Time time = null;
    public int length = 0;
    public TimeAndStringLength(Time time, int length) {
        this.time = time;
        this.length = length;
    }
}
