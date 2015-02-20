/*=========================================================================

  Program:   Orbit Visualization Tool
  Source:    $Source: /stor/devel/ovt2g/ovt/util/StringUtils.java,v $
  Date:      $Date: 2003/09/28 17:52:56 $
  Version:   $Revision: 1.3 $


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
 * StringUtils.java
 *
 * Created on July 16, 2001, 11:14 PM
 */

package ovt.util;

/**
 *
 * @author  root
 * @version 
 */
public class StringUtils extends Object {

    /** Creates new StringUtils */
    public StringUtils() {
    }
    
    /** cut's double from string
    public static double firstDouble(String str, int beginindex) {
        int startIndex = doubleStartsAt(str, beginindex);
        int endIndex = doubleEndsAt(str, startIndex);
        return new Double(str.substring(startIndex, endIndex+1)).doubleValue();
    }*/

    /** search for the last index of double in the string starrting from beginindex*/
    public static int doubleEndsAt(StringBuffer str, int beginindex) {
        int i = 0;
        for ( i = beginindex; i<str.length(); i++) {
            char ch = str.charAt(i);
            // if this char is not a digit or "." - return index of previous char
            if (!isDecimalDigitNumber(ch) && ch != '.' && ch != 'e' && ch != '-' && ch != '+') { 
                if (i>0) {
                    char c = str.charAt(i-1);
                    if (isDecimalDigitNumber(c) || c == '.') return i-1;
                } else return -1;
            }
        }
        return i-1;
    }

    public static int doubleEndsAt(String text, int beginindex) {
        return doubleEndsAt(new StringBuffer(text), beginindex);
    }
    /** search for the last index of double in the string starrting from beginindex*/
    public static int doubleStartsAt(StringBuffer str, int fromIndex) {
        int i = 0;
        for ( i = fromIndex; i<str.length(); i++) {
            char ch = str.charAt(i);
            // if this char is not a digit or "." - return index of previous char
            if (isDecimalDigitNumber(ch)) { 
                //Log.log("DIGIT='"+ch+"'");
                return i;
            }
            if (ch == '.' || ch == '-') { 
                // check if the next char is digit - ok
                if (i+1 < str.length()) {
                    if (isDecimalDigitNumber(str.charAt(i+1))) return i;
                }
            }
        }
        return -1;
    }
    public static int doubleStartsAt(String text, int fromIndex) {
        return doubleStartsAt(new StringBuffer(text), fromIndex);
    }
    
    /** cut's double from string*/
    public static boolean isDecimalDigitNumber(char ch) {
        int type = Character.getType(ch);
        return (type == Character.DECIMAL_DIGIT_NUMBER);
    }
    
    
}
